#include "StringUtils.h"

#include <stdarg.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#define DEFAULT_BUFFER_LENGTH 128

char* duplicateString(const char *original)
{
	char *ret = NULL;
	size_t length;

	if (original)
	{
		length = strlen(original);
		ret = (char*)malloc(sizeof(char) * (length + 1));
		memcpy(ret, original, length + 1);
	}

	return ret;
}

char* formatString(const char *format, ...)
{
	char *ret;
	va_list args;

	va_start(args, format);
	ret = vformatString(format, args);
	va_end(args);

	return ret;
}

char* vformatString(const char *format, va_list args)
{
	char *buffer = NULL;
	int bufferLength = DEFAULT_BUFFER_LENGTH;

	while (1)
	{
		if (buffer)
			free(buffer);

		buffer = (char*)malloc(sizeof(char) * bufferLength);
		if (vsnprintf(buffer, bufferLength, format, args) >= 0)
			break;

		bufferLength *= 2;
	}

	return buffer;
}