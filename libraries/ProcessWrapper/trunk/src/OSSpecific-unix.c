#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <libgen.h>
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
#define SLEEP_DURATION 360

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
int beingKilled=0;

void teardownJob()
{
	//we are getting killed
	int exitCode = -1;

	//kill vmwrapper or container
	kill(pid, SIGTERM);

	//wait for vmwrapper/container to terminate
	waitpid(pid, &exitCode, 0);
}

int dumpStats() {
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
	set the exit code to 143. It means the enclosing environment, e.g.,
	the queueing system is terminating it, likely for too much time, processes, 
	or memory.
	*/
	if (running==1 && beingKilled==1) 
	{
		teardownJob();
		// 2020 May 28 CCH: Changing this error code to 143 to be consistent with bash
		exitCode=143;
	}
	writeExitResults(CL->getResourceUsageFile(CL),
               	autorelease(createExitResults(exitCode,
               	toMicroseconds(usage.ru_utime),
               	toMicroseconds(usage.ru_stime),
               	(long long)(stop.tv_sec - start.tv_sec) * (1000 * 1000) +
                       	(long long)(stop.tv_usec - start.tv_usec),
               	(long long)usage.ru_maxrss * 1024)));
	return exitCode;
}

void sig_handler(int signo)
{
	if (signo == SIGTERM) {
		beingKilled=1;
		dumpStats();
	}
	else if (signo == SIGCHLD) {
		dumpStats();
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
  		fprintf(stderr, "Can't catch SIGTERM\n");
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

	const char *exec_str = commandLine->getExecutable(commandLine);

	if (!exec_str)
		return 0;

	cmdLine = formCommandLine(
		exec_str,
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

	//LAK: Start up BES communication
	connectToBes();

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
	if (signal(SIGCHLD, sig_handler) == SIG_ERR)
  		fprintf(stderr, "Can't catch SIGCHLD\n");
	running=1;
	int ticks=0;
	while (running==1 && beingKilled==0) {
		// 2020-06-05 by ASG .. change the order to dumpstats first, then sleep
		if ((ticks % SLEEP_DURATION) == 0)
			exitCode=dumpStats();
		sleep(1);
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
	// 2020-04-16 - deep in Coronovirus shutdown - ASG
	// Also send a copy to the new accounting DB
	
	out = fopen(path, "wt");
	if (!out)
	{
		fprintf(stderr, "Unable to open resource usage file \"%s\".\n",
			path);
	}

	results->toXML(results, out);
	fclose(out);
	// Begin new code
	// path has the following form /path-stuff/JWD/rusage.xml
	//	/A/JWD/C
	//===== New code 2020-04-16
	// The path is of the form /<path prefix>/<job-dir-name>/rusage.xml
	// or - /A/B/C    we turn it into
	//	/A/Accounting/B/C
//===================
    	char* token; 
    	char copy[2048];
	sprintf(copy,"%s",path);
 

	char *rest=copy;
  	int count=0;
	char *tokens[512];
    	while ((token = strtok_r(rest, "/", &rest))) { 
		tokens[count]=token;
		count++;
	}

	char *A,*B,*C;
	char buf[2048];buf[0]='/';buf[1]=0;
	C=tokens[count-1];
	B=tokens[count-2];
	for (int i=0;i<count-2;i++) {strcat(buf,tokens[i]);strcat(buf,"/");}	
	strcat(buf,"Accounting/");
	strcat(buf,B);strcat(buf,"/");

	strcat(buf,C);
// =====================

/*	FILE *f=fopen("/home/dev/debug.txt","a+");
	fprintf(f,"The directory is: %s\n",buf);
	fclose(f);
*/
	out = fopen(buf, "w+");
	if (!out)
	{
		fprintf(stderr, "Unable to open resource usage file \"%s\".\n",
			buf);
	}

	results->toXML(results, out);
	fclose(out);
	//====  end of new code

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


// NOTE: ALL OF THE SOCKET CODE IS NOT THREAD SAFE.

static pthread_t bes_conn_pthread;
static int bes_conn_socket = -1;

//network information of the BES
static struct BESConnectionParameters
{
    char host[128];
    short port;
} bes_conn_params;

void *_startBesConnection(void *arg)
{
	printf("starting bes connection\n");

    // Open the socket
    struct sockaddr_in sa;
    bes_conn_socket = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (bes_conn_socket == -1)
	{
        perror("bes_connection: cannot create socket\n");
        return NULL;
    }

    // Allow re-use of the port
    const static int tr = 1;
    setsockopt(bes_conn_socket,SOL_SOCKET,SO_REUSEADDR,&tr,sizeof(int));

    // Set the host and port
	printf("host=%s", bes_conn_params.host);
	printf("port=%d", bes_conn_params.port);
    memset(&sa, 0, sizeof sa);
    sa.sin_family = AF_INET;
    sa.sin_port = htons(bes_conn_params.port);
    sa.sin_addr.s_addr = inet_addr(bes_conn_params.host);

	if(connect(bes_conn_socket, (const struct sockaddr *)&sa, sizeof(struct sockaddr_in)) < 0)
	{
		char addrStr[INET_ADDRSTRLEN];
		inet_ntop(AF_INET, &(sa.sin_addr), addrStr, INET_ADDRSTRLEN);
        fprintf(stderr, "bes_connection: could not connect to %s:%d\n", addrStr, sa.sin_port);
        goto stop;
    }

	printf("connected to the BES\n");

	//test write
	printf("sending hello string to BES\n");
	write(bes_conn_socket, "hello BES, are you there?\n", 27);

    while(1)
	{
        // test reads
        char buffer[1024];
        int numread = read(bes_conn_socket, buffer, 1023);
        buffer[numread] = '\0';
        printf("bes_listener: got string %s\n", buffer);
    }

    printf("bes_listener: closing connectfd...\n");
    close(bes_conn_socket);

stop:
    close(bes_conn_socket);
    return NULL;
}

void _pipefail(int sig)
{
    fprintf(stderr, "bes_connection: pipefail -- not exiting\n");
}

void _setBesIPInfo()
{
	FILE *bes_info_file;
	char buff[256];

	bes_info_file = fopen(".bes-info", "r");

	fgets(buff, 256, bes_info_file);

	fclose(bes_info_file);

	//parse file
	char *search_ptr;
	char *ip = strtok_r(buff, ":\n", &search_ptr);
	int port = (int)strtol(strtok_r(NULL, ":\n", &search_ptr), NULL, 10);

	if (port == 0)
		fprintf(stderr, "set_bes_ip_info: Failed to parse port number\n");

	if (ip == NULL)
        fprintf(stderr, "set_bes_ip_info: Failed to parse IP address from bes info file\n");

	bes_conn_params.port = port;
	strncpy(bes_conn_params.host, ip, 128);
}

int connectToBes() {
    printf("bes_connection: spawning bes connection thread\n");
	_setBesIPInfo();
    signal(SIGPIPE, &_pipefail);
    int err = pthread_create(&bes_conn_pthread, NULL, &_startBesConnection, NULL);
    if(!err)
	{
        fprintf(stderr, "bes_connection: spawning bes connection thread failed: %d\n", err);
        bes_conn_pthread = 0;
        return -1;
    }
    return 0;
}