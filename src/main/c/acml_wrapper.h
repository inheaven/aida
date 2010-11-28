#include <jni.h>

void check_memory(JNIEnv *, void *);

#ifndef _Included_ru_inhell_aida_acml_ACML
#define _Included_ru_inhell_aida_acml_ACML
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     ru_inhell_aida_acml_ACML
 * Method:    dgesvd
 * Signature: (Ljava/lang/String;Ljava/lang/String;II[DI[D[DI[DI[I)V
 */
JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_dgesvd
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jdoubleArray, jint, jdoubleArray, jdoubleArray, jint, jdoubleArray, jint, jintArray);

/*
 * Class:     ru_inhell_aida_acml_ACML
 * Method:    dgemm
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIID[DI[DID[DI)V
 */
JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_dgemm
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jint, jdouble, jdoubleArray, jint, jdoubleArray, jint, jdouble, jdoubleArray, jint);

/*
 * Class:     ru_inhell_aida_acml_ACML
 * Method:    sgesvd
 * Signature: (Ljava/lang/String;Ljava/lang/String;II[FI[F[FI[FI[I)V
 */
JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_sgesvd
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jfloatArray, jint, jfloatArray, jfloatArray, jint, jfloatArray, jint, jintArray);

/*
 * Class:     ru_inhell_aida_acml_ACML
 * Method:    sgemm
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIIF[FI[FIF[FI)V
 */
JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_sgemm
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jint, jfloat, jfloatArray, jint, jfloatArray, jint, jfloat, jfloatArray, jint);

/*
 * Class:     ru_inhell_aida_acml_ACML
 * Method:    test
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_ru_inhell_aida_acml_ACML_test
  (JNIEnv *, jobject, jstring);

#ifdef __cplusplus
}
#endif
#endif

