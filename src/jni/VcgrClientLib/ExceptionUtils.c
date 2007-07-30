#include "ExceptionUtils.h"

void throwSimpleException(JNIEnv *env, char *exceptionClassName,
	char *message)
{
	jclass cls = (*env)->FindClass(env, exceptionClassName);

	/* if cls is NULL, an exception has already been thrown */
	if (cls != NULL)
	{
		(*env)->ThrowNew(env, cls, message);

		/* Free local ref */
		(*env)->DeleteLocalRef(env, cls);
	}
}
