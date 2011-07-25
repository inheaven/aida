#include "cula_wrapper.h"


void check_memory(JNIEnv * env, void * arg) {
	if (arg != NULL) {
		return;
	}

	env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), "Out of memory transferring array to native code in F2J JNI");
}

void checkStatus(culaStatus status)
{
    char buf[80];

    if(!status)
        return;

    culaGetErrorInfoString(status, culaGetErrorInfo(), buf, sizeof(buf));
    printf("%s\n", buf);

    culaShutdown();
    exit(EXIT_FAILURE);
}

void culaInit(){

}

JNIEXPORT void JNICALL
Java_ru_inhell_aida_cula_CULA_vssa
    (JNIEnv *env, jobject calling_obj, jint n, jint l, jint p, jintArray pp, jint m, jfloatArray timeseries,
        jfloatArray forecast, jint count){
        jboolean isCopy = JNI_TRUE;

	    culaStatus status;

//	    double t = getHighResolutionTime();

	    status = culaInitialize();
        checkStatus(status);

	    jint *jni_pp = (jint *)env->GetPrimitiveArrayCritical(pp, &isCopy);
	    check_memory(env, jni_pp);

	    jfloat *jni_timeseries = (jfloat *)env->GetPrimitiveArrayCritical(timeseries, &isCopy);
	    check_memory(env, jni_timeseries);

	    jfloat *jni_forecast = (jfloat *)env->GetPrimitiveArrayCritical(forecast, &isCopy);
	    check_memory(env, jni_forecast);

	    int k = n - l + 1;
	    int ld = l - 1;
	    int f_size = n + m + l - 1;

	    float *x = new float[l*k];
	    float *xi = new float[l*k];
	    float *g = new float[n * p];
	    float *s = new float[l];
	    float *u = new float[l*l];
	    float *vt = new float[k*k];

	    float *ui = new float[l];
	    float *vi = new float[k];
	    float *xii = new float[l*k];

	    float *z = new float[l * (n + m)];
	    float *r = new float[ld];

	    float *vd = new float[ld * m];
	    float *pi = new float[m];

	    float *vdxvdt = new float[ld * ld];
	    float *rxrt = new float[ld * ld];
	    float *pr = new float[ld * ld];

	    float *yd = new float[(l-1)];
	    float *zi = new float[l];


//        printf("0. %10.4f \n", getHighResolutionTime() - t);

        for (int index = 0; index < count; ++index){
            for (int j = 0; j < k; ++j){
                memcpy(x + j*l, jni_timeseries + (j + index), l * sizeof(float));
            }

//            t = getHighResolutionTime();

            status = culaSgesvd('S', 'S', l, k, x, l, s, u, l, vt, k);
            checkStatus(status);

//            printf("1. %10.4f ", getHighResolutionTime() - t);

            memset(xi, 0, l*k*sizeof(float));

//            t = getHighResolutionTime();

            for (int ii=0; ii < p; ++ii){
                int i = jni_pp[ii];

                memcpy(ui, u + i*l, l * sizeof(float));

                for (int j = 0; j < k; ++j){
                    vi[j] = vt[i + j*k];
                }

                status = culaSgemm('N', 'T', l, k, 1, s[i], ui, l, vi, k, 0, xii, l);
                checkStatus(status);

                for (int j = 0; j < l*k; ++j){
                    xi[j] += xii[j];
                }
            }

//            printf("2. %10.4f ", getHighResolutionTime()- t);

//            t = getHighResolutionTime();

            float v2 = 0;

            for (int i=0; i < m; ++i){
                memcpy(vd + i*ld, u + i*l, ld * sizeof(float));
                pi[i] = u[l-1 + i*l];
                v2 += pi[i]*pi[i];
            }

            memset(r, 0, ld*sizeof(float));

            for (int i = 0; i < m; ++i){
                for (int j = 0; j < ld; ++j){
                    r[j] += vd[j + i*ld] * pi[i];
                }
            }

            for (int j=0; j < ld; ++j){
                r[j] /= (1-v2);
            }

//            printf("9. %10.4f ", getHighResolutionTime()- t);

//            t = getHighResolutionTime();

            status = culaSgemm('N', 'T', ld, ld, m, 1, vd, ld, vd, ld, 0, vdxvdt, ld);
            checkStatus(status);

            status = culaSgemm('N', 'T', ld, ld, 1, 1-v2, r, ld, r, ld, 0, rxrt, ld);
            checkStatus(status);

//            printf("3. %10.4f  ", getHighResolutionTime()- t);

            for (int i = 0; i < ld * ld; ++i){
                pr[i] = vdxvdt[i] + rxrt[i];
            }

            memcpy(z, xi, l*k*sizeof(float));

//            t = getHighResolutionTime();

            for (int i = k; i < n + m; ++i){
                memcpy(yd, z + (1 + (i-1)*l), ld * sizeof(float));

                status = culaSgemm('N', 'N', ld, 1, ld, 1, pr, ld, yd, ld, 0, zi, ld);
                checkStatus(status);

                zi[l-1] = 0;
                for (int j = 0; j < ld; ++j){
                    zi[l-1] += r[j] * yd[j];
                }

                memcpy(z + i*l, zi, l * sizeof(float));
            }

//            printf("4. %10.4f ", getHighResolutionTime()- t);

//            t = getHighResolutionTime();

            diagonalAveraging(z, l, n + m, jni_forecast, f_size*index);

//            printf("5. %10.4f  \n", getHighResolutionTime()- t);
        }

//        t = getHighResolutionTime();

        env->ReleasePrimitiveArrayCritical(pp, jni_pp, JNI_ABORT);
        env->ReleasePrimitiveArrayCritical(timeseries, jni_timeseries, JNI_ABORT);
        env->ReleasePrimitiveArrayCritical(forecast, jni_forecast, 0);

//        printf("7. %10.4f ", getHighResolutionTime()- t);

//        t = getHighResolutionTime();

        culaShutdown();

//        printf("6. %10.4f  ", getHighResolutionTime()- t);

        delete[] x;
        delete[] xi;
        delete[] g;
        delete[] s;
        delete[] u;
        delete[] vt;
        delete[] ui;
        delete[] vi;
        delete[] xii;
        delete[] z;
        delete[] r;
        delete[] vd;
        delete[] pi;
        delete[] vdxvdt;
        delete[] rxrt;
        delete[] pr;
        delete[] yd;
        delete[] zi;

//        printf("10. %10.4f  \n", getHighResolutionTime()- t);
	}

