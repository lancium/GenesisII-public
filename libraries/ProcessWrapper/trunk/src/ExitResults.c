#include "ExitResults.h"
#include "Memory.h"

#include <stdio.h>

static int exitCodeImpl(struct ExitResults*);
static void toJsonImpl(struct ExitResults*, FILE*);

typedef struct ExitResultsImpl
{
	ExitResults interface;

	int _exitCode;
	double _userTimeSeconds;
	double _systemTimeSeconds;
	double _wallclockTimeSeconds;
	long long _maximumRSSBytes;
	char _processorID[256];
} ExitResultsImpl;

ExitResults* createExitResults(int exitCode,
	double userTimeSeconds,
	double systemTimeSeconds,
	double wallclockTimeSeconds,
	long long maximumRSSBytes,
	char* processorID)
{
	ExitResultsImpl *impl = allocate(sizeof(ExitResultsImpl), NULL);

	impl->interface.exitCode = exitCodeImpl;
	impl->interface.toJson = toJsonImpl;

	impl->_exitCode = exitCode;
	impl->_userTimeSeconds = userTimeSeconds;
	impl->_systemTimeSeconds = systemTimeSeconds;
	impl->_wallclockTimeSeconds = wallclockTimeSeconds;
	impl->_maximumRSSBytes = maximumRSSBytes;
	strncpy(impl->_processorID, processorID, 255);

	return (ExitResults*)impl;
}

int exitCodeImpl(struct ExitResults *er)
{
	ExitResultsImpl *impl = (ExitResultsImpl*)er;

	return impl->_exitCode;
}

void toJsonImpl(struct ExitResults *er, FILE *out)
{
	ExitResultsImpl *impl = (ExitResultsImpl*)er;

	fprintf(out, "{\n    \"location\": {\n");
	fprintf(out, "         \"exit-code\": \"%d\",\n", impl->_exitCode);
	fprintf(out, "         \"user-time\": \"%f\",\n", impl->_userTimeSeconds);
	fprintf(out, "         \"system-time\": \"%f\",\n", impl->_systemTimeSeconds);
	fprintf(out, "         \"wallclock-time\": \"%f\",\n", impl->_wallclockTimeSeconds);
	fprintf(out, "         \"maximum-rss\": \"%lld\",\n", impl->_maximumRSSBytes);
	fprintf(out, "         \"processor-id\": \"%s\"\n", impl->_processorID);
	fprintf(out, "    }\n}");
}
