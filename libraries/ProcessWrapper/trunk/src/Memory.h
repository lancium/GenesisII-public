#ifndef __MEMORY_H__
#define __MEMORY_H__

#include <stdio.h>

typedef void (*Destructor)(void*);

void* allocate(size_t, Destructor);
void* setDestructor(void *ptr, Destructor destructor);
void* retain(void*);
void release(void*);
int retainCount(void*);
void* autorelease(void*);

typedef void* AutoreleasePool;

AutoreleasePool createAutoreleasePool();

typedef struct AllocationStatistics
{
	unsigned long bytesAllocated;
	unsigned long bytesDeallocated;
} AllocationStatistics;

AllocationStatistics allocationStatistics();

#endif
