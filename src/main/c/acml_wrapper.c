#include "acml_wrapper.h"

inline void check_memory(JNIEnv * env, void * arg) {
	if (arg != NULL) {
		return;
	}
	/*
	 * WARNING: Memory leak
	 *
	 * This doesn't clean up successful allocations prior to throwing this exception.
	 * However, it's a pretty dire situation to be anyway and the client code is not
	 * expected to recover.
	 */
	(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"),
		"Out of memory transferring array to native code in F2J JNI");
}

JNIEXPORT void JNICALL
Java_ru_inhell_aida_acml_ACML_dgesvd (JNIEnv *env, jobject calling_obj, jstring jobu, jstring jobvt, jint m, jint n,
                                      jdoubleArray a, jint lda, jdoubleArray s, jdoubleArray u, jint ldu, jdoubleArray vt,
                                      jint ldvt, jintArray info)
{
	char *jni_jobu = (char *)(*env)->GetStringUTFChars(env, jobu, JNI_FALSE);
	char *jni_jobvt = (char *)(*env)->GetStringUTFChars(env, jobvt, JNI_FALSE);

	jdouble *jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
	check_memory(env, jni_a);

	jdouble *jni_s = (*env)->GetPrimitiveArrayCritical(env, s, JNI_FALSE);
	check_memory(env, jni_s);

	jdouble *jni_u = (*env)->GetPrimitiveArrayCritical(env, u, JNI_FALSE);
	check_memory(env, jni_u);

	jdouble *jni_vt = (*env)->GetPrimitiveArrayCritical(env, vt, JNI_FALSE);
	check_memory(env, jni_vt);

    jint *jni_info = (*env)->GetPrimitiveArrayCritical(env, info, JNI_FALSE);
	check_memory(env, jni_info);

	dgesvd(jni_jobu[0], jni_jobvt[0], &m, &n, jni_a, &lda, jni_s, jni_u, &ldu, jni_vt, &ldvt, &jni_info[0]);

	(*env)->ReleaseStringUTFChars(env, jobu, jni_jobu);
	(*env)->ReleaseStringUTFChars(env, jobvt, jni_jobvt);
	(*env)->ReleasePrimitiveArrayCritical(env, a, jni_a, 0);
	(*env)->ReleasePrimitiveArrayCritical(env, s, jni_s, 0);
	(*env)->ReleasePrimitiveArrayCritical(env, u, jni_u, 0);
	(*env)->ReleasePrimitiveArrayCritical(env, vt, jni_vt, 0);
	(*env)->ReleasePrimitiveArrayCritical(env, info, jni_info, 0);
}

JNIEXPORT void JNICALL
Java_ru_inhell_aida_acml_ACML_dgemm (JNIEnv *env, jobject calling_obj, jstring transa, jstring transb, jint m, jint n,
                                     jint k, jdouble alpha, jdoubleArray a, jint lda, jdoubleArray b, jint ldb,
                                     jdouble beta, jdoubleArray c, jint ldc)
{
    char *jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
    char *jni_transb = (char *)(*env)->GetStringUTFChars(env, transb, JNI_FALSE);

    jdouble *jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
	check_memory(env, jni_a);

	jdouble *jni_b = (*env)->GetPrimitiveArrayCritical(env, b, JNI_FALSE);
	check_memory(env, jni_b);

	jdouble *jni_c = (*env)->GetPrimitiveArrayCritical(env, c, JNI_FALSE);
	check_memory(env, jni_c);

	dgemm(jni_transa[0], jni_transb[0], &m, &n, &k, &alpha, jni_a, &lda, jni_b, &ldb, &beta, jni_c, &ldc);

	(*env)->ReleaseStringUTFChars(env, transa, jni_transa);
	(*env)->ReleaseStringUTFChars(env, transb, jni_transb);
	(*env)->ReleasePrimitiveArrayCritical(env, a, jni_a, 0);
	(*env)->ReleasePrimitiveArrayCritical(env, b, jni_b, 0);
	(*env)->ReleasePrimitiveArrayCritical(env, c, jni_c, 0);
}

