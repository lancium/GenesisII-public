#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <time.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/wait.h>
#include <sys/stat.h>

#include "Memory.h"
#include "OSSpecific.h"
#include "StringFunctions.h"
#include "Fuse.h"

#define GENII_INSTALL_DIR_VAR "GENII_INSTALL_DIR"
#define GENII_USER_DIR_VAR "GENII_USER_DIR"
#define FUSE_DEVICE "/dev/fuse"

#ifdef PWRAP_macosx
	#define UNMOUNT_BINARY_NAME "umount"
#else
	#define UNMOUNT_BINARY_NAME "fusermount"
#endif

static void commandLineDestructor(void *ptr);
static char** formCommandLine(const char *executable, LinkedList *arguments);
static int setupChildEnvironment(HashMap *environmentVariables,
	const char *standardInput, const char *standardOutput,
	const char *standardError);
static void setupEnvironmentVariables(HashMap *environmentVariables);
static int handlePossibleRedirect(int desiredFD, const char *path, int oflag,
	mode_t mode);
static long long toMicroseconds(struct timeval tv);
static void writeExitResults(const char *path, ExitResults*);
static int checkMountPoint(const char *mountPoint);
static int findGridBinary(HashMap *environmentOverload,
	char **gridBinary, char **errorMessage);
static int verifyUserInstallDir(HashMap *environmentOverload,
	char **errorMessage);
static int findUnmountBinary(char **unmountBinary, char **errorMessage);
static const char* getOverloadedEnvironment(const char *variableName,
	HashMap *environmentOverload);

#ifndef PWRAP_macosx
	static int verifyFuseDevice(char **errorMessage);
#endif

int wrapJob(CommandLine *commandLine)
{
	int exitCode;
	pid_t pid;
	FuseMounter *mounter = NULL;
	FuseMount *mount = NULL;
	char **cmdLine;
	struct rusage usage;
	struct timeval start;
	struct timeval stop;

	/* Now, if a grid file system was requested, we try and set that up.
	 */
	if (commandLine->getGridMountPoint(commandLine))
	{
		if (checkMountPoint(commandLine->getGridMountPoint(commandLine)))
			return -1;
		autorelease(mounter = createFuseMounter(
			commandLine->getEnvironmentVariables(commandLine)));
		if (mounter->getError(mounter))
		{
			fprintf(stderr, "%s\n", mounter->getError(mounter));
			return -1;
		}
	}

	if (!commandLine->getExecutable(commandLine))
		return 0;

	cmdLine = formCommandLine(
		commandLine->getExecutable(commandLine),
		commandLine->getArguments(commandLine));

	if (cmdLine == NULL)
		return -1;

	/* We go ahead and switch working directorys first so that if there are any
	 * relative paths, we use them.
	 */
	if (commandLine->getWorkingDirectory(commandLine))
	{
		if (chdir(commandLine->getWorkingDirectory(commandLine)))
		{
			fprintf(stderr, "Unable to change working directory to %s.\n",
				commandLine->getWorkingDirectory(commandLine));
			return -1;
		}
	}

	if (mounter)
	{
		autorelease(mount = mounter->mount(mounter,
			commandLine->getGridMountPoint(commandLine)));
		if (!mount)
			return -1;
	}

	gettimeofday(&start, NULL);
	pid = fork();
	if (pid < 0)
	{
		/* Couldn't fork */
		fprintf(stderr, "Unable to fork new process.\n");
		release(cmdLine);
		return -1;
	} else if (pid == 0)
	{
		/* I am the child process */
		if (setupChildEnvironment(
			commandLine->getEnvironmentVariables(commandLine),
			commandLine->getStandardInput(commandLine),
			commandLine->getStandardOutput(commandLine),
			commandLine->getStandardError(commandLine)))
			return -1;
		
		execvp(cmdLine[0], cmdLine);

		/* If we got this far, the something went wrong with the exec. */
		fprintf(stderr, "Unable to execute command \"%s\".\n",
			cmdLine[0]);
		release(cmdLine);
		return -1;
	}

	release(cmdLine);

	/* I am the parent process */
	if (wait4(pid, &exitCode, 0x0, &usage) != pid)
	{
		fprintf(stderr, "Unable to wait for child to exit.\n");
		return -1;
	}

	gettimeofday(&stop, NULL);

	if (mount)
		mount->unmount(mount);

	if (WIFSIGNALED(exitCode))
		exitCode = 128 + WTERMSIG(exitCode);
	else
		exitCode = WEXITSTATUS(exitCode);

	writeExitResults(commandLine->getResourceUsageFile(commandLine),
		autorelease(createExitResults(exitCode,
		toMicroseconds(usage.ru_utime),
		toMicroseconds(usage.ru_stime),
		(long long)(stop.tv_sec - start.tv_sec) * (1000 * 1000) +
			(long long)(stop.tv_usec - start.tv_usec),
		(long long)usage.ru_maxrss * 1024)));
	return exitCode;
}

