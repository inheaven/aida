#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include <cuda_runtime.h>
#include <cublas_v2.h>
#include <culapackdevice.h>
#include <culablasdevice.h>

void checkStatus(culaStatus status)
{
    char buf[80];

    if(!status)
        return;

    culaGetErrorInfoString(status, culaGetErrorInfo(), buf, sizeof(buf));
    printf("Cula error: %s\n", buf);

    culaShutdown();
	cudaDeviceReset();
    exit(EXIT_FAILURE);
}


void checkCudaError(cudaError_t err)
{
    if(!err)
        return;

    printf("Cuda error: %s\n", cudaGetErrorString(err));

    culaShutdown();
	cudaDeviceReset();
    exit(EXIT_FAILURE);
}

void checkCublasStatus(cublasStatus_t stat)
{
	if (stat != CUBLAS_STATUS_SUCCESS ) {
		printf ( "CUBLAS Error \n" );

		culaShutdown();
		cudaDeviceReset();
		exit(EXIT_FAILURE);
	}	    
}

#define BLOCK_ROWS  16
#define BLOCK_DIM 16
#define THREADS_SIZE 256


// Функция транспонирования матрицы c использования разделяемой памяти
//
// inputMatrix - указатель на исходную матрицу
// outputMatrix - указатель на матрицу результат
// width - ширина исходной матрицы (она же высота матрицы-результата)
// height - высота исходной матрицы (она же ширина матрицы-результата)
//
__global__ void transposeMatrixFast(float* inputMatrix, float* outputMatrix, int width, int height)
{
    __shared__ float temp[BLOCK_DIM][BLOCK_DIM];

    int xIndex = blockIdx.x * blockDim.x + threadIdx.x;
    int yIndex = blockIdx.y * blockDim.y + threadIdx.y;

    if ((xIndex < width) && (yIndex < height))
    {
        // Линейный индекс элемента строки исходной матрицы
        int idx = yIndex * width + xIndex;

        //Копируем элементы исходной матрицы
        temp[threadIdx.y][threadIdx.x] = inputMatrix[idx];
    }

    //Синхронизируем все нити в блоке
    __syncthreads();

    xIndex = blockIdx.y * blockDim.y + threadIdx.x;
    yIndex = blockIdx.x * blockDim.x + threadIdx.y;

    if ((xIndex < height) && (yIndex < width))
    {
        // Линейный индекс элемента строки исходной матрицы
        int idx = yIndex * height + xIndex;

        //Копируем элементы исходной матрицы
         outputMatrix[idx] = temp[threadIdx.x][threadIdx.y];
    }
}

__global__ void vector_add(const float *A, const float *B, float *C, int N)
{
    int i = blockDim.x * blockIdx.x + threadIdx.x;
	
    if (i < N){
        C[i] = A[i] + B[i];		 
    }
}

//pi[i] = u[l-1 + i*l];
__global__ void calc_pi(float *u, float *v2, int l, int m){
	float v2_local = 0;

	if (blockIdx.x == 0 && threadIdx.x == 0){
		for (int i = 0; i < m; i++){			
			v2_local += u[l-1 + i*l] * u[l-1 + i*l];
		}

		v2[0] = v2_local;		
	}
}

__global__ void calc_r(float *vd, float *u, float *r, float *ra, float *v2, int ld, int m){
    int j = blockDim.x * blockIdx.x + threadIdx.x;

	float r_local = 0;

	if (j < ld){
		for (int i = 0; i < m; ++i){        
            r_local += vd[j + i*ld] * u[ld + i*(ld+1)];
        }

		r[j] = r_local;
		ra[j] = r_local/(1-v2[0]);
    }
}

__global__ void calc_zi(float *zi, float *r, float *yd, int ld){
	if (blockIdx.x == 0 && threadIdx.x == 0){
		float zi_local = 0;

		for (int i = 0; i < ld; ++i){
			zi_local += r[i] * yd[i];
		}

		zi[ld] = zi_local;
	}
}

