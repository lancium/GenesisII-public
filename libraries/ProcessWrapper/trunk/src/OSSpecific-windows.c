#include <windows.h>

#include <time.h>
#include <sys/timeb.h>

#include "OSSpecific.h"
#include "StringFunctions.h"

static char* createEscapedString(const char *string);
static wchar_t* createCommandLineString(const char *executable,
	LinkedList* arguments);
static void setupEnvironment(HashMap *env);
static wchar_t* convertWorkingDirectory(CommandLine *commandLine);
static HANDLE openStandardStream(BOOL isStandardInput, const char *path);
HashMap* mergeEnvironments(HashMap *over);
static void writeResults(const char *path, ExitResults *results);

int wrapJob(CommandLine *commandLine)
{
	int exitCode = -1;
	wchar_t *commandLineString;
	HANDLE job;
	int onNT = FALSE;
    DWORD processFlag;
	OSVERSIONINFO ver;
	wchar_t *workingDirectory = convertWorkingDirectory(commandLine);
	STARTUPINFO startupInfo;
	PROCESS_INFORMATION processInfo;
	JOBOBJECT_BASIC_ACCOUNTING_INFORMATION acctInfo;
	JOBOBJECT_EXTENDED_LIMIT_INFORMATION extLimit;
	struct __timeb64 start;
	struct __timeb64 stop;
	ExitResults *results;
	const char *gridMountPoint;

	gridMountPoint = commandLine->getGridMountPoint(commandLine);
	if (gridMountPoint)
	{
		fprintf(stderr,
			"Windows does not support mounting the grid file system.\n");
		goto CreatedNothing;
	}

    ver.dwOSVersionInfoSize = sizeof(ver);
    GetVersionEx(&ver);
    if (ver.dwPlatformId == VER_PLATFORM_WIN32_NT)
        onNT = TRUE;

	if (onNT)
        processFlag = CREATE_NO_WINDOW | CREATE_UNICODE_ENVIRONMENT;
    else
        processFlag = DETACHED_PROCESS;
	
	processFlag |= CREATE_SUSPENDED;

	job = CreateJobObject(NULL, NULL);
	if (job == INVALID_HANDLE_VALUE)
	{
		fprintf(stderr, "Unable to create job object for process.\n");
		goto CreatedNothing;
	}

	commandLineString = createCommandLineString(
		commandLine->getExecutable(commandLine), 
		commandLine->getArguments(commandLine));

	memset(&startupInfo, 0, sizeof(startupInfo));
    startupInfo.cb = sizeof(startupInfo);
    startupInfo.dwFlags = STARTF_USESTDHANDLES;
	startupInfo.hStdInput = openStandardStream(TRUE,
		commandLine->getStandardInput(commandLine));
    startupInfo.hStdOutput = openStandardStream(FALSE,
		commandLine->getStandardOutput(commandLine));
    startupInfo.hStdError = openStandardStream(FALSE,
		commandLine->getStandardError(commandLine));

	setupEnvironment(commandLine->getEnvironmentVariables(commandLine));
	if (!CreateProcess(NULL, commandLineString, NULL, NULL,
		TRUE, processFlag, NULL, workingDirectory,
		&startupInfo, &processInfo))
	{
		fprintf(stderr, "Unable to create process.\n");
		goto CreatedStreams;
	}

	if (!AssignProcessToJobObject(job, processInfo.hProcess))
	{
		fprintf(stderr, "Unable to assign process to job object.\n");
		goto CreatedProcess;
	}

	ResumeThread(processInfo.hThread);
	_ftime(&start);

CreatedProcess:
	WaitForSingleObject(processInfo.hProcess, INFINITE);
	_ftime(&stop);

	QueryInformationJobObject(job, JobObjectBasicAccountingInformation,
		&acctInfo, sizeof(acctInfo), NULL);
	QueryInformationJobObject(job, JobObjectExtendedLimitInformation,
		&extLimit, sizeof(extLimit), NULL);
	
	GetExitCodeProcess(processInfo.hProcess, &exitCode);

	results = createExitResults(exitCode,
		acctInfo.TotalUserTime.QuadPart / 10,
		acctInfo.TotalKernelTime.QuadPart / 10,
		((__int64)(stop.time - start.time) * 1000000) +
			((__int64)(stop.millitm - start.millitm) * 1000),
			extLimit.PeakJobMemoryUsed);

	if (commandLine->getResourceUsageFile(commandLine) != NULL)
		writeResults(commandLine->getResourceUsageFile(commandLine), results);
	release(results);

	CloseHandle(processInfo.hProcess);
	CloseHandle(processInfo.hThread);
	
CreatedStreams:
	if (startupInfo.hStdInput != INVALID_HANDLE_VALUE)
		CloseHandle(startupInfo.hStdInput);
	if (startupInfo.hStdOutput != INVALID_HANDLE_VALUE)
		CloseHandle(startupInfo.hStdOutput);
	if (startupInfo.hStdError != INVALID_HANDLE_VALUE)
		CloseHandle(startupInfo.hStdError);

CreatedJobObject:
	free(commandLineString);

	CloseHandle(job);

CreatedNothing:
	release(workingDirectory);

	return exitCode;
}

