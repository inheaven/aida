#include "acml.h"
#include "acml_wrapper.h"
#include <stdio.h>
#include <algorithm>
#include <string.h>
#include <math.h>

void check_memory(JNIEnv * env, void * arg) {
	if (arg != NULL) {
		return;
	}
	/*
	 * WARNING: Memory leak
	 *pgftnrtl.dll
	 * This doesn't clean up successful allocations prior to throwing this exception.
	 * However, it's a pretty dire situation to be anyway and the client code is not
	 * expected to recover.
	 */
	
	env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), "Out of memory transferring array to native code in F2J JNI");
}

// LAPACK

JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_sgesvd 
	(JNIEnv *env, jobject calling_obj, jstring jobu, jstring jobvt, jint m, jint n, jfloatArray a, jint lda, jfloatArray s, jfloatArray u, jint ldu, jfloatArray vt, jint ldvt, jintArray info)
{

    jboolean isCopy = JNI_TRUE;

	char * jni_jobu = (char *) env->GetStringUTFChars(jobu, &isCopy);
	char * jni_jobvt = (char *)env->GetStringUTFChars(jobvt, &isCopy);

	jfloat *jni_a = (jfloat *)env->GetPrimitiveArrayCritical(a, &isCopy);
	check_memory(env, jni_a);

	jfloat *jni_s = (jfloat *)env->GetPrimitiveArrayCritical(s, &isCopy);
	check_memory(env, jni_s);

	jfloat *jni_u = (jfloat *)env->GetPrimitiveArrayCritical(u, &isCopy);
	check_memory(env, jni_u);

	jfloat *jni_vt = (jfloat *)env->GetPrimitiveArrayCritical(vt, &isCopy);
	check_memory(env, jni_vt);

        jint *jni_info = (jint *)env->GetPrimitiveArrayCritical(info, &isCopy);
	check_memory(env, jni_info);

	//MP
	//acmlsetnumthreads(acmlgetnumprocs());

//    env->MonitorEnter(calling_obj);
	sgesvd(jni_jobu[0], jni_jobvt[0], (long)m, (long)n, jni_a, (long)lda, jni_s, jni_u, (long)ldu, jni_vt, (long)ldvt, (int *)&jni_info[0]);
//	env->MonitorExit(calling_obj);

	env->ReleaseStringUTFChars(jobu, jni_jobu);
	env->ReleaseStringUTFChars(jobvt, jni_jobvt);
	env->ReleasePrimitiveArrayCritical(a, jni_a, JNI_ABORT);
	env->ReleasePrimitiveArrayCritical(s, jni_s, 0);
	env->ReleasePrimitiveArrayCritical(u, jni_u, 0);
	env->ReleasePrimitiveArrayCritical(vt, jni_vt, 0);
	env->ReleasePrimitiveArrayCritical(info, jni_info, 0);
}

JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_sgesdd
	(JNIEnv *env, jobject calling_obj, jstring jobz, jint m, jint n, jfloatArray a, jint lda, jfloatArray s, jfloatArray u, jint ldu, jfloatArray vt, jint ldvt, jintArray info)
{
    jboolean isCopy = JNI_TRUE;

	char * jni_jobz = (char *) env->GetStringUTFChars(jobz, &isCopy);

	jfloat *jni_a = (jfloat *)env->GetPrimitiveArrayCritical(a, &isCopy);
	check_memory(env, jni_a);

	jfloat *jni_s = (jfloat *)env->GetPrimitiveArrayCritical(s, &isCopy);
	check_memory(env, jni_s);

	jfloat *jni_u = (jfloat *)env->GetPrimitiveArrayCritical(u, &isCopy);
	check_memory(env, jni_u);

	jfloat *jni_vt = (jfloat *)env->GetPrimitiveArrayCritical(vt, &isCopy);
	check_memory(env, jni_vt);

        jint *jni_info = (jint *)env->GetPrimitiveArrayCritical(info, &isCopy);
	check_memory(env, jni_info);

	//MP
	//acmlsetnumthreads(acmlgetnumprocs());

	//env->MonitorEnter(calling_obj);
	sgesdd(jni_jobz[0], (long)m, (long)n, jni_a, (long)lda, jni_s, jni_u, (long)ldu, jni_vt, (long)ldvt, (int *)&jni_info[0]);
	//env->MonitorExit(calling_obj);

	env->ReleaseStringUTFChars(jobz, jni_jobz);
	env->ReleasePrimitiveArrayCritical(a, jni_a, JNI_ABORT);
	env->ReleasePrimitiveArrayCritical(s, jni_s, 0);
	env->ReleasePrimitiveArrayCritical(u, jni_u, 0);
	env->ReleasePrimitiveArrayCritical(vt, jni_vt, 0);
	env->ReleasePrimitiveArrayCritical(info, jni_info, 0);
}


JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_dgesvd 
	(JNIEnv *env, jobject calling_obj, jstring jobu, jstring jobvt, jint m, jint n, jdoubleArray a, jint lda, jdoubleArray s, jdoubleArray u, jint ldu, jdoubleArray vt, jint ldvt, jintArray info)
{	

	char * jni_jobu = (char *) env->GetStringUTFChars(jobu, JNI_FALSE);
	char * jni_jobvt = (char *)env->GetStringUTFChars(jobvt, JNI_FALSE);

	jdouble *jni_a = (jdouble *)env->GetPrimitiveArrayCritical(a, JNI_FALSE);
	check_memory(env, jni_a);

	jdouble *jni_s = (jdouble *)env->GetPrimitiveArrayCritical(s, JNI_FALSE);
	check_memory(env, jni_s);

	jdouble *jni_u = (jdouble *)env->GetPrimitiveArrayCritical(u, JNI_FALSE);
	check_memory(env, jni_u);

	jdouble *jni_vt = (jdouble *)env->GetPrimitiveArrayCritical(vt, JNI_FALSE);
	check_memory(env, jni_vt);

        jint *jni_info = (jint *)env->GetPrimitiveArrayCritical(info, JNI_FALSE);
	check_memory(env, jni_info);

	//MP
	//acmlsetnumthreads(acmlgetnumprocs());

        //env->MonitorEnter(calling_obj);
	dgesvd(jni_jobu[0], jni_jobvt[0], (long)m, (long)n, jni_a, (long)lda, jni_s, jni_u, (long)ldu, jni_vt, (long)ldvt, (int *)&jni_info[0]);
	//env->MonitorExit(calling_obj);

	env->ReleaseStringUTFChars(jobu, jni_jobu);
	env->ReleaseStringUTFChars(jobvt, jni_jobvt);
	env->ReleasePrimitiveArrayCritical(a, jni_a, 0);
	env->ReleasePrimitiveArrayCritical(s, jni_s, 0);
	env->ReleasePrimitiveArrayCritical(u, jni_u, 0);
	env->ReleasePrimitiveArrayCritical(vt, jni_vt, 0);
	env->ReleasePrimitiveArrayCritical(info, jni_info, 0);
}


//BLAS

JNIEXPORT void JNICALL
Java_ru_inhell_aida_acml_ACML_sgemm 
	(JNIEnv *env, jobject calling_obj, jstring transa, jstring transb, jint m, jint n, jint k, jfloat alpha, jfloatArray a, jint lda, jfloatArray b, jint ldb, jfloat beta, jfloatArray c, jint ldc)
{
    jboolean isCopy = JNI_TRUE;

    char *jni_transa = (char *)env->GetStringUTFChars(transa, &isCopy);
    char *jni_transb = (char *)env->GetStringUTFChars(transb, &isCopy);

    jfloat *jni_a = (jfloat *)env->GetPrimitiveArrayCritical(a, &isCopy);
	check_memory(env, jni_a);

	jfloat *jni_b = (jfloat *)env->GetPrimitiveArrayCritical(b, &isCopy);
	check_memory(env, jni_b);

	jfloat *jni_c = (jfloat *)env->GetPrimitiveArrayCritical(c, &isCopy);
	check_memory(env, jni_c);

	//MP
	//acmlsetnumthreads(acmlgetnumprocs());

    //env->MonitorEnter(calling_obj);
	sgemm(jni_transa[0], jni_transb[0], (long)m, (long)n, (long)k, alpha, jni_a, (long)lda, jni_b, (long)ldb, beta, jni_c, (long)ldc);
	//env->MonitorExit(calling_obj);

	env->ReleaseStringUTFChars(transa, jni_transa);
	env->ReleaseStringUTFChars(transb, jni_transb);
	env->ReleasePrimitiveArrayCritical(a, jni_a, JNI_ABORT);
	env->ReleasePrimitiveArrayCritical(b, jni_b, JNI_ABORT);
	env->ReleasePrimitiveArrayCritical(c, jni_c, 0);
}

JNIEXPORT void JNICALL
Java_ru_inhell_aida_acml_ACML_dgemm
	(JNIEnv *env, jobject calling_obj, jstring transa, jstring transb, jint m, jint n, jint k, jdouble alpha, jdoubleArray a, jint lda, jdoubleArray b, jint ldb, jdouble beta, jdoubleArray c, jint ldc)
{
    char *jni_transa = (char *)env->GetStringUTFChars(transa, JNI_FALSE);
    char *jni_transb = (char *)env->GetStringUTFChars(transb, JNI_FALSE);

    jdouble *jni_a = (jdouble *)env->GetPrimitiveArrayCritical(a, JNI_FALSE);
	check_memory(env, jni_a);

	jdouble *jni_b = (jdouble *)env->GetPrimitiveArrayCritical(b, JNI_FALSE);
	check_memory(env, jni_b);

	jdouble *jni_c = (jdouble *)env->GetPrimitiveArrayCritical(c, JNI_FALSE);
	check_memory(env, jni_c);

	//MP
	//acmlsetnumthreads(acmlgetnumprocs());

	dgemm(jni_transa[0], jni_transb[0], (long)m, (long)n, (long)k, alpha, jni_a, (long)lda, jni_b, (long)ldb, beta, jni_c, (long)ldc);

	env->ReleaseStringUTFChars(transa, jni_transa);
	env->ReleaseStringUTFChars(transb, jni_transb);
	env->ReleasePrimitiveArrayCritical(a, jni_a, 0);
	env->ReleasePrimitiveArrayCritical(b, jni_b, 0);
	env->ReleasePrimitiveArrayCritical(c, jni_c, 0);
}