__device__ float getSum(float *y, int rows, int cols, int first, int last, int k){
    float sum = 0;

    for (int m = first; m <= last; ++m){
        sum += rows < cols ? y[m - 1 + (k - m + 1)*rows] : y[k - m + 1 + (m - 1)*rows];
    }

    return sum;
}

__global__ void diagonalAveraging(float *y, int rows, int cols, float *g, int begin){
    int i = blockDim.x * blockIdx.x + threadIdx.x;

    int l1 = rows;
    int k1 = cols;
    int n = l1 + k1 - 1;

    if (i < l1 - 1){
        g[begin + i] = getSum(y, rows, cols, 1, i + 1, i) / (i + 1);
    }else if (i >= l1 - 1 && i < k1){
        g[begin + i] = getSum(y, rows, cols, 1, l1, i) / l1;
    }else if (i >= k1 && i < n){
        g[begin + i] = getSum(y, rows, cols, i - k1 + 2, n - k1 + 1, i) / (n - i);
    }
}

void printArray(float *a, int size){
	printf("\n");

	for (int i=0; i < size; ++i){
		printf("%10.4f",  a[i]);
		
		if ((i+1)%16==0) printf("\n");
	}

	printf("\n");
}

void d_printArray(float *d_a, int size){
	float *a = (float *)malloc(size * sizeof(float));

	cudaMemcpy(a, d_a, size * sizeof(float), cudaMemcpyDeviceToHost);

	printArray(a, size);
}

