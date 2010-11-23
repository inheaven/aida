#include <jni.h>

#ifndef _Included_ru_inhell_aida_acml_ACML
#define _Included_ru_inhell_aida_acml_ACML
#ifdef __cplusplus
extern "C" {
#endif

void check_memory(JNIEnv *, void *);

JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_dgesvd 
	(JNIEnv *, jobject, jstring, jstring, jint, jint, jdoubleArray, jint, jdoubleArray, jdoubleArray, jint, jdoubleArray, jint, jintArray);

JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_dgemm
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jint, jdouble, jdoubleArray, jint, jdoubleArray, jint, jdouble, jdoubleArray, jint);

#ifdef __cplusplus
}
#endif
#endif
