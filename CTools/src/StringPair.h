#ifndef __STRING_PAIR_H__
#define __STRING_PAIR_H__

typedef struct StringPairType *StringPair;
typedef struct StringPairType
{
	void (*destroy)(StringPair);
	StringPair (*copy)(StringPair);

	const char* (*getFirst)(StringPair);
	const char* (*getSecond)(StringPair);
} StringPairType;

StringPair createStringPair(const char *first, const char *second);

#endif