void fillInOsSpecificFuseInformation(char **errorMessage,
	char **gridBinary, char **unmountBinary)
{
	*errorMessage = createStringFromCopy(
		"Windows does not support FUSE file systems!");
}

char* createEscapedString(const char *string)
{
	size_t sourcei;
	size_t targeti;
	size_t length = strlen(string);
	char *ret = (char*)allocate(sizeof(char) * (length * 2 + 3), NULL);
	ret[0] = '"';
	for (sourcei = 0, targeti = 1; string[sourcei]; sourcei++)
	{
		if (string[sourcei] == '"')
			ret[targeti++] = '\\';

		ret[targeti++] = string[sourcei];
	}

	ret[targeti++] = '"';
	ret[targeti] = (char)0;
	return ret;
}

wchar_t *createCommandLineString(const char *executable,
	LinkedList* arguments)
{
	StringBuilder *builder = createStringBuilder(128);
	Iterator *iter;
	char *tmp;
	wchar_t *ret;
	wchar_t *rett;

	builder->append(builder, autorelease(createEscapedString(executable)));
	
	iter = arguments->createIterator(arguments);
	while (iter->hasNext(iter))
	{
		tmp = (char*)iter->next(iter);
		builder->append(builder, " ");
		builder->append(builder, autorelease(createEscapedString(tmp)));
	}

	tmp = builder->createString(builder);
	release(builder);

	ret = createWideStringFromMBCopy(tmp);
	release(tmp);

	rett = (wchar_t*)malloc(sizeof(wchar_t) * (wcslen(ret) + 1));
	wcscpy(rett, ret);
	release(ret);

	return rett;
}
void setupEnvironment(HashMap *env)
{
	LinkedList *keys;
	Iterator *iter;
	const char *key;
	const char *value;
	wchar_t *wKey;
	wchar_t *wValue;

	keys = env->createKeyList(env);
	iter = keys->createIterator(keys);
	while (iter->hasNext(iter))
	{
		key = (const char*)(iter->next(iter));
		value = (const char*)(env->get(env, key));

		wKey = createWideStringFromMBCopy(key);
		wValue = createWideStringFromMBCopy(value);

		SetEnvironmentVariable(wKey, wValue);
		release(wKey);
		release(wValue);
	}

	release(iter);
	release(keys);
}

wchar_t* convertWorkingDirectory(CommandLine *commandLine)
{
	const char *workingDir = commandLine->getWorkingDirectory(commandLine);
	
	if (workingDir == NULL)
		return NULL;

	return createWideStringFromMBCopy(workingDir);
}

HANDLE openStandardStream(BOOL isStandardInput, const char *path)
{
	wchar_t *wPath = createWideStringFromMBCopy(path);
	HANDLE ret;
	SECURITY_ATTRIBUTES sa;

	memset(&sa, 0, sizeof(SECURITY_ATTRIBUTES));
	sa.nLength = sizeof(SECURITY_ATTRIBUTES);
	sa.lpSecurityDescriptor = NULL;
	sa.bInheritHandle = TRUE;

	if (path == NULL)
		return path = "NUL";

	ret = CreateFile(wPath, isStandardInput ? GENERIC_READ : GENERIC_WRITE,
		isStandardInput ? FILE_SHARE_READ : FILE_SHARE_WRITE,
		&sa, isStandardInput ? OPEN_EXISTING : CREATE_ALWAYS,
		FILE_ATTRIBUTE_NORMAL, NULL);
	release(wPath);
	if (ret == INVALID_HANDLE_VALUE)
	{
		fprintf(stderr, "Unable to create/open standard redirect file \"%s\".\n",
			path);
		exit(-1);
	}

	return ret;
}

void writeResults(const char *path, ExitResults *results)
{
	FILE *output;

	output = fopen(path, "wt");
	if (!output)
	{
		fprintf(stderr, "Unable to open results output file.\n");
		return;
	}

	results->toXML(results, output);

	fclose(output);
}
