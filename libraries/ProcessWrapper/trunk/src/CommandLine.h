#ifndef __COMMAND_LINE_H__
#define __COMMAND_LINE_H__

#include "Memory.h"
#include "LinkedList.h"
#include "HashMap.h"

typedef struct CommandLine
{
	const char* (*getResourceUsageFile)(struct CommandLine*);

	const char* (*getGridMountPoint)(struct CommandLine*);
	const char* (*getWorkingDirectory)(struct CommandLine*);

	const char* (*getStandardInput)(struct CommandLine*);
	const char* (*getStandardOutput)(struct CommandLine*);
	const char* (*getStandardError)(struct CommandLine*);

	HashMap* (*getEnvironmentVariables)(struct CommandLine*);

	const char* (*getExecutable)(struct CommandLine*);
	LinkedList* (*getArguments)(struct CommandLine*);
} CommandLine;

CommandLine* createCommandLineFromArguments(int argc, char **argv);

#endif
