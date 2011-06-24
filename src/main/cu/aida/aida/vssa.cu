#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <cuda_runtime.h>
#include <cublas_v2.h>
#include <culapackdevice.h>

void checkStatus(culaStatus status)
{
    char buf[80];

    if(!status)
        return;

    culaGetErrorInfoString(status, culaGetErrorInfo(), buf, sizeof(buf));
    printf("%s\n", buf);

    //culaShutdown();
    //exit(EXIT_FAILURE);
}


void checkCudaError(cudaError_t err)
{
    if(!err)
        return;

    printf("%s\n", cudaGetErrorString(err));

    //culaShutdown();
    //exit(EXIT_FAILURE);
}

void checkCublasStatus(cublasStatus_t stat)
{
	if (stat != CUBLAS_STATUS_SUCCESS ) {
		printf ( "CUBLAS Error \n" );

		//culaShutdown();
		//exit(EXIT_FAILURE);
	}	    
}

#define TILE_DIM    16
#define BLOCK_ROWS  16

// -------------------------------------------------------
// Copies
// width and height must be integral multiples of TILE_DIM
// -------------------------------------------------------

__global__ void transpose(float *odata, float* idata, int width, int height)
{
	int xIndex = blockDim.x * blockIdx.x + threadIdx.x;
	int yIndex = blockDim.y * blockIdx.y + threadIdx.y;
  
	int index  = xIndex + width*yIndex;
  
	for (int i=0; i<TILE_DIM; i+=16) {
		odata[index+i*width] = idata[index+i*width];
	}  
}

__global__ void vector_add(const float* A, const float* B, float* C, int N)
{
    int i = blockDim.x * blockIdx.x + threadIdx.x;

    if (i < N)
        C[i] = A[i] + B[i];
}

__global__ void calc_r(float* pi, float* u, float* r, float* vd, float v2, int l, int m){
	int i = blockDim.x * blockIdx.x + threadIdx.x;
	int j = blockDim.y * blockIdx.y + threadIdx.y;

	int ld = l-1;

	if (i < m){
		pi[i] = u[ld + i*l];
	    v2 += powf(pi[i], 2);
	}

	if (j < ld){
		r[j] += vd[j + i*ld] * pi[i];
	}
}


void vssa(int n, int l, int p, int* pp, int m, float* timeseries, float* forecast, int count){
	cudaError_t err;
    culaStatus status;
	cublasStatus_t stat ;
	cublasHandle_t handle ;

	int f_size = n + m + l - 1; 
	int k = n - l + 1;
	int ld = l - 1;
	
	//Init and copy timeseries to device
	float* d_timeseries;

	err = cudaMalloc(&d_timeseries, (n+count)*sizeof(float));
	checkCudaError(err);

	err = cudaMemcpy(d_timeseries, timeseries, (n+count)*sizeof(float), cudaMemcpyHostToDevice);
	checkCudaError(err);

	//Init and copy forecast 
	float* d_forecast;
	
	err = cudaMalloc(&d_forecast, f_size*count*sizeof(float));
	checkCudaError(err);

	//Init Cublas
	stat = cublasCreate(&handle);
	checkCublasStatus(stat);

	//Init CULA
	status = culaInitialize();
    checkStatus(status);
	
	//Init local variable
	float* x;
	err = cudaMalloc(&x, l*k*sizeof(float));
	checkCudaError(err);

	float *s;
	err = cudaMalloc(&s, l*sizeof(float));
	checkCudaError(err);

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
	err = cudaMalloc(&xii, l*sizeof(float));
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

	float *vdxvdt;
	err = cudaMalloc(&vdxvdt, ld*ld*sizeof(float));
	checkCudaError(err);

	float *rxrt;
	err = cudaMalloc(&rxrt, ld*ld*sizeof(float));
	checkCudaError(err);
	
	float *pr;
	err = cudaMalloc(&pr, ld*ld*sizeof(float));
	checkCudaError(err);

	//Execute vssa for count points
	for (int index = 0; index < count; ++index){
		//Populate trajectory matrix
		for (int j = 0; j < k; ++j){
			err = cudaMemcpy(x + j*l, d_timeseries + (j + index), l * sizeof(float), cudaMemcpyDeviceToDevice);
			checkCudaError(err);
        }

		//Execute SVD
		status = culaDeviceSgesvd('S', 'S', l, k, x, l, s, u, l, vt, k); //todo S vs A
        checkStatus(status);

		dim3 grid(k / BLOCK_ROWS, k / BLOCK_ROWS, 1);
		dim3 threads(BLOCK_ROWS, BLOCK_ROWS, 1);
		transpose<<<grid, threads>>>(v, vt, k, k);

		//Init Xi
		err = cudaMemset(xi, 0, l*k*sizeof(float));
		checkCudaError(err);

		for (int ii=0; ii < p; ++ii){
            int i = pp[ii];

            err = cudaMemcpy(ui, u + i*l, l * sizeof(float), cudaMemcpyDeviceToDevice);
			checkCudaError(err);
						
			err = cudaMemcpy(vi, vt + i*k, k * sizeof(float), cudaMemcpyDeviceToDevice);
			checkCudaError(err);
			
            stat = cublasSgemm(handle, CUBLAS_OP_N, CUBLAS_OP_T, l, k, 1, &s[i], ui, l, vi, k, 0, xii, l); //todo v vs vt
            checkCublasStatus(stat);

			int threadsPerBlock = 256;
			int blocksPerGrid = (l*k + threadsPerBlock - 1) / threadsPerBlock;
			vector_add<<<blocksPerGrid, threadsPerBlock>>>(xi, xii, xi, l*k);
        }

		//Calculate Pr matrix
		err = cudaMemset(v2, 0, sizeof(float));
		checkCudaError(err);

		err = cudaMemset(r, 0, ld*sizeof(float));
		checkCudaError(err);

		for (int i=0; i < m; ++i){
			err = cudaMemcpy(vd + i*ld, u + i*l, ld * sizeof(float), cudaMemcpyDeviceToDevice);
			checkCudaError(err);
		}

		dim3 grid(m / BLOCK_ROWS, ld / BLOCK_ROWS, 1);
		dim3 threads(BLOCK_ROWS, BLOCK_ROWS, 1);
		calc_r<<<grid, threads>>>(pi, u, r, vd, v2, l, m); //todo

		for (int j=0; j < ld; ++j){
            r[j] /= (1-v2);
        }

        stat = cublasSgemm(handle, CUBLAS_OP_N, CUBLAS_OP_T, ld, ld, m, 1, vd, ld, vd, ld, 0, vdxvdt, ld);
		checkCublasStatus(stat);

        stat = cublasSgemm(handle, CUBLAS_OP_N, CUBLAS_OP_T, ld, ld, 1, 1-v2, r, ld, r, ld, 0, rxrt, ld);
		checkCublasStatus(stat);

		int threadsPerBlock = 256;
		int blocksPerGrid = (l*k + threadsPerBlock - 1) / threadsPerBlock;
		vector_add<<<blocksPerGrid, threadsPerBlock>>>(vdxvdt, rxrt, pr, ld*ld);


		
		















		cublasDestroy(handle);
		culaShutdown();
	}


}

