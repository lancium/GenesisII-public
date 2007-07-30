#ifndef __UTILITY_H__
#define __UTILITY_H__

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif	/* C Plus Plus */

extern void print_listing(char** listing, int size);

extern int convert_listing(JNIEnv *env, char *** clisting, jarray listing);

extern char* convert_jstring(JNIEnv *env, jstring the_string);

extern int convert_jstring_using_data(JNIEnv *env, jstring the_string, char * data);

#ifdef __cplusplus
}
#endif	/* C PLUS PLUS */

#endif