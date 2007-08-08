#ifndef __STRING_MANIPULATION_H__
#define __STRING_MANIPULATION_H__

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif	/* C Plus Plus */

extern jstring NewPlatformString(JNIEnv *env, char *s, int length);

extern jobjectArray NewPlatformStringArray(JNIEnv *env, char **strv, int strc);

#ifdef __cplusplus
}
#endif	/* C PLUS PLUS */

#endif
