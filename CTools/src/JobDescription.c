#include <stdlib.h>

#include "StringUtils.h"
#include "StringPair.h"
#include "PairList.h"
#include "JobDescription.h"

typedef struct JobDescriptionImpl
{
	JobDescriptionType jobDescType;

	char *_jobName;
	char *_ogrshVersion;
	char *_stdin;
	char *_stdout;
	char *_stderr;

	PairList _environment;
	PairList _stageIns;
	PairList _stageOuts;

	char *_executable;
	PairList _arguments;
} JobDescriptionImpl;

static void setJobNameImpl(JobDescription desc, const char *jobName);
static const char* getJobNameImpl(JobDescription desc);
static void setRequiredOGRSHVersionImpl(JobDescription desc,
	const char *requiredOGRSHVersion);
static const char* getRequiredOGRSHVersionImpl(JobDescription desc);
static void setStdinRedirectImpl(JobDescription, const char *stdinRedirect);
static void setStdoutRedirectImpl(JobDescription, const char *stdoutRedirect);
static void setStderrRedirectImpl(JobDescription, const char *stderrRedirect);
static const char* getStdinRedirectImpl(JobDescription);
static const char* getStdoutRedirectImpl(JobDescription);
static const char* getStderrRedirectImpl(JobDescription);
static void addEnvironmentPairImpl(JobDescription,
	const char *variable, const char *value);
static PairListIterator getEnvironmentIteratorImpl(JobDescription);
static void addStageInImpl(JobDescription,
	const char *filename, const char *uri);
static PairListIterator getStageInIteratorImpl(JobDescription);
static void addStageOutImpl(JobDescription,
	const char *filename, const char *uri);
static PairListIterator getStageOutIteratorImpl(JobDescription);
static void setExecutableImpl(JobDescription, const char *executable);
static const char* getExecutableImpl(JobDescription);
static void addArgumentImpl(JobDescription, const char *arg);
static PairListIterator getArgumentsImpl(JobDescription);
static const char* verify(JobDescription);
static void destroyImpl(JobDescription);

JobDescription createJobDescription()
{
	JobDescriptionImpl *impl;

	impl = (JobDescriptionImpl*)malloc(sizeof(JobDescriptionImpl));

	impl->jobDescType.setJobName = setJobNameImpl;
	impl->jobDescType.getJobName = getJobNameImpl;
	impl->jobDescType.setRequiredOGRSHVersion = setRequiredOGRSHVersionImpl;
	impl->jobDescType.getRequiredOGRSHVersion = getRequiredOGRSHVersionImpl;
	impl->jobDescType.setStdinRedirect = setStdinRedirectImpl;
	impl->jobDescType.setStdoutRedirect = setStdoutRedirectImpl;
	impl->jobDescType.setStderrRedirect = setStderrRedirectImpl;
	impl->jobDescType.getStdinRedirect = getStdinRedirectImpl;
	impl->jobDescType.getStdoutRedirect = getStdoutRedirectImpl;
	impl->jobDescType.getStderrRedirect = getStderrRedirectImpl;
	impl->jobDescType.addEnvironmentPair = addEnvironmentPairImpl;
	impl->jobDescType.getEnvironmentIterator = getEnvironmentIteratorImpl;
	impl->jobDescType.addStageIn = addStageInImpl;
	impl->jobDescType.getStageInIterator = getStageInIteratorImpl;
	impl->jobDescType.addStageOut = addStageOutImpl;
	impl->jobDescType.getStageOutIterator = getStageOutIteratorImpl;
	impl->jobDescType.setExecutable = setExecutableImpl;
	impl->jobDescType.getExecutable = getExecutableImpl;
	impl->jobDescType.addArgument = addArgumentImpl;
	impl->jobDescType.getArguments = getArgumentsImpl;
	impl->jobDescType.verify = verify;
	impl->jobDescType.destroy = destroyImpl;
	
	impl->_jobName = NULL;
	impl->_ogrshVersion = NULL;
	impl->_stdin = NULL;
	impl->_stdout = NULL;
	impl->_stderr = NULL;
	impl->_environment = createPairList();
	impl->_stageIns = createPairList();
	impl->_stageOuts = createPairList();
	impl->_executable = NULL;
	impl->_arguments = createPairList();

	return (JobDescription)impl;
}

