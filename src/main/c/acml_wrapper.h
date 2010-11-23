#include <jni.h>

void check_memory(JNIEnv *, void *);

JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_dgesvd
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jdoubleArray, jint, jdoubleArray, jdoubleArray, jint, jdoubleArray, jint, jintArray);

JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_dgemm
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jint, jdouble, jdoubleArray, jint, jdoubleArray, jint, jdouble, jdoubleArray, jint);
