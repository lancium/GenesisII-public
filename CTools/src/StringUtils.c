#include <string.h>
#include <stdlib.h>

#include "StringUtils.h"

char* duplicateString(const char *original)
{
	char *ret;

	if (original == NULL)
		return NULL;

	ret = (char*)malloc(strlen(original) + 1);
	strcpy(ret, original);

	return ret;
}
