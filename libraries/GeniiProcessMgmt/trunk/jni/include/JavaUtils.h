#ifndef __JAVA_UTILS_H__
#define __JAVA_UTILS_H__

#include "StringUtils.h"

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>

jclass getCachedJavaClass(JNIEnv *env, jclass *cacheVariable, const char *className);
jmethodID getCachedJavaMethod(JNIEnv *env, jclass cl, jmethodID *cacheVariable,
	const char *methodName, const char *methodSig);
jfieldID getJavaField(JNIEnv *env, jclass cl, const char *fieldName, 
	const char *fieldSig);

void throwNewException(JNIEnv *env, 
	const char *exceptionClass, jclass *cachedExceptionClass, 
	const char *format, ...);
void vthrowNewException(JNIEnv *env,
	const char *exceptionClass, jclass *cachedExceptionClass,
	const char *format, va_list args);

void throwNewRuntimeException(JNIEnv *env, const char *format, ...);
void throwNewIOException(JNIEnv *env, const char *format, ...);

#endif