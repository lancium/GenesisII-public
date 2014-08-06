#include <stdio.h>
#include <stdlib.h>

#include "Memory.h"

static unsigned long bytesAllocated = 0;
static unsigned long bytesDeallocated = 0;

typedef struct RetainInformation
{
	int retainCount;
	unsigned int allocationSize;
	Destructor destructor;
} RetainInformation;

void* allocate(size_t size, Destructor destructor)
{
	RetainInformation *ret = (RetainInformation*)malloc(
		sizeof(RetainInformation) + size);
	ret->retainCount = 1;
	ret->allocationSize = size;
	ret->destructor = destructor;

	bytesAllocated += size;
	return ret + 1;
}

void* setDestructor(void *ptr, Destructor destructor)
{
	if (ptr)
	{
		RetainInformation *ret = (RetainInformation*)ptr;
		ret -= 1;
		ret->destructor = destructor;
	}
	
	return ptr;
}

void* retain(void *ptr)
{
	if (ptr)
	{
		RetainInformation *ret = (RetainInformation*)ptr;
		ret -= 1;
		ret->retainCount++;
	}

	return ptr;
}

int retainCount(void *ptr)
{
	if (ptr)
	{
		RetainInformation *ret = (RetainInformation*)ptr;
		ret -= 1;
		return ret->retainCount;
	}

	return 0;
}

void release(void *ptr)
{
	if (ptr)
	{
		RetainInformation *ret = (RetainInformation*)ptr;
		ret -= 1;
		ret->retainCount--;
		if (ret->retainCount == 0)
		{
			if (ret->destructor)
				ret->destructor(ptr);
			bytesDeallocated += ret->allocationSize;
			free(ret);
		}
	}
}

typedef struct AutoreleaseNode
{
	void *ptr;
	struct AutoreleaseNode *next;
} AutoreleaseNode;

typedef struct AutoreleasePoolType
{
	AutoreleaseNode *head;
	struct AutoreleasePoolType *prev;
	struct AutoreleasePoolType *next;
} AutoreleasePoolType;

static void autoreleasePoolDestructor(void *ptr);
static AutoreleasePoolType* headPool = NULL;
static AutoreleasePoolType* tailPool = NULL;

void* autorelease(void *ptr)
{
	AutoreleaseNode *node;

	if (!ptr)
		return ptr;

	if (!tailPool)
	{
		fprintf(stderr, "No Autorelease pool defined.\n");
		exit(-1);
	}

	node = (AutoreleaseNode*)malloc(sizeof(AutoreleaseNode));
	node->ptr = ptr;
	node->next = tailPool->head;
	tailPool->head = node;
	return ptr;
}

AutoreleasePool createAutoreleasePool()
{
	AutoreleasePoolType *pool;

	pool = (AutoreleasePoolType*)allocate(sizeof(AutoreleasePoolType),
		autoreleasePoolDestructor);
	if (tailPool)
		tailPool->next = pool;
	else
		headPool = pool;
	pool->next = NULL;
	pool->prev = tailPool;
	pool->head = NULL;
	tailPool = pool;

	return pool;
}

void autoreleasePoolDestructor(void *ptr)
{
	AutoreleasePoolType *pool = ptr;
	AutoreleaseNode *node;

	if (pool->next)
		release(pool->next);

	tailPool = pool->prev;
	if (tailPool)
		tailPool->next = NULL;
	else
		headPool = NULL;

	while (pool->head)
	{
		node = pool->head;
		pool->head = node->next;
		release(node->ptr);
		free(node);
	}
}


AllocationStatistics allocationStatistics()
{
	AllocationStatistics stats = {bytesAllocated, bytesDeallocated};
	return stats;
}
