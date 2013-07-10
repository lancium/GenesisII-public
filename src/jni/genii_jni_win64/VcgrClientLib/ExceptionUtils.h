#ifndef __EXCEPTION_UTILS_H__
#define __EXCEPTION_UTILS_H__

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif	/* C Plus Plus */

void throwSimpleException(JNIEnv *env, char *exceptionClassName,
	char *message);

#ifdef __cplusplus
}
#endif	/* C PLUS PLUS */

#endif
