#include "JavaUtils.h"

#include <jni.h>

static jclass runtimeExceptionClass = 0;
static jclass ioExceptionClass = 0;

jclass getCachedJavaClass(JNIEnv *env, jclass *cacheVariable, const char *className)
{
	jclass tmp;

	if (!(*cacheVariable))
	{
		tmp = (*env)->FindClass(env, className);
		if (!tmp)
			throwNewRuntimeException(env, "Unable to locate java class \"%s\".", 
				className);
		else
		{
			*cacheVariable = (*env)->NewGlobalRef(env, tmp);
			if (!(*cacheVariable))
				throwNewRuntimeException(env, 
					"Unable to create global reference for class \"%s\".", className);
		}
	}

	return *cacheVariable;
}

jmethodID getCachedJavaMethod(JNIEnv *env, jclass cl, jmethodID *cacheVariable,
	const char *methodName, const char *methodSig)
{
	jmethodID tmp;

	if (!(*cacheVariable))
	{
		tmp = (*env)->GetMethodID(env, cl, methodName, methodSig);
		if (!tmp)
			throwNewRuntimeException(env, "Unable to find method \"%s\".", methodName);
		else 
		{
			*cacheVariable = (jmethodID)((*env)->NewGlobalRef(env, (jobject)tmp));
			if (!(*cacheVariable))
				throwNewRuntimeException(env, 
					"Unable to create global ref to method \"%s\".", methodName);
		}
	}

	return *cacheVariable;
}

jfieldID getJavaField(JNIEnv *env, jclass cl,
	const char *fieldName, const char *fieldSig)
{
	jfieldID ret;

	ret = (*env)->GetFieldID(env, cl, fieldName, fieldSig);
	if (!ret)
		throwNewRuntimeException(env, "Unable to find field \"%s\".", fieldName);

	return ret;
}

void throwNewException(JNIEnv *env, 
	const char *exceptionClass, jclass *cachedExceptionClass, 
	const char *format, ...)
{
	va_list args;

	va_start(args, format);
	vthrowNewException(env, exceptionClass, cachedExceptionClass, format, args);
	va_end(args);
}

void vthrowNewException(JNIEnv *env,
	const char *exceptionClassName, jclass *cachedExceptionClass,
	const char *format, va_list args)
{
	jclass exceptionClass;
	char *msg;

	msg = vformatString(format, args);

	exceptionClass = getCachedJavaClass(env, cachedExceptionClass, exceptionClassName);
	if (exceptionClass)
		(*env)->ThrowNew(env, exceptionClass, msg);
	else
		throwNewRuntimeException(env, "Unable to throw exception.");

	free(msg);
}

void throwNewRuntimeException(JNIEnv *env, const char *format, ...)
{
	va_list args;

	va_start(args, format);
	vthrowNewException(env, "java/lang/RuntimeException", 
		&runtimeExceptionClass, format, args);
	va_end(args);
}

void throwNewIOException(JNIEnv *env, const char *format, ...)
{
	va_list args;

	va_start(args, format);
	vthrowNewException(env, "java/io/IOException", 
		&ioExceptionClass, format, args);
	va_end(args);
}