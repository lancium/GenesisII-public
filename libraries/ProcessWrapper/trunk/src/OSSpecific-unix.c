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
#include <errno.h>

#include <pthread.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <semaphore.h>

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

// 2020-07-23 by JAA -- just a quick struct for the CPU info
// This is currently designed around Intel processors.
// AMD CPUs may not provide GHz, so changes will be necessary
typedef struct {
	char processorType[64]; // This should be enough, let me know. Also, null terminated
	float GHz; // in GHZ, e.g., 2.5
} procInfo;

static void commandLineDestructor(void *ptr);
static char** formCommandLine(const char *executable, LinkedList *arguments);
static int setupChildEnvironment(HashMap *environmentVariables,
	const char *standardInput, const char *standardOutput,
	const char *standardError);
static void setupEnvironmentVariables(HashMap *environmentVariables);
static int handlePossibleRedirect(int desiredFD, const char *path, int oflag,
	mode_t mode);
static void writeExitResults(const char *path, ExitResults*);
static int checkMountPoint(const char *mountPoint);
static int findGridBinary(HashMap *environmentOverload,
	char **gridBinary, char **errorMessage);
static int verifyUserInstallDir(HashMap *environmentOverload,
	char **errorMessage);
static int findUnmountBinary(char **unmountBinary, char **errorMessage);
static const char* getOverloadedEnvironment(const char *variableName,
	HashMap *environmentOverload);

int sendBesMessage(const char* message);
int connectionSetup();
void freeze();
void thaw();

#ifndef PWRAP_macosx
	static int verifyFuseDevice(char **errorMessage);
#endif

// The following is an ugly hack by ASG to allow the signal handler to print out info
CommandLine *CL=0;
struct timeval start;
struct timeval stop;
pid_t pid;

int externalKill = 0;
static pthread_t bes_conn_pthread;

procInfo getProcInfo(){
	char * line = NULL;
	size_t len = 0;
	ssize_t read;
	procInfo p = {};
	int isIntel = 0;

	FILE *cpuinfo = fopen("/proc/cpuinfo", "rb");

	if (cpuinfo == NULL){
		perror("Failed to open file");
		return p;
	}

	while ((read = getline(&line, &len, cpuinfo)) != -1){
		if (strstr(line, "vendor_id	:") != NULL){
			strtok(line, ":\n");
			char * vendor = strtok(NULL, ":\n");
			if(strcmp(vendor+1, "GenuineIntel\n") == 0){
				isIntel = 1;
			}
		}
		if (strstr(line, "model name	:") != NULL){
			strtok(line, ":\n");
			char * processor = strtok(NULL, ":\n");
			if(isIntel){
				strtok(processor, "@");
				char * freq = strtok(NULL, "@");
				snprintf(p.processorType, 64, "%s", processor+1);
				p.GHz = strtof(freq, NULL);
			}
			else{
				snprintf(p.processorType, 64, "%s", processor+1);
				p.GHz = 0.0;
			}
			break;
		}
	}
	fclose(cpuinfo);
	return p;
}

void dumpStats(int exitCode) {
/* 2019-05-28 by ASG. dump-stats gets the rusage info and dumps it to a file.
	Note that it is using global variables. This is unfortunate, but the 
	only way to get the data into a signal handler.
*/
	struct rusage usage;
	getrusage(RUSAGE_CHILDREN,&usage);
	gettimeofday(&stop, NULL);

	procInfo p = getProcInfo();
	writeExitResults(CL->getResourceUsageFile(CL),
				autorelease(createExitResults(exitCode,
				(double)usage.ru_utime.tv_sec + (double)usage.ru_utime.tv_usec / (double) 1000000,
				(double)usage.ru_stime.tv_sec + (double)usage.ru_stime.tv_usec / (double) 1000000,
				(double)(stop.tv_sec - start.tv_sec) + (double)(stop.tv_usec - start.tv_usec) / (double) 1000000,
				(long long)usage.ru_maxrss * 1024,
				p.processorType)));
}

