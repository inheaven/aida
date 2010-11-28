
/* dgemm Example Program Text */
/*
 * ACML-GPU version 1.0 Copyright AMD,NAG 2003
 */

#include <acml.h>
#include <stdio.h>
#include <malloc.h>

void randmat_c(int , double *, int);

int main(void)
{
#define NMAX 2048
#define NUMBER_OF_CALLS 100

  const int lda=NMAX, ldb=NMAX, ldc=NMAX;
      int     i,j, m, k, n;
      double  *a,*b,*c,*d, alpha, beta;
	  float   t1,t2,tx,flops;

  m = NMAX;
  n = NMAX;
  k = NMAX;

  alpha = 1.2;
  beta = .7;

  a = (double *)malloc((m+1)*(k+1)*sizeof(double));
  b = (double *)malloc((k+1)*(n+1)*sizeof(double));
  c = (double *)malloc((m+1)*(n+1)*sizeof(double));
  d = (double *)malloc((m+1)*(n+1)*sizeof(double));

  /* These macros allow access to 1-d arrays as though
     they are 2-d arrays stored in column-major order,
     as required by ACML C routines. */
#define A(I,J) a[((J)-1)*lda+(I)-1]
#define B(I,J) b[((J)-1)*ldb+(I)-1]
#define C(I,J) c[((J)-1)*ldc+(I)-1]
#define D(I,J) d[((J)-1)*ldc+(I)-1]


  printf("ACML-GPU example: DGEMM call\n");
  printf("--------------------------------------------------------------\n");
  printf("\n");

  /* Initialize matrix A  - random */
  randmat_c(m*k,a,1);

  /* Initialize matrix B - identity */
  for (i = 1; i <= k; i++)
    {
      for (j = 1; j <= n; j++) B(i,j) = 0.0;
	  B(i,i) = 1.0;
    }

  /* Initialize matrix C - zeroes */
  for (i = 1; i <= k; i++)
      for (j = 1; j <= n; j++) D(i,j) = C(i,j) = 0.0;

  printf("Matrix A (%d x %d):\n", m, k);
  for (i = 1; i <= 4; i++)
    {
      for (j = 1; j <= 4; j++)
        printf("%8.4f ", A(i,j));
      printf("\n");
    }

  printf("\n");
  printf("Matrix B (%d x %d):\n", k, n);
  for (i = 1; i <= 4; i++)
    {
      for (j = 1; j <= 4; j++)
        printf("%8.4f ", B(i,j));
      printf("\n");
    }
 
  /*  The ACML-GPU library performs initialization on the first call,
      So we exclude the first call from the benchmark.
  */

  acmlsetnumthreads(3);

  /* Perform first multiply */
  dgemm('N', 'N', m, n, k, alpha, a, m, b, k, beta, c, m);

  t1 = second();
  for (i = 2; i <= NUMBER_OF_CALLS; ++i) {
	  /* Perform 2nd, 3rd, etc. multiply */
	  dgemm('N', 'N', m, n, k, alpha, a, m, b, k, beta, c, m);
  }
  t2 = second();
  tx=t2-t1;

  printf("\n");
  printf("Matrix C:\n");
  for (i = 1; i <= 4; i++)
    {
      for (j = 1; j <= 4; j++)
        printf("%8.4f ", C(i,j));
      printf("\n");
    }

  flops = 2.0f * (float)m * (float)n * (float)k * (float)(NUMBER_OF_CALLS-1);
  printf("\nTime: %d calls in %6.2f seconds, %10.0f MFlops\n",
	(int) (NUMBER_OF_CALLS-1), (double) (tx), (double) (1.0e-6*flops/tx));
  return 0;
}

/* initialize an array x with n random numbers */

void randmat_c(int n, double *x, int start)
{
#define MSTATE 20
#define MSEED 10
  int genid, info, lseed, lstate, subid;
  int seed[MSEED];
  static int state[MSTATE];
  double a,b;

  /* Use the NAG basic generator as the base generator */
  genid = 1;
  subid = 1;

  /* Populate the seed array, basic generator needs one seed, and a
     STATE array of length 16 */
  lstate = 16;
  lseed = 1;
  seed[0] = 122421;

  /* Initialize the base generator */
  if (start) drandinitialize(genid,subid,seed,&lseed,state,&lstate,&info);

  /* Generate a sequence from a uniform U(1,2) distribution */
  a = 1.0;
  b = 2.0;
  dranduniform(n,a,b,state,x,&info);
}