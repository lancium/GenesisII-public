#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

#include "Memory.h"
#include "StringFunctions.h"

#ifdef PWRAP_windows

	wchar_t* createWideStringFromMBCopy(const char *string)
	{
		size_t length;
		wchar_t *ret;
		size_t requiredLength;

		if (string == NULL)
			return NULL;

		length = strlen(string);
		
		requiredLength = mbstowcs(NULL, string, length + 1);
		ret = (wchar_t*)allocate(sizeof(wchar_t) * (requiredLength + 1), NULL);
		mbstowcs(ret, string, length + 1);
		return ret;
	}

#endif

char* createStringFromCopy(const char *string)
{
	size_t length = strlen(string);
	char *ret = (char*)allocate(sizeof(char) * (length + 1), NULL);
	memcpy(ret, string, sizeof(char) * (length + 1));
	return ret;
}

char* createStringFromFormat(const char *format, ...)
{
    char *tmp;
    char *ret;
#ifndef SPBS_macosx
    int written;
    int size;
#endif

    va_list args;

#ifdef SPBS_macosx

    va_start(args, format);
    vasprintf(&tmp, format, args);
    va_end(args);

#else

    size = 128;
    while (1)
    {
        tmp = (char*)malloc(sizeof(char) * size);

        va_start(args, format);
        written = vsnprintf(tmp, size, format, args);
        va_end(args);

        if (written >= size)
        {
            size *= 2;
            free(tmp);
        } else
            break;
    }

#endif

    ret = createStringFromCopy(tmp);
    free(tmp);
    return ret;
}

SSIZE_T indexOf(const char *string, const char c)
{
	size_t index;

	for (index = 0; string[index]; index++)
	{
		if (string[index] == c)
			return index;
	}

	return -1;
}

SSIZE_T lastIndexOf(const char *string, const char c)
{
	SSIZE_T lastMatch = -1;
	size_t index;

	for (index = 0; string[index]; index++)
	{
		if (string[index] == c)
			lastMatch = index;
	}

	return lastMatch;
}

char* createStringFromSubstring(const char *original, size_t start,
	SSIZE_T end)
{
	size_t newLength = end - start;
	char *ret;

	if (end < 0)
		return createStringFromCopy(original + start);

	ret = (char*)allocate(sizeof(char) * (newLength + 1), NULL);
	memcpy(ret, original + start, sizeof(char) * newLength);
	ret[newLength] = (char)0;
	return ret;
}

int startsWith(const char *string, const char *query)
{
	size_t lcv;

	for (lcv = 0; ; lcv++)
	{
		if (!query[lcv])
			return 1;

		if (string[lcv] != query[lcv])
			return 0;
	}

	/* Shouldn't get here */
	return 0;
}

static void stringBuilderDestructor(void *ptr);
static void appendImpl(struct StringBuilder*, const char *);
static char* createStringImpl(struct StringBuilder*);

typedef struct StringBuilderImpl
{
	StringBuilder iface;

	char *_buffer;
	unsigned int _bufferSize;
	unsigned int _next;
} StringBuilderImpl;

StringBuilder* createStringBuilder(unsigned int initialSize)
{
	StringBuilderImpl *ret = (StringBuilderImpl*)allocate(
		sizeof(StringBuilderImpl), stringBuilderDestructor);

	ret->iface.append = appendImpl;
	ret->iface.createString = createStringImpl;

	ret->_bufferSize = initialSize;
	ret->_buffer = (char*)allocate(sizeof(char) * ret->_bufferSize, NULL);
	ret->_next = 0;

	return (StringBuilder*)ret;
}

void stringBuilderDestructor(void *ptr)
{
	StringBuilderImpl *impl = (StringBuilderImpl*)ptr;

	release(impl->_buffer);
}

void appendImpl(struct StringBuilder *ptr, const char *string)
{
	StringBuilderImpl *impl = (StringBuilderImpl*)ptr;
	size_t length = strlen(string);
	char *tmp;

	while ((impl->_next + length) >= impl->_bufferSize)
	{
		tmp = (char*)allocate(sizeof(char) * impl->_bufferSize * 2, NULL);
		memcpy(tmp, impl->_buffer, sizeof(char) * (impl->_next));
		release(impl->_buffer);
		impl->_buffer = tmp;
		impl->_bufferSize *= 2;
	}

	memcpy(impl->_buffer + impl->_next, string, length);
	impl->_next += length;
}

char* createStringImpl(struct StringBuilder *ptr)
{
	StringBuilderImpl *impl = (StringBuilderImpl*)ptr;
	char *ret;

	ret = (char*)allocate(sizeof(char) * (impl->_next + 1), NULL);
	memcpy(ret, impl->_buffer, sizeof(char) * impl->_next);
	ret[impl->_next] = (char)0;

	return ret;
}

unsigned int defaultStringHashFunction(void *ptr)
{
	char *string = (char*)ptr;
	unsigned int ret = 0x0;

	while (*string)
	{
		ret <<= 1;
		ret ^= (int)(*string);
		string++;
	}

	return ret;
}

int defaultStringEqualsFunction(void *ptr1, void *ptr2)
{
	char *string1 = (char*)ptr1;
	char *string2 = (char*)ptr2;

	return strcmp(string1, string2) == 0;
}
