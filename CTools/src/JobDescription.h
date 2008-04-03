#ifndef __JOB_DESCRIPTION_H__
#define __JOB_DESCRIPTION_H__

#include "StringPair.h"
#include "PairList.h"

typedef struct JobDescriptionType *JobDescription;
typedef struct JobDescriptionType
{
	void (*setJobName)(JobDescription, const char *jobName);
	const char* (*getJobName)(JobDescription);

	void (*setRequiredOGRSHVersion)(JobDescription,
		const char *requiredOGRSHVersion);
	const char* (*getRequiredOGRSHVersion)(JobDescription);

	void (*setStdinRedirect)(JobDescription, const char *stdinRedirect);
	void (*setStdoutRedirect)(JobDescription, const char *stdoutRedirect);
	void (*setStderrRedirect)(JobDescription, const char *stderrRedirect);
	const char* (*getStdinRedirect)(JobDescription);
	const char* (*getStdoutRedirect)(JobDescription);
	const char* (*getStderrRedirect)(JobDescription);

	void (*addEnvironmentPair)(JobDescription,
		const char *variable, const char *value);
	PairListIterator (*getEnvironmentIterator)(JobDescription);

	void (*addStageIn)(JobDescription,
		const char *filename, const char *uri);
	PairListIterator (*getStageInIterator)(JobDescription);

	void (*addStageOut)(JobDescription,
		const char *filename, const char *uri);
	PairListIterator (*getStageOutIterator)(JobDescription);

	void (*setExecutable)(JobDescription, const char *executable);
	const char* (*getExecutable)(JobDescription);

	void (*addArgument)(JobDescription, const char *arg);
	PairListIterator (*getArguments)(JobDescription);

	const char* (*verify)(JobDescription);

	void (*destroy)(JobDescription);
} JobDescriptionType;

JobDescription createJobDescription();

#endif
