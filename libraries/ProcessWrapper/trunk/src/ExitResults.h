#ifndef __EXIT_RESULTS_H__
#define __EXIT_RESULTS_H__

#include <stdio.h>

typedef struct ExitResults
{
	int (*exitCode)(struct ExitResults*);
	void (*toJson)(struct ExitResults*, FILE*);
} ExitResults;

ExitResults* createExitResults(int exitCode,
	double userTimeSeconds,
	double systemTimeSeconds,
	double wallclockTimeSeconds,
	long long maximumRSSBytes,
	char* processorID);

#endif