void setJobNameImpl(JobDescription desc, const char *jobName)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	if (impl->_jobName != NULL)
		free(impl->_jobName);
	impl->_jobName = duplicateString(jobName);
}

const char* getJobNameImpl(JobDescription desc)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	return impl->_jobName;
}

void setRequiredOGRSHVersionImpl(JobDescription desc,
	const char *requiredOGRSHVersion)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	if (impl->_ogrshVersion != NULL)
		free(impl->_ogrshVersion);
	impl->_ogrshVersion = duplicateString(requiredOGRSHVersion);
}

const char* getRequiredOGRSHVersionImpl(JobDescription desc)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	return impl->_ogrshVersion;
}

void setStdinRedirectImpl(JobDescription desc, const char *stdinRedirect)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	if (impl->_stdin != NULL)
		free(impl->_stdin);
	impl->_stdin = duplicateString(stdinRedirect);
}

void setStdoutRedirectImpl(JobDescription desc, const char *stdoutRedirect)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	if (impl->_stdout != NULL)
		free(impl->_stdout);
	impl->_stdout = duplicateString(stdoutRedirect);
}

void setStderrRedirectImpl(JobDescription desc, const char *stderrRedirect)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	if (impl->_stderr != NULL)
		free(impl->_stderr);
	impl->_stderr = duplicateString(stderrRedirect);
}

const char* getStdinRedirectImpl(JobDescription desc)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	return impl->_stdin;
}

const char* getStdoutRedirectImpl(JobDescription desc)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	return impl->_stdout;
}

const char* getStderrRedirectImpl(JobDescription desc)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	return impl->_stderr;
}

void addEnvironmentPairImpl(JobDescription desc,
	const char *variable, const char *value)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;
	StringPair pair;

	pair = createStringPair(variable, value);
	impl->_environment->append(impl->_environment, pair);
	pair->destroy(pair);
}

PairListIterator getEnvironmentIteratorImpl(JobDescription desc)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;
	return impl->_environment->iterator(impl->_environment);
}

void addStageInImpl(JobDescription desc,
	const char *filename, const char *uri)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;
	StringPair pair;

	pair = createStringPair(filename, uri);
	impl->_stageIns->append(impl->_stageIns, pair);
	pair->destroy(pair);
}

PairListIterator getStageInIteratorImpl(JobDescription desc)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;
	return impl->_stageIns->iterator(impl->_stageIns);
}

void addStageOutImpl(JobDescription desc,
	const char *filename, const char *uri)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;
	StringPair pair;

	pair = createStringPair(filename, uri);
	impl->_stageOuts->append(impl->_stageOuts, pair);
	pair->destroy(pair);
}

PairListIterator getStageOutIteratorImpl(JobDescription desc)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;
	return impl->_stageOuts->iterator(impl->_stageOuts);
}

void setExecutableImpl(JobDescription desc, const char *executable)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	if (impl->_executable != NULL)
		free(impl->_executable);
	impl->_executable = duplicateString(executable);
}

const char* getExecutableImpl(JobDescription desc)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	return impl->_executable;
}

void addArgumentImpl(JobDescription desc, const char *arg)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;
	StringPair pair;

	pair = createStringPair(arg, NULL);
	impl->_arguments->append(impl->_arguments, pair);
	pair->destroy(pair);
}

PairListIterator getArgumentsImpl(JobDescription desc)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;
	return impl->_arguments->iterator(impl->_arguments);
}

const char* verify(JobDescription desc)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	if (impl->_executable == NULL)
		return "Executable parameter cannot be left empty.";

	return NULL;
}

void destroyImpl(JobDescription desc)
{
	JobDescriptionImpl *impl = (JobDescriptionImpl*)desc;

	if (impl->_jobName != NULL)
		free(impl->_jobName);
	if (impl->_ogrshVersion != NULL)
		free(impl->_ogrshVersion);
	if (impl->_stdin != NULL)
		free(impl->_stdin);
	if (impl->_stdout != NULL)
		free(impl->_stdout);
	if (impl->_stderr != NULL)
		free(impl->_stderr);
	if (impl->_executable != NULL)
		free(impl->_executable);

	impl->_environment->destroy(impl->_environment);
	impl->_stageIns->destroy(impl->_stageIns);
	impl->_stageOuts->destroy(impl->_stageOuts);
	impl->_arguments->destroy(impl->_arguments);
}
