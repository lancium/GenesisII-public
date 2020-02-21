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
#include <signal.h>

#include <pthread.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>



#include "Memory.h"
#include "OSSpecific.h"
#include "StringFunctions.h"
#include "Fuse.h"

#define GENII_INSTALL_DIR_VAR "GENII_INSTALL_DIR"
#define GENII_USER_DIR_VAR "GENII_USER_DIR"
#define FUSE_DEVICE "/dev/fuse"
#define SLEEP_DURATION 1

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

// The following is an ugly hack by ASG to allow the signal handler to print out info
CommandLine *CL=0;
struct timeval start;
struct timeval stop;
pid_t pid;
int running=1;

int dumpStats(int beingKilled) {
/* 2019-05-28 by ASG. dump-stats gets the rusage info and dumps it to a file.
	Note that it is using global variables. This is unfortunate, but the
	only way to get the data into a signal handler.
*/
	int exitCode=100;
	struct rusage usage;
	// Check if it is still running, if not set running=0
	pid_t tp=waitpid(pid,&exitCode,WNOHANG);
	if (tp==pid) running=0;
	getrusage(RUSAGE_CHILDREN,&usage);
	if (WIFSIGNALED(exitCode))
		exitCode = 128 + WTERMSIG(exitCode);
	else
		exitCode = WEXITSTATUS(exitCode);
	gettimeofday(&stop, NULL);
	/*
	2019-08-22 by ASG. If the child is still running AND being killed,
	set the exit code to 250. It means the enclosing environment, e.g.,
	the queueing system is terminating it, likely for too much time, processes,
	or memory.
	*/
	if (running==1 && beingKilled==1) exitCode=250;

	writeExitResults(CL->getResourceUsageFile(CL),
               	autorelease(createExitResults(exitCode,
               	toMicroseconds(usage.ru_utime),
               	toMicroseconds(usage.ru_stime),
               	(long long)(stop.tv_sec - start.tv_sec) * (1000 * 1000) +
                       	(long long)(stop.tv_usec - start.tv_usec),
               	(long long)usage.ru_maxrss * 1024)));

	/* 2020-01-08 LAK terminating child process instead of waiting for queueing
	   system to kill the process itself
	*/
	if (running==1 && beingKilled==1) kill(pid, SIGKILL);

	return exitCode;
}

void sig_handler(int signo)
{
	if (signo == SIGTERM) {
		dumpStats(1);
	}
}

int wrapJob(CommandLine *commandLine)
{
	int exitCode;

	FuseMounter *mounter = NULL;
	FuseMount *mount = NULL;
	char **cmdLine;
	// 2019--5-27 by ASG. Put in signal handler for SIGTERM
	CL=commandLine;
	if (signal(SIGTERM, sig_handler) == SIG_ERR)
  		printf("\ncan't catch SIGTERM\n");
	// End updates

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
/*		Old code
	if (wait4(pid, &exitCode, 0x0, &usage) != pid)
	{
		fprintf(stderr, "Unable to wait for child to exit.\n");
		return -1;
	}
*/
	// 2019-05-27 by ASG. Code to deal with the process getting killed and losing
	// the accounting records.
	running=1;
	int ticks=0;
	while (running==1) {
		sleep(SLEEP_DURATION);
		exitCode=dumpStats(0);
		ticks++;
	}
	// End of updates

	if (mount)
		mount->unmount(mount);


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

// NOTE: The following code is thread-unsafe.

static pthread_t bes_conn_pthread;
static pthread_t bes_listener_pthread;
static int bes_listen_socket = -1;
static int bes_connect_socket = -1;

static struct BESListenerParameters {
    const char *hostmask;
    short port;
} bes_listener_params;

static struct BESConnectionParameters {
    const char *host;
    short port;
} bes_conn_params;

void *_startListener(void *arg) {
    // Open the main control socket
    struct sockaddr_in sa;
    bes_listen_socket = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (bes_listen_socket == -1) {
        perror("cannot create socket");
        return NULL;
    }

    // Allow re-use of the port
    const static int tr = 1;
    setsockopt(bes_listen_socket,SOL_SOCKET,SO_REUSEADDR,&tr,sizeof(int));

    // Set the host mask and port
    memset(&sa, 0, sizeof sa);
    sa.sin_family = AF_INET;
    sa.sin_port = htons(bes_listener_params.port);
    inet_pton(AF_INET, bes_listener_params.hostmask, &sa.sin_addr.s_addr);
    // sa.sin_addr.s_addr = htonl(INADDR_ANY);

    // Bind to port
    if(bind(bes_listen_socket, (struct sockaddr *)&sa, sizeof sa) == -1) {
        perror("bind failed");
        goto stop;
    }

    // Listen for connections
    if(listen(bes_listen_socket, 10) == -1) {
        perror("listen failed");
        goto stop;
    }

    // TODO: Make this a loop? - jth 02/21/2020
    {
        struct sockaddr_in client_addr;
        socklen_t client_addr_size = sizeof(client_addr);
        int connectfd = accept(bes_listen_socket, (struct sockaddr *)&client_addr,
                &client_addr_size);

        if(connectfd < 0) {
            perror("accept failed");
            close(connectfd);
            goto stop;
        }

        char ip[INET_ADDRSTRLEN];
        inet_ntop(AF_INET, &client_addr, ip, client_addr_size);
        printf("Established connection to %s:%hu\n",
                ip, ntohs(client_addr.sin_port));

        while(1) {
            // TODO: experimental
            char buffer[1024];
            int numread = read(connectfd, buffer, 1023);
            buffer[numread] = '\0';

            printf("bes_listener: got string %s\n", buffer);
        }

        printf("closing...\n");

        close(connectfd);
    }

stop:

    close(bes_listen_socket);

    return NULL;
}

void *_startBesConnection(void *arg) {
    struct sockaddr_in addr;
    int port = bes_conn_params.port;
    const char *ip = bes_conn_params.host;
    memset(&addr, 0, sizeof(struct sockaddr_in));
    inet_pton(AF_INET, ip, &addr.sin_addr);
    addr.sin_port = htons(port);
    addr.sin_family = AF_INET;

    if(connect(bes_connect_socket, (const struct sockaddr *)&addr, sizeof(struct sockaddr_in)) < 0) {
        fprintf(stderr, "err: could not connect to %s:%d\n", ip, port);
        return NULL;
    }

    close(bes_connect_socket);

    return NULL;
}

int startBesListener() {
    printf("info: spawning bes listener thread\n");
    bes_listener_params.hostmask = "0.0.0.0"; // TODO: set hostmask
    int err = pthread_create(&bes_listener_pthread, NULL, &_startListener, NULL);
    if(!err) {
        printf("err: spawning bes listener thread failed: %d\n", err);
        bes_listener_pthread = 0;
        return -1;
    }
    return 0;
}

int stopBesListener() {
    pthread_cancel(bes_listener_pthread);
    return 0;
}

void _pipefail(int sig) {
    fprintf(stderr, "bes_connect: pipefail -- not exiting");
}

int connectToBes() {
    printf("info: spawning bes connection thread\n");
    bes_conn_params.port = 9999;       // TODO: set port
    bes_conn_params.host = "10.0.0.1"; // TODO: set host
    signal(SIGPIPE, &_pipefail);
    int err = pthread_create(&bes_conn_pthread, NULL, &_startBesConnection, NULL);
    if(!err) {
        printf("err: spawning bes connection thread failed: %d\n", err);
        bes_conn_pthread = 0;
        return -1;
    }
    return 0;
}
