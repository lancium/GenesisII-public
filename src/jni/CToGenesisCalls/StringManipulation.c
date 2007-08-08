#include <stdio.h>
#include <string.h>
#include "StringManipulation.h"

#ifdef _WIN32
#define PATH_SEPARATOR ';'
#else
#define PATH_SEPARATOR ':'
#endif

#define NULL_CHECK0(e) if ((e) == 0) return 0

jstring platformEncoding = NULL;

jstring getPlatformEncoding(JNIEnv *env) {
    if (platformEncoding == NULL) {
        jstring propname = (*env)->NewStringUTF(env, "sun.jnu.encoding");
        if (propname) {
            jclass cls;
            jmethodID mid;
            NULL_CHECK0 (cls = (*env)->FindClass(env, "java/lang/System"));
            NULL_CHECK0 (mid = (*env)->GetStaticMethodID(
		                   env, cls, 
			           "getProperty",
			           "(Ljava/lang/String;)Ljava/lang/String;"));
            platformEncoding = (*env)->CallStaticObjectMethod (
                                    env, cls, mid, propname);
        } 
    }
    return platformEncoding;
}

jboolean isEncodingSupported(JNIEnv *env, jstring enc) {
    jclass cls;
    jmethodID mid;
    NULL_CHECK0 (cls = (*env)->FindClass(env, "java/nio/charset/Charset"));
    NULL_CHECK0 (mid = (*env)->GetStaticMethodID(
	                   env, cls, 
		           "isSupported",
		           "(Ljava/lang/String;)Z"));
    return (jboolean)(*env)->CallStaticObjectMethod (env, cls, mid, enc);
}

/*
 * Returns a new Java string object for the specified platform string.
 */
jstring NewPlatformString(JNIEnv *env, char *s, int length)
{    
	int len = length != -1 ? length : (int)strlen(s);
    jclass cls;
    jmethodID mid;
    jbyteArray ary;
    jstring enc;

    if (s == NULL)
	return 0;
    enc = getPlatformEncoding(env);

    ary = (*env)->NewByteArray(env, len);
    if (ary != 0) {
        jstring str = 0;
	(*env)->SetByteArrayRegion(env, ary, 0, len, (jbyte *)s);
	if (!(*env)->ExceptionOccurred(env)) {
            if (isEncodingSupported(env, enc) == JNI_TRUE) {
                NULL_CHECK0(cls = (*env)->FindClass(env, "java/lang/String"));
                NULL_CHECK0(mid = (*env)->GetMethodID(env, cls, "<init>", 
	   				  "([BLjava/lang/String;)V"));
	        str = (*env)->NewObject(env, cls, mid, ary, enc);
	    } else {
                /*If the encoding specified in sun.jnu.encoding is not 
                  endorsed by "Charset.isSupported" we have to fall back 
                  to use String(byte[]) explicitly here without specifying
                  the encoding name, in which the StringCoding class will 
                  pickup the iso-8859-1 as the fallback converter for us. 
	        */
                NULL_CHECK0(cls = (*env)->FindClass(env, "java/lang/String"));
                NULL_CHECK0(mid = (*env)->GetMethodID(env, cls, "<init>", 
	   				  "([B)V"));
	        str = (*env)->NewObject(env, cls, mid, ary);
            }
	    (*env)->DeleteLocalRef(env, ary);
	    return str;
        }
    } 
    return 0;
}

/*
 * Returns a new array of Java string objects for the specified
 * array of platform strings.
 */
jobjectArray NewPlatformStringArray(JNIEnv *env, char **strv, int strc)
{
    jarray cls;
    jarray ary;
    int i;

    NULL_CHECK0(cls = (*env)->FindClass(env, "java/lang/String"));
    NULL_CHECK0(ary = (*env)->NewObjectArray(env, strc, cls, 0));
    for (i = 0; i < strc; i++) {
	jstring str = NewPlatformString(env, *strv++, -1);
	NULL_CHECK0(str);
	(*env)->SetObjectArrayElement(env, ary, i, str);
	(*env)->DeleteLocalRef(env, str);
    }
    return ary;
}