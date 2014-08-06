#ifndef __EXIT_RESULTS_H__
#define __EXIT_RESULTS_H__

#include <stdio.h>

typedef struct ExitResults
{
	int (*exitCode)(struct ExitResults*);
	void (*toXML)(struct ExitResults*, FILE*);
} ExitResults;

ExitResults* createExitResults(int exitCode,
	long long userTimeMicroseconds,
	long long systemTimeMicroseconds,
	long long wallclockTimeMicroseconds,
	long long maximumRSSBytes);

#endif
