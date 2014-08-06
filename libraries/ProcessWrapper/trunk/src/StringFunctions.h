#ifndef __STRING_FUNCTIONS_H__
#define __STRING_FUNCTIONS_H__

#include <string.h>

#ifndef PWRAP_windows
	#include <unistd.h>

	#define SSIZE_T ssize_t
#else
	#include <windows.h>

	wchar_t* createWideStringFromMBCopy(const char *);
#endif

char* createStringFromCopy(const char*);
char* createStringFromFormat(const char *format, ...);
SSIZE_T indexOf(const char *string, const char c);
SSIZE_T lastIndexOf(const char *string, const char c);

/* If end == -1, then do it to the end */
char* createStringFromSubstring(const char *original, size_t start,
	SSIZE_T end);

int startsWith(const char *string, const char *query);

typedef struct StringBuilder
{
	void (*append)(struct StringBuilder*, const char *);
	char* (*createString)(struct StringBuilder*);
} StringBuilder;

StringBuilder* createStringBuilder(unsigned int initialSize);

unsigned int defaultStringHashFunction(void*);
int defaultStringEqualsFunction(void*, void*);

#endif