void diagonalAveraging(float *y, int rows, int cols, float *g, int begin){
        int l1 = min(rows, cols);
        int k1 = max(rows, cols);

        int n = l1 + k1 - 1;

        for (int k = 0; k < l1 - 1; ++k){
            g[begin + k] = getSum(y, rows, cols, 1, k + 1, k) / (k + 1);
        }

        for (int k = l1 - 1; k < k1; ++k){
            g[begin + k] = getSum(y, rows, cols, 1, l1, k) / l1;
        }

        for (int k = k1; k < n; ++k){
            g[begin + k] = getSum(y, rows, cols, k - k1 + 2, n - k1 + 1, k) / (n - k);
        }
    }

float getSum(float *y, int rows, int cols, int first, int last, int k){
        float sum = 0;

        for (int m = first; m <= last; ++m){
            sum += rows < cols ? y[m - 1 + (k - m + 1)*rows] : y[k - m + 1 + (m - 1)*rows];
        }

        return sum;
    }

    double getHighResolutionTime(void)
    {
        double freq;
        double seconds;
        LARGE_INTEGER end_time;
        LARGE_INTEGER performance_frequency_hz;

        QueryPerformanceCounter(&end_time);
        QueryPerformanceFrequency(&performance_frequency_hz);

        seconds = (double) end_time.QuadPart;
        freq = (double) performance_frequency_hz.QuadPart;
        seconds /= freq;

        return seconds;
    }


