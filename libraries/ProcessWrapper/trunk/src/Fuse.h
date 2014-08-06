#ifndef __FUSE_H__
#define __FUSE_H__

#include "HashMap.h"

typedef struct FuseMount
{
	int (*unmount)(struct FuseMount*);
} FuseMount;

typedef struct FuseMounter
{
	const char* (*getError)(struct FuseMounter*);
	FuseMount* (*mount)(struct FuseMounter*, const char *mountPoint);
} FuseMounter;

FuseMounter* createFuseMounter(HashMap *environmentOverload);

#endif
