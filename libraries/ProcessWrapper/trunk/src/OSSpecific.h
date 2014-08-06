#ifndef __OS_SPECIFIC_H__
#define __OS_SPECIFIC_H__

#include "CommandLine.h"
#include "ExitResults.h"

#ifdef PWRAP_windows
	typedef wchar_t filechar_t;

	#define FILETEXT(STR) L##STR
	#define FILEPRINT fwprintf

#else
	typedef char filechar_t;

	#define FILETEXT(STR) STR
	#define FILEPRINT fprintf
#endif

int wrapJob(CommandLine *commandLine);
void fillInOsSpecificFuseInformation(HashMap *environmentOverload,
	char **errorMessage, char **gridBinary, char **unmountBinary);

#endif
