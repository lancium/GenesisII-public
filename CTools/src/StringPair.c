#include <stdlib.h>

#include "StringUtils.h"
#include "StringPair.h"

typedef struct StringPairImpl
{
	StringPairType pairType;

	char *_first;
	char *_second;
} StringPairImpl;

static void destroyStringPair(StringPair);
static StringPair copyStringPair(StringPair);
static const char* getFirstFromStringPair(StringPair);
static const char* getSecondFromStringPair(StringPair);

StringPair createStringPair(const char *first, const char *second)
{
	StringPairImpl *impl = (StringPairImpl*)malloc(sizeof(StringPairImpl));
	impl->pairType.destroy = destroyStringPair;
	impl->pairType.copy = copyStringPair;
	impl->pairType.getFirst = getFirstFromStringPair;
	impl->pairType.getSecond = getSecondFromStringPair;
	impl->_first = duplicateString(first);
	impl->_second = duplicateString(second);

	return (StringPair)impl;
}

void destroyStringPair(StringPair pair)
{
	StringPairImpl *impl = (StringPairImpl*)pair;

	if (impl->_first != NULL)
		free(impl->_first);
	if (impl->_second != NULL)
		free(impl->_second);

	free(impl);
}

StringPair copyStringPair(StringPair pair)
{
	StringPairImpl *impl = (StringPairImpl*)pair;

	return createStringPair(impl->_first, impl->_second);
}

const char* getFirstFromStringPair(StringPair pair)
{
	StringPairImpl *impl = (StringPairImpl*)pair;
	return impl->_first;
}

const char* getSecondFromStringPair(StringPair pair)
{
	StringPairImpl *impl = (StringPairImpl*)pair;
	return impl->_second;
}
