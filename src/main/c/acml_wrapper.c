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