//TEST

//JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_test
//	(JNIEnv *env, jobject calling_obj, jstring test)
//{
//	printf(env->GetStringUTFChars(test, JNI_FALSE));
//}

JNIEXPORT void JNICALL
Java_ru_inhell_aida_acml_ACML_vssa
    (JNIEnv *env, jobject calling_obj, jint n, jint l, jint p, jintArray pp, jint m, jfloatArray timeseries,
        jfloatArray forecast, jint svd)
	{
	    jboolean isCopy = JNI_TRUE;

	    jint *jni_pp = (jint *)env->GetPrimitiveArrayCritical(pp, &isCopy);
	    check_memory(env, jni_pp);


	    jfloat *jni_timeseries = (jfloat *)env->GetPrimitiveArrayCritical(timeseries, &isCopy);
	    check_memory(env, jni_timeseries);

	    jfloat *jni_forecast = (jfloat *)env->GetPrimitiveArrayCritical(forecast, &isCopy);
	    check_memory(env, jni_forecast);

	    int k = n - l + 1;
	    int ld = l - 1;

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

	    float *yd = new float[l-1];
	    float *zi = new float[l];

	    for (int j = 0; j < k; ++j){
	        memcpy(x + j*l, jni_timeseries + j, l * sizeof(jfloat));
	    }

        //sgesdd, sgesvd
        if (svd == 0){
            sgesdd('S', l, k, x, l, s, u, l, vt, k, new int[1]);
        }else{
            sgesvd('S', 'S', l, k, x, l, s, u, l, vt, k, new int[1]);
        }

        memset(xi, 0, l*k*sizeof(float));

	    for (int ii=0; ii < p; ++ii){
	        int i = jni_pp[ii];

	        memcpy(ui, u + i*l, l * sizeof(float));

	        for (int j = 0; j < k; ++j){
	            vi[j] = vt[i + j*k];
	        }

	        sgemm('N', 'T', l, k, 1, s[i], ui, l, vi, k, 0, xii, l);

	        for (int j = 0; j < l*k; ++j){
	            xi[j] += xii[j];
	        }
	    }

	    float v2 = 0;

	    for (int i=0; i < m; ++i){
	        memcpy(vd + i*ld, u + i*l, ld * sizeof(float));
	        pi[i] = u[l-1 + i*l];
	        v2 += pow(pi[i], 2);
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

        sgemm('N', 'T', ld, ld, m, 1, vd, ld, vd, ld, 0, vdxvdt, ld);
        sgemm('N', 'T', ld, ld, 1, 1-v2, r, ld, r, ld, 0, rxrt, ld);

        for (int i = 0; i < ld * ld; ++i){
            pr[i] = vdxvdt[i] + rxrt[i];
        }

        memcpy(z, xi, l*k*sizeof(float));

        for (int i = k; i < n + m; ++i){
            memcpy(yd, z + 1 + (i-1)*l, ld * sizeof(float));

            sgemm('N', 'N', ld, 1, ld, 1, pr, ld, yd, ld, 0, zi, ld);

            zi[l-1] = 0;
            for (int j = 0; j < ld; ++j){
                zi[l-1] += r[j] * yd[j];
            }

            memcpy(z + i*l, zi, l * sizeof(float));
        }

        diagonalAveraging(z, l, n + m, jni_forecast);

        env->ReleasePrimitiveArrayCritical(pp, jni_pp, JNI_ABORT);
        env->ReleasePrimitiveArrayCritical(timeseries, jni_timeseries, JNI_ABORT);
        env->ReleasePrimitiveArrayCritical(forecast, jni_forecast, 0);

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
	}

void diagonalAveraging(float *y, int rows, int cols, float *g){
        int l1 = std::min(rows, cols);
        int k1 = std::max(rows, cols);

        int n = l1 + k1 - 1;

        for (int k = 0; k < l1 - 1; ++k){
            g[k] = getSum(y, rows, cols, 1, k + 1, k) / (k + 1);
        }

        for (int k = l1 - 1; k < k1; ++k){
            g[k] = getSum(y, rows, cols, 1, l1, k) / l1;
        }

        for (int k = k1; k < n; ++k){
            g[k] = getSum(y, rows, cols, k - k1 + 2, n - k1 + 1, k) / (n - k);
        }
    }

float getSum(float *y, int rows, int cols, int first, int last, int k){
        float sum = 0;

        for (int m = first; m <= last; ++m){
            sum += rows < cols ? y[m - 1 + (k - m + 1)*rows] : y[k - m + 1 + (m - 1)*rows];
        }

        return sum;
    }