void sig_handler(int signo)
{
	if (signo == SIGTERM) {
		// LAK: we write out a preliminary dumpStats json file. The reason why we do this is in case our child does not terminate or respond to the SIGTERM, we do not lose the wallclock time.
		// However, we will NOT get a correct exit code, system/user time, or max memory usage unless the post child termination dumpStats also runs.
		dumpStats(143);
		externalKill = 1;
		kill(pid, SIGTERM);
	}
}

int wrapJob(CommandLine *commandLine)
{
	int exitCode;

	FuseMounter *mounter = NULL;
	FuseMount *mount = NULL;
	char **cmdLine;
	CL=commandLine;

	if (signal(SIGTERM, sig_handler) == SIG_ERR)
  		fprintf(stderr, "Can't catch SIGTERM\n");

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

	// //LAK: Start up BES communication
	connectionSetup();

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
		
		// 2020-07-22 by JAA -- wait for cgroup to be setup
		// this allows all children off of our child to be
		// contained in freezer
		raise(SIGSTOP);

		execvp(cmdLine[0], cmdLine);

		/* If we got this far, the something went wrong with the exec. */
		fprintf(stderr, "Unable to execute command \"%s\".\n",
			cmdLine[0]);
		release(cmdLine);
		return -1;
	}

	release(cmdLine);

	dumpStats(228); //create empty rusage file

	// 2020-07-22 by JAA -- create cgroup using secondary program
	// should be set up in path, requires sudoers setup if not in same group
	char freezeCMD[50] = "sudo freeze create ";
	// PID to char*, buffer is size of length of pid_max
	// We'll say 4194304 possible PIDs on 64-bit, so length is 7+1
	char pidStr[8]; 
	sprintf(pidStr, "%d", pid);
	// Call "freeze" program with the PID and create param.
	strcat(freezeCMD, pidStr);
	system(freezeCMD);
	// Wait for child to signal if it hasn't already
	waitpid(pid, NULL, WUNTRACED);
	// Signal child to continue
	kill(pid, SIGCONT);

	waitpid(pid, &exitCode, WUNTRACED);

	if (WIFSIGNALED(exitCode))
		exitCode = 128 + WTERMSIG(exitCode);
	else
		exitCode = WEXITSTATUS(exitCode);

	if(externalKill && exitCode == 0)
	{
		exitCode = 143; //set to be consistent with bash early termination exit code
	}

	dumpStats(exitCode);

	pthread_cancel(bes_conn_pthread);

	pthread_join(bes_conn_pthread, NULL);

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

	results->toJson(results, out);
	fclose(out);
	// Begin new code
	// path has the following form /path-stuff/JWD/rusage.json
	//	/A/JWD/C
	//===== New code 2020-04-16
	// The path is of the form /<path prefix>/<job-dir-name>/rusage.json
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
	out = fopen(buf, "w+");
	if (!out)
	{
		fprintf(stderr, "Unable to open resource usage file \"%s\".\n",
			buf);
	}

	results->toJson(results, out);
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

//configuration about how to setup listerner
static struct BESListenerParameters
{
    char hostmask[128];
    int port;
} bes_listener_params;

//network information of the BES and our own listener information
static struct BESConnectionParameters
{
    char host[128];
    int port;
} bes_conn_params;

static char nonce[64];
static int bes_listen_socket = -1;
static int bes_send_socket = -1;

static sem_t port_sem;