extern "C" __declspec(dllexport) void vssa(int n, int l, int p, int* pp, int m, float* timeseries, float* forecast, int count){
	cudaError_t err;
    culaStatus status;
	cublasStatus_t stat ;
	cublasHandle_t handle ;
		
	//Init Cublas
	stat = cublasCreate(&handle);
	checkCublasStatus(stat);

	int f_size = n + m + l - 1; 
	int k = n - l + 1;
	int ld = l - 1;
	
	//Init and copy timeseries to device
	float* d_timeseries;

	err = cudaMalloc(&d_timeseries, (n+count)*sizeof(float));
	checkCudaError(err);

	err = cudaMemcpyAsync(d_timeseries, timeseries, (n+count-1)*sizeof(float), cudaMemcpyHostToDevice);
	checkCudaError(err);
	
	//Init and copy forecast 
	float* d_forecast;
	
	err = cudaMalloc(&d_forecast, f_size*count*sizeof(float));
	checkCudaError(err);	
	
	//Init local variable
	float* x;
	err = cudaMalloc(&x, l*k*sizeof(float));
	checkCudaError(err);

	float *s;
	err = cudaMalloc(&s, l*sizeof(float));
	checkCudaError(err);

	float *s_h = (float *)malloc(l*sizeof(float));	

	float *u;
	err = cudaMalloc(&u, l*l*sizeof(float));
	checkCudaError(err);

	float *vt;
	err = cudaMalloc(&vt, k*k*sizeof(float));
	checkCudaError(err);

	float *v;
	err = cudaMalloc(&v, k*k*sizeof(float));
	checkCudaError(err);

	float *xi;
	err = cudaMalloc(&xi, l*k*sizeof(float));
	checkCudaError(err);

	float *ui;
	err = cudaMalloc(&ui, l*sizeof(float));
	checkCudaError(err);

	float *vi;
	err = cudaMalloc(&vi, k*sizeof(float));
	checkCudaError(err);

	float *xii;
	err = cudaMalloc(&xii, l*k*sizeof(float));
	checkCudaError(err);

	float *pi;
	err = cudaMalloc(&pi, m*sizeof(float));
	checkCudaError(err);

	float *vd;
	err = cudaMalloc(&vd, ld*m*sizeof(float));
	checkCudaError(err);

	float *v2;
	err = cudaMalloc(&v2, sizeof(float));
	checkCudaError(err);

	float *r;
	err = cudaMalloc(&r, ld*sizeof(float));
	checkCudaError(err);

	float *ra;
	err = cudaMalloc(&ra, ld*sizeof(float));
	checkCudaError(err);

	float *vdxvdt;
	err = cudaMalloc(&vdxvdt, ld*ld*sizeof(float));
	checkCudaError(err);

	float *rxrt;
	err = cudaMalloc(&rxrt, ld*ld*sizeof(float));
	checkCudaError(err);
	
	float *pr;
	err = cudaMalloc(&pr, ld*ld*sizeof(float));
	checkCudaError(err);

	float *z;
	err = cudaMalloc(&z, l*(n+m)*sizeof(float));
	checkCudaError(err);

	float *zi;
	err = cudaMalloc(&zi, l*sizeof(float));
	checkCudaError(err);

	float *yd;
	err = cudaMalloc(&yd, ld*sizeof(float));
	checkCudaError(err);
	
	int threads_x = THREADS_SIZE;
		
	int grid_m = (m + threads_x - 1)/threads_x;
	int grid_ld = (ld + threads_x - 1)/threads_x;
	int grid_ld2 = (ld*ld + threads_x - 1)/threads_x;
	int grid_l_k = (l*k + threads_x - 1)/threads_x;
	int grid_f_size = (f_size + threads_x - 1)/threads_x;

	dim3 grid_k_k((k + BLOCK_ROWS - 1) / BLOCK_ROWS, (k + BLOCK_ROWS - 1) / BLOCK_ROWS, 1);		
	dim3 grid_m_ld((m + BLOCK_ROWS - 1)/BLOCK_ROWS, (ld + BLOCK_ROWS - 1)/BLOCK_ROWS);
    dim3 threads_x_y(BLOCK_ROWS, BLOCK_ROWS);

	//Execute vssa for count points
	for (int index = 0; index < count; ++index){
		//Populate trajectory matrix
		for (int j = 0; j < k; ++j){
			err = cudaMemcpyAsync(x + j*l, d_timeseries + (j + index), l * sizeof(float), cudaMemcpyDeviceToDevice);
			//checkCudaError(err);
        }	
				
		//Execute SVD
		status = culaInitialize();
		checkStatus(status);
		status = culaDeviceSgesvd('S', 'S', l, k, x, l, s, u, l, vt, k); //todo S vs A
        checkStatus(status);
		culaShutdown();

		//copy s to host
		err = cudaMemcpyAsync(s_h, s, l * sizeof(float), cudaMemcpyDeviceToHost);
		//checkStatus(status);		
						
		transposeMatrixFast<<<grid_k_k, threads_x_y>>>(vt, v, k, k);
		//cudaDeviceSynchronize();
				
		//Init Xi
		err = cudaMemsetAsync(xi, 0, l*k*sizeof(float));
		//checkCudaError(err);
				
		//Alpha and beta const
		const float alpha_one = 1.0f;
		const float beta_zero = 0.0f;	
		
		for (int ii=0; ii < p; ++ii){
            int i = pp[ii];

            err = cudaMemcpyAsync(ui, u + i*l, l * sizeof(float), cudaMemcpyDeviceToDevice);
			//checkCudaError(err);
						
			err = cudaMemcpyAsync(vi, v + i*k, k * sizeof(float), cudaMemcpyDeviceToDevice);
			//checkCudaError(err);
															
            stat = cublasSgemm(handle, CUBLAS_OP_N, CUBLAS_OP_T, l, k, 1, &s_h[i], ui, l, vi, k, &beta_zero, xii, l); //todo v vs vt			
            checkCublasStatus(stat);			
			
			vector_add<<<grid_l_k, threads_x>>>(xi, xii, xi, l*k);
			//cudaDeviceSynchronize();			
        }		

		//Calculate Pr matrix
		for (int i=0; i < m; ++i){
			err = cudaMemcpyAsync(vd + i*ld, u + i*l, ld * sizeof(float), cudaMemcpyDeviceToDevice);
			//checkCudaError(err);
		}
				
		calc_pi<<<grid_m, threads_x>>>(u, v2, l, m);
		//cudaDeviceSynchronize();
				
		calc_r<<<grid_ld, threads_x>>>(vd, u, r, ra, v2, ld, m);
		//cudaDeviceSynchronize();
						
        stat = cublasSgemm(handle, CUBLAS_OP_N, CUBLAS_OP_T, ld, ld, m, &alpha_one, vd, ld, vd, ld, &beta_zero, vdxvdt, ld);
		checkCublasStatus(stat);

        stat = cublasSgemm(handle, CUBLAS_OP_N, CUBLAS_OP_T, ld, ld, 1, &alpha_one, ra, ld, r, ld, &beta_zero, rxrt, ld);
		checkCublasStatus(stat);

		vector_add<<<grid_ld2, threads_x>>>(vdxvdt, rxrt, pr, ld*ld);
		//cudaDeviceSynchronize();
		
		//Calculate Z
		err = cudaMemcpyAsync(z, xi, l*k*sizeof(float), cudaMemcpyDeviceToDevice);
		//checkCudaError(err);
								
		for (int i = k; i < n + m; ++i){
		    err = cudaMemcpyAsync(yd, z + (1 + (i-1)*l), ld * sizeof(float), cudaMemcpyDeviceToDevice);
		    //checkCudaError(err);
			
		    stat = cublasSgemm(handle, CUBLAS_OP_N, CUBLAS_OP_N, ld, 1, ld, &alpha_one, pr, ld, yd, ld, &beta_zero, zi, ld);
		    //checkCublasStatus(stat);						
			
		    calc_zi<<<1, 1>>>(zi, ra, yd, ld);
			//cudaDeviceSynchronize();
						
		    err = cudaMemcpyAsync(z + i*l, zi, l * sizeof(float), cudaMemcpyDeviceToDevice);
		    //checkCudaError(err);
		}
		
		diagonalAveraging<<<grid_f_size, threads_x>>>(z, l, n + m, d_forecast, f_size*index);				
	}   

	cudaDeviceSynchronize();

	err = cudaMemcpy(forecast, d_forecast, count*f_size*sizeof(float), cudaMemcpyDeviceToHost);
	checkCudaError(err);

	free(s_h);

	cublasDestroy(handle);	
	
    cudaFree(d_timeseries);
    cudaFree(d_forecast);
    cudaFree(x);
    cudaFree(s);
    cudaFree(u);
    cudaFree(vt);
    cudaFree(v);
    cudaFree(xi);
    cudaFree(ui);
    cudaFree(vi);
    cudaFree(xii);
    cudaFree(pi);
    cudaFree(vd);
    cudaFree(v2);
    cudaFree(r);
    cudaFree(ra);
    cudaFree(vdxvdt);
    cudaFree(rxrt);
    cudaFree(pr);
    cudaFree(z);
    cudaFree(zi);
    cudaFree(yd);	
}

#define tn 4000
#define tl 2000
#define tp 16
#define tm 32
#define tc 2

int main(int argc, char** argv){
	printf("start\n");	

	float *ts = new float[tn+tc];

	for (int i = 0; i < tn+tc; ++i){
		ts[i] = i;
	}

	float f[(tn+tl+tm-1)*tc];

	int *pp = new int[tp];
	for (int i = 0; i < tp; ++i){
		pp[i] = i;
	}
	
	time_t t1 = time (NULL);

	vssa(tn, tl, tp, pp, tm, ts, f, tc);

	time_t t2 = time (NULL);

	printf ("time: %ld \n", (t2-t1));		

	printf("finish!");

	return 0;
}



