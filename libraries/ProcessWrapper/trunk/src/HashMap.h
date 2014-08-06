#ifndef __HASH_MAP_H__
#define __HASH_MAP_H__

#include "Common.h"
#include "LinkedList.h"

typedef struct HashMap
{
	void (*put)(struct HashMap*, void *key, void *data);
	void* (*get)(struct HashMap*, void *key);

	unsigned int (*length)(struct HashMap*);

	LinkedList* (*createKeyList)(struct HashMap*);
} HashMap;

HashMap* createHashMap(unsigned int initialSize,
	HashFunction keyHashFunction, EqualsFunction keyEqualsFunction);

#endif
