#ifndef __PAIR_LIST_H__
#define __PAIR_LIST_H__

#include "StringPair.h"

typedef struct PairListIteratorType *PairListIterator;
typedef struct PairListIteratorType
{
	int (*hasNext)(PairListIterator);
	StringPair (*next)(PairListIterator);

	void (*destroy)(PairListIterator);
} PairListIteratorType;


typedef struct PairListType *PairList;
typedef struct PairListType
{
	PairListIterator (*iterator)(PairList);
	void (*append)(PairList, StringPair);

	void (*destroy)(PairList);
} PairListType;

PairList createPairList();

#endif
