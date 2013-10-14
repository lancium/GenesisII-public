#ifndef __STRING_UTILS_H__
#define __STRING_UTILS_H__

#include <stdarg.h>

char* duplicateString(const char *original);
char* formatString(const char *format, ...);
char* vformatString(const char *format, va_list args);

#endif