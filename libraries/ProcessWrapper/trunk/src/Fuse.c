#include <stdlib.h>
#include <time.h>
#include <signal.h>

#ifndef PWRAP_windows
	#include <sys/param.h>
#endif

#include <sys/types.h>

#ifdef PWRAP_linux
	#include <sys/wait.h>
#endif

#include "Fuse.h"
#include "Memory.h"
#include "OSSpecific.h"
#include "StringFunctions.h"
	
#define FS_WAIT_TIME 8

static int unmountImpl(struct FuseMount*);
static const char* getErrorImpl(struct FuseMounter*);
static FuseMount* mountImpl(struct FuseMounter*, const char *mountPoint);
static void fuseMounterDestructor(void *ptr);
static void fuseMountDestructor(void *ptr);
static void uninterruptableSleep(unsigned int seconds);
static void runUnmount(const char *unmountBinary, const char *mountPoint);

typedef struct FuseMountImpl
{
	FuseMount iface;

	char *_unmountBinary;
	char *_mountPoint;
	pid_t _mountPid;
} FuseMountImpl;

typedef struct FuseMounterImpl
{
	FuseMounter iface;

	char *_errorMessage;

	char *_gridBinary;
	char *_unmountBinary;
} FuseMounterImpl;

FuseMounter* createFuseMounter(HashMap *environmentOverload)
{
	FuseMounterImpl *ret = (FuseMounterImpl*)allocate(sizeof(FuseMounterImpl),
		fuseMounterDestructor);

	ret->iface.getError = getErrorImpl;
	ret->iface.mount = mountImpl;

	ret->_errorMessage = NULL;
	ret->_gridBinary = NULL;
	ret->_unmountBinary = NULL;

	fillInOsSpecificFuseInformation(environmentOverload,
		&(ret->_errorMessage), &(ret->_gridBinary), &(ret->_unmountBinary));

	return (FuseMounter*)ret;
}

int unmountImpl(struct FuseMount *mount)
{
	FuseMountImpl *impl = (FuseMountImpl*)mount;

	/* Do we have anything to do? */
	if (impl->_mountPid <= 0)
		return 0;

	runUnmount(impl->_unmountBinary, impl->_mountPoint);

	/* At this point, we would hope that the unmount succeeded, but to be
	 * safe, we go ahead and forceably kill the process too
	 */
	kill(-1 * impl->_mountPid, SIGKILL);

	impl->_mountPid = -1;
	return 0;
}

const char* getErrorImpl(struct FuseMounter *mounter)
{
	FuseMounterImpl *impl = (FuseMounterImpl*)mounter;

	return impl->_errorMessage;
}

FuseMount* mountImpl(struct FuseMounter *mounter, const char *mountPoint)
{
	FuseMounterImpl *impl = (FuseMounterImpl*)mounter;
	FuseMountImpl *ret = (FuseMountImpl*)allocate(sizeof(FuseMountImpl),
		fuseMountDestructor);
	int status;
	int fd;
	char *localMountPoint;

	ret->iface.unmount = unmountImpl;
	
	ret->_unmountBinary = (char*)retain(impl->_unmountBinary);
	ret->_mountPoint = createStringFromCopy(mountPoint);
	ret->_mountPid = fork();

	if (ret->_mountPid < 0)
	{
		fprintf(stderr, "Unable to fork new process for grid fs mount.\n");
		return NULL;
	} else if (ret->_mountPid > 0)
	{
		/* I am the parent */

		/* Check immediatey for the process to have exited or not started,
		 * then Wait for a few seconds to see if the mount exits early */
		sleep(1);
		if (waitpid(ret->_mountPid, &status, WNOHANG) > 0)
		{
			fprintf(stderr, "Grid file system mount exited early!\n");
			return NULL;
		}

		uninterruptableSleep(FS_WAIT_TIME);
		if (waitpid(ret->_mountPid, &status, WNOHANG) > 0)
		{
			fprintf(stderr, "Grid file system mount exited early!\n");
			return NULL;
		}

		/* If we got this far, we assume that the file system is mounted */
	} else
	{
		/* I am the child */

		/* Set myself as a new process group */
		setsid();

		/* We don't need any files open */
		for (fd = 0; fd < NOFILE; fd++)
			close(fd);

		localMountPoint = createStringFromFormat("local:%s",
			mountPoint);
		execl(impl->_gridBinary, impl->_gridBinary,
			"fuse", "--mount", localMountPoint, NULL);

		/* If we got this far, something went wrong */
		fprintf(stderr, "For some reason, the execl command failed!\n");
		exit(1);
	}

	return (FuseMount*)ret;
}

void fuseMounterDestructor(void *ptr)
{
	FuseMounterImpl *impl = (FuseMounterImpl*)ptr;

	release(impl->_errorMessage);
	release(impl->_gridBinary);
	release(impl->_unmountBinary);
}

void fuseMountDestructor(void *ptr)
{
	FuseMountImpl *impl = (FuseMountImpl*)ptr;

	if (impl->_mountPid > 0)
		unmountImpl((struct FuseMount*)ptr);

	release(impl->_unmountBinary);
	release(impl->_mountPoint);
}

void uninterruptableSleep(unsigned int seconds)
{
	time_t doneWaitingTime = time(NULL) + FS_WAIT_TIME;
	int waitTime;

	while (1)
	{
		waitTime = doneWaitingTime - time(NULL);
		if (waitTime <= 0)
			break;
		sleep(waitTime);
	}
}

#ifdef PWRAP_macosx

void runUnmount(const char *unmountBinary, const char *mountPoint)
{
	int result;
	char *commandLine;

	/* For mac os, we use the standard unmount binary */
	commandLine = createStringFromFormat("\"%s\" \"%s\"",
		unmountBinary, mountPoint);

	result = system(commandLine);
	if (result == 0)
	{
		/* It worked fine.  Let's give the file system a couple of seconds
		 * to unmount
		 */
		uninterruptableSleep(4);
	} else if (result < 0)
	{
		fprintf(stderr, "Error trying to run the unmount command.\n");
	} else if (result == 127)
	{
		fprintf(stderr,
			"Error trying to invoke sh to run the unmount command.\n");
	} else if (result > 0)
	{
		fprintf(stderr,
			"Unmount command exited with non-zero exit code!\n");
	}

	release(commandLine);
}

#else

void runUnmount(const char *unmountBinary, const char *mountPoint)
{
	int result;
	char *commandLine;

	/* For linux, we use fusermount to unmount the file system */
	commandLine = createStringFromFormat("\"%s\" -u -z \"%s\"",
		unmountBinary, mountPoint);

	result = system(commandLine);
	if (result == 0)
	{
		/* It worked fine.  Let's give the file system a couple of seconds
		 * to unmount
		 */
		uninterruptableSleep(4);
	} else if (result < 0)
	{
		fprintf(stderr, "Error trying to run the unmount command.\n");
	} else if (result == 127)
	{
		fprintf(stderr,
			"Error trying to invoke sh to run the unmount command.\n");
	} else if (result > 0)
	{
		fprintf(stderr,
			"Unmount command exited with non-zero exit code!\n");
	}

	release(commandLine);
}

#endif
