#include <jni.h>
#include <cula.h>
#include <culablas.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <algorithm>
#include <string.h>
#include <math.h>
#include <time.h>

#include <Windows.h>
#include <Psapi.h>

#ifndef _Included_ru_inhell_aida_cula_CULA
#define _Included_ru_inhell_aida_cula_CULA
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_ru_inhell_aida_cula_CULA_vssa (JNIEnv *, jobject, jint, jint, jint, jintArray, jint, jfloatArray, jfloatArray, jint);

void check_memory(JNIEnv *, void *);
void checkStatus(culaStatus);

void diagonalAveraging(float *, int, int, float *, int);
float getSum(float *, int, int, int, int, int);

double getHighResolutionTime(void);

#ifdef __cplusplus
}
#endif
#endif