void *_startBesListenerThread(void *arg)
{
	printf("starting bes listener\n");

    // Open the main listener socket
    struct sockaddr_in sa;
    bes_listen_socket = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (bes_listen_socket == -1)
	{
        printf("bes_listener: cannot create socket");
		sem_post(&port_sem);
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

	// Bind to port
    if(bind(bes_listen_socket, (struct sockaddr *)&sa, sizeof sa) == -1)
	{
        printf("bes_listener: bind failed");
        close(bes_listen_socket);
		sem_post(&port_sem);
		return NULL;
    }

	// Listen for connections
    if(listen(bes_listen_socket, 10) == -1)
	{
        perror("bes_listener: listen failed");
        close(bes_listen_socket);
    }

	sem_post(&port_sem);

	struct sockaddr_in client_addr;
    socklen_t client_addr_size = sizeof(client_addr);
	while (1)
	{
    	int connectfd = accept(bes_listen_socket, (struct sockaddr *)&client_addr,
			&client_addr_size);

		if(connectfd < 0)
		{
    	    perror("bes_listener: accept failed");
    	    close(connectfd);
    	}

    	char ip[INET_ADDRSTRLEN];
    	inet_ntop(AF_INET, &client_addr, ip, client_addr_size);
		printf("bes_listener: established connection to %s:%hu\n",
    	    ip, ntohs(client_addr.sin_port));

    	// reads
    	char buffer[256];
		memset(&buffer, 0, 256);
		int numread = read(connectfd, buffer, 256);
		printf("bes_listener: got string %s\n", buffer);

		//check if freeze command
		char command[256];
		memset(&command, 0, 256);
		snprintf(command, 256, "%s freeze\n", nonce);
		if(strncmp(buffer, command, 256) == 0)
		{
			//this is a freeze command
			printf("this is a freeze command\n");
			freeze();
		}
		memset(&command, 0, 256);
		snprintf(command, 256, "%s thaw\n", nonce);
		if(strncmp(buffer, command, 256) == 0)
		{
			//this is a thaw command
			printf("this is a thaw command\n");
			thaw();
		}

		//clear buffer
		memset(&command, 0, 256);
		snprintf(command, 256, "%s OK\n", nonce);
		write(connectfd, command, 256);

		printf("bes_listener: closing connection socket...\n");
    	close(connectfd);
	}
	close(bes_listen_socket);

	return NULL;
}

void _pipefail(int sig)
{
    fprintf(stderr, "bes_connection: pipefail -- not exiting\n");
}

int _readBesInfo()
{
	FILE *bes_info_file;
	char buff[256];
	memset(&buff, 0, 256);

	bes_info_file = fopen(".bes-info", "r");

	fgets(buff, 256, bes_info_file);

	//parse file
	char *search_ptr;
	char *ip = strtok_r(buff, ":\n", &search_ptr);
	strncpy(bes_conn_params.host, ip, 128);
	int port = (int)strtol(strtok_r(NULL, ":\n", &search_ptr), NULL, 10);
	bes_conn_params.port = port;

	//get second line
	memset(&buff, 0, 256);
	fgets(buff, 256, bes_info_file);
	fclose(bes_info_file);

	//read in nonce
	strncpy(nonce, strtok(buff, "\n"), 64);

	if (port == 0)
	{
		printf("set_bes_ip_info: Failed to parse port number\n");
		return -1;
	}

	if (ip == NULL)
	{
        printf("set_bes_ip_info: Failed to parse IP address from bes info file\n");
		return -1;
	}
	
	printf("b.host is %s\n", bes_conn_params.host);
	printf("b.port is %d\n", bes_conn_params.port);
	printf("nonce is %s\n", nonce);
}

int _setLocalIPInfo()
{
	strncpy(bes_listener_params.hostmask, "0.0.0.0", 128); // default
	bes_listener_params.port = 0; //random port
	return 0;
}

int _registerWithBes(int port)
{
	char cmd[256];
	memset(&cmd, 0, 256);
	snprintf(cmd, 256, "register %d", port);
	return sendBesMessage(cmd);
}

int _startBesListener()
{
    printf("bes_listener: spawning bes listerner thread\n");
	_setLocalIPInfo();
    signal(SIGPIPE, &_pipefail);
    int err = pthread_create(&bes_conn_pthread, NULL, &_startBesListenerThread, NULL);
    if(err)
	{
        fprintf(stderr, "bes_listener: spawning bes listerner thread failed: %d\n", err);
        bes_conn_pthread = 0;
        return -1;
    }
    return 0;
}

int connectionSetup()
{
	int fd = open("pwrapper_out.txt", O_RDWR | O_CREAT, S_IRUSR | S_IWUSR);
	dup2(fd, STDERR_FILENO);
	dup2(fd, STDOUT_FILENO);

	//this is called once from the main thread, so this is ok
	sem_init(&port_sem, 0, 0);

	_readBesInfo();
	_startBesListener();

	sem_wait(&port_sem);

	//now get our information
	struct sockaddr_in sa;
	int addrlen = sizeof(sa);
	getsockname(bes_listen_socket, (struct sockaddr *)&sa, &addrlen);
	int port = ntohs(sa.sin_port);
	char ip[INET_ADDRSTRLEN];
	inet_ntop(AF_INET, &(sa.sin_addr), ip, INET_ADDRSTRLEN);

	_registerWithBes(port);

	sem_destroy(&port_sem);
}

int sendBesMessage(const char* message)
{
	struct sockaddr_in addr;
    int port = bes_conn_params.port;
    const char *ip = bes_conn_params.host;
    memset(&addr, 0, sizeof(struct sockaddr_in));
    inet_pton(AF_INET, ip, &addr.sin_addr);
    addr.sin_port = htons(port);
    addr.sin_family = AF_INET;

	//if socket hasn't already been created, create
	if (bes_send_socket == -1)
	{
		// Open the main outcall socket
    	struct sockaddr_in sa;
    	bes_send_socket = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
   		if (bes_send_socket == -1)
		{
			printf("sendBesMessage: cannot create socket");
			return -1;
    	}

		// Allow re-use of the port
    	const static int tr = 1;
    	setsockopt(bes_listen_socket,SOL_SOCKET,SO_REUSEADDR,&tr,sizeof(int));
	}

    if(connect(bes_send_socket, (const struct sockaddr *)&addr, sizeof(struct sockaddr_in)) < 0)
	{
        fprintf(stderr, "sendBesMessage: could not connect to %s:%d with error: %s\n", ip, port, strerror(errno));
        return -1;
    }

	if (snprintf(NULL, 0, "%s %s\n", nonce, message) >= 255)
	{
		fprintf(stderr, "sendBesMessage: message is too long for 256 byte buffer.\n");
		return -1;
	}

	char command[256];
	memset(&command, 0, 256);
	snprintf(command, 256, "%s %s\n", nonce, message);
	printf("sendBesMessage: sending %s to %s:%d\n", command, ip, port);
	write(bes_send_socket, command, strlen(command));

	memset(&command, 0, 256);
	read(bes_send_socket, command, 256);

	printf("got back from bes: %s\n", command);

	char okMess[64];
	memset(&okMess, 0, 64);
	snprintf(okMess, 64, "%s %s\n", nonce, "OK");
	if (strcmp(command, okMess) != 0)
	{
		fprintf(stderr, "sendBesMessage: response to command did not match expected acknowledgement. message: %s vs expected: %s\n", command, okMess);
	}

	close(bes_send_socket);

	return 0;
}

void freeze()
{
	// 2020-07-22 by JAA -- freeze process's cgroup
	// should be set up at fork
	char freezeCMD[50] = "sudo freeze freeze ";
	// PID to char*, buffer is size of length of pid_max
	// We'll say 4194304 possible PIDs on 64-bit, so length is 7+1
	char pidStr[8]; 
	sprintf(pidStr, "%d", pid);
	// Call "freeze" program with the PID and create param.
	strcat(freezeCMD, pidStr);
	system(freezeCMD);
	return;
}

void thaw()
{
	// 2020-07-22 by JAA -- thaws process's cgroup
	// should be set up at fork
	char freezeCMD[50] = "sudo freeze thaw ";
	char pidStr[8]; 
	sprintf(pidStr, "%d", pid);
	strcat(freezeCMD, pidStr);
	system(freezeCMD);
	return;
}