#ifdef PWRAP_macosx

void fillInOsSpecificFuseInformation(HashMap *environmentOverload,
	char **errorMessage, char **gridBinary, char **unmountBinary)
{
	*errorMessage = createStringFromCopy(
		"FUSE file systems not yet supported!");
}

#else

void fillInOsSpecificFuseInformation(HashMap *environmentOverload,
	char **errorMessage, char **gridBinary, char **unmountBinary)
{
	if (findGridBinary(environmentOverload, gridBinary, errorMessage))
		return;
	if (verifyUserInstallDir(environmentOverload, errorMessage))
		return;
	if (findUnmountBinary(unmountBinary, errorMessage))
		return;
#ifndef PWRAP_macosx
	verifyFuseDevice(errorMessage);
#endif

	if (!*gridBinary)
		*errorMessage = createStringFromCopy(
			"Internal Error:  gridBinary undefined!");
	if (!*unmountBinary)
		*errorMessage = createStringFromCopy(
			"Internal Error:  unmountBinary undefined!");
}

#endif

void commandLineDestructor(void *ptr)
{
	char **cmdLine = (char**)ptr;

	while (*cmdLine)
	{
		release(*cmdLine);
		cmdLine++;
	}
}

char** formCommandLine(const char *executable, LinkedList *arguments)
{
	Iterator *iter;
	char **cmdLine;
	int lcv;

	cmdLine = (char**)allocate(
		sizeof(char*) * (arguments->length(arguments) + 2),
		commandLineDestructor);
	memset(cmdLine, 0, sizeof(char*) * (arguments->length(arguments) + 2));
	cmdLine[0] = retain((void*)executable);

	iter = arguments->createIterator(arguments);
	lcv = 1;
	while (iter->hasNext(iter))
		cmdLine[lcv++] = retain(iter->next(iter));

	release(iter);
	return cmdLine;
}

int setupChildEnvironment(HashMap *environmentVariables,
	const char *standardInput, const char *standardOutput,
	const char *standardError)
{
	if (handlePossibleRedirect(2, standardError,
		O_WRONLY | O_CREAT | O_TRUNC, 0644))
		return -1;

	setupEnvironmentVariables(environmentVariables);

	if (handlePossibleRedirect(0, standardInput, O_RDONLY, 0))
		return -1;

	if (handlePossibleRedirect(1, standardOutput,
		O_WRONLY | O_CREAT | O_TRUNC, 0644))
		return -1;

	return 0;
}

void setupEnvironmentVariables(HashMap *environmentVariables)
{
	char *var;
	LinkedList *vars;
	Iterator *iter;

	vars = environmentVariables->createKeyList(environmentVariables);
	iter = vars->createIterator(vars);
	while (iter->hasNext(iter))
	{
		var = iter->next(iter);
		setenv(var, environmentVariables->get(environmentVariables,
			var), 1);
	}

	release(iter);
	release(vars);
}

int handlePossibleRedirect(int desiredFD, const char *path, int oflag,
	mode_t mode)
{
	int fd;

	if (path == NULL)
		return 0;

	fd = open(path, oflag, mode);
	if (fd < 0)
	{
		fprintf(stderr, "Unable to open redirect file \"%s\".\n",
			path);
		return -1;
	}

	if (fd != desiredFD)
	{
		if (dup2(fd, desiredFD) < 0)
		{
			fprintf(stderr,
				"Unable to duplicate file descriptor for redirect \"%s\".\n",
				path);
			return -1;
		}

		close(fd);
	}

	return 0;
}

long long toMicroseconds(struct timeval tv)
{
	long long ret = tv.tv_usec;
	ret += (long long)tv.tv_sec * 1000 * 1000;
	return ret;
}

void writeExitResults(const char *path, ExitResults *results)
{
	FILE *out;

	out = fopen(path, "wt");
	if (!out)
	{
		fprintf(stderr, "Unable to open resource usage file \"%s\".\n",
			path);
	}

	results->toXML(results, out);
	fclose(out);
}

int checkMountPoint(const char *mountPoint)
{
	struct stat statbuf;
	if (stat(mountPoint, &statbuf))
	{
		fprintf(stderr, "Unable to find mount point %s\n",
			mountPoint);
		return -1;
	}

	if (!(statbuf.st_mode & S_IFDIR))
	{
		fprintf(stderr, "Mount point %s is not a directory.\n",
			mountPoint);
		return -1;
	}

	return 0;
}

int findGridBinary(HashMap *environmentOverload,
	char **gridBinary, char **errorMessage)
{
	const char *geniiInstallDir;
	struct stat statbuf;

	geniiInstallDir = getOverloadedEnvironment(GENII_INSTALL_DIR_VAR,
		environmentOverload);

	if (!geniiInstallDir)
	{
		*errorMessage = createStringFromFormat(
			"Environment variable %s is not defined!", GENII_INSTALL_DIR_VAR);
		return 1;
	}

	*gridBinary = createStringFromFormat("%s/grid", geniiInstallDir);
	if (stat(*gridBinary, &statbuf))
	{
		*errorMessage = createStringFromFormat(
			"Grid binary at %s cannot be found.", *gridBinary);
		return 1;
	}

	if (!(statbuf.st_mode & S_IFREG))
	{
		*errorMessage = createStringFromFormat(
			"Grid binary %s is not a file!", *gridBinary);
		return 1;
	}

	if (access(*gridBinary, X_OK))
	{
		*errorMessage = createStringFromFormat(
			"Grid binary %s cannot be executed!", *gridBinary);
		return 1;
	}

	return 0;
}

int verifyUserInstallDir(HashMap *environmentOverload,
	char **errorMessage)
{
	const char *userDir = getOverloadedEnvironment(GENII_USER_DIR_VAR,
		environmentOverload);
	struct stat statbuf;

	if (!userDir)
	{
		*errorMessage = createStringFromFormat(
			"Environment variable %s is not defined!", GENII_USER_DIR_VAR);
		return 1;
	}

	if (stat(userDir, &statbuf))
	{
		*errorMessage = createStringFromFormat(
			"User directory %s does not exist.", userDir);
		return 1;
	}

	if (!(statbuf.st_mode & S_IFDIR))
	{
		*errorMessage = createStringFromFormat(
			"User directory %s is not a directory.", userDir);
		return 1;
	}

	return 0;
}

int findUnmountBinary(char **unmountBinary, char **errorMessage)
{
	AutoreleasePool pool = createAutoreleasePool();
	SSIZE_T index;
	struct stat statbuf;
	char *path = getenv("PATH");
	int result = 1;

	if (!path)
	{
		*errorMessage = createStringFromFormat(
			"Unable to find unmount binary %s.", UNMOUNT_BINARY_NAME);
		goto releasePool;
	}

	autorelease(path = createStringFromCopy(path));
	do
	{
		index = indexOf(path, ':');
		if (index >= 0)
			path[index] = '\0';

		*unmountBinary = createStringFromFormat("%s/%s",
			path, UNMOUNT_BINARY_NAME);
		if (!stat(*unmountBinary, &statbuf))
		{
			if (statbuf.st_mode & S_IFREG)
			{
				if (!access(*unmountBinary, X_OK))
				{
					result = 0;
					goto releasePool;
				}
			}
		}
		release(*unmountBinary);
		*unmountBinary = NULL;
		if (index >= 0)
			path = path + index + 1;
		else
			*path = '\0';
	} while(*path != '\0');

	*errorMessage = createStringFromFormat(
		"Unable to find unmount binary %s.", UNMOUNT_BINARY_NAME);

releasePool:
	release(pool);

	return result;
}

int verifyFuseDevice(char **errorMessage)
{
	struct stat statbuf;

	if (stat(FUSE_DEVICE, &statbuf))
	{
		*errorMessage = createStringFromFormat(
			"Unable to locate fuse device %s.", FUSE_DEVICE);
		return 1;
	}

	if (!(statbuf.st_mode & S_IFCHR))
	{
		*errorMessage = createStringFromFormat(
			"Path %s does not refer to a character device.",
			FUSE_DEVICE);
		return 1;
	}

	if (access(FUSE_DEVICE, R_OK | W_OK))
	{
		*errorMessage = createStringFromFormat(
			"Path %s is not read/write accessible.",	
			FUSE_DEVICE);
		return 1;
	}

	return 0;
}

const char* getOverloadedEnvironment(const char *variableName,
	HashMap *environmentOverload)
{
	const char *result = NULL;

	if (environmentOverload)
		result = (const char*)environmentOverload->get(environmentOverload,
			(void*)variableName);

	if (result == NULL)
		result = getenv(variableName);

	return result;
}
