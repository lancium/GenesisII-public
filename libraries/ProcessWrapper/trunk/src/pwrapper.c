#include <stdio.h>
#include <stdlib.h>

#define VM_DEV_ENVIRONMENT 0

#if VM_DEV_ENVIRONMENT
	#include <fcntl.h>
#endif

#include "Memory.h"
#include "StringFunctions.h"
#include "CommandLine.h"
#include "OSSpecific.h"

const char* version="Version 1.6";

static void usage(const char *program);

int main(int argc, char **argv)
{
	#if VM_DEV_ENVIRONMENT
		int errorfd = open("/home/dev/pwrapper_error.txt", O_WRONLY|O_CREAT, 0666);
		dup2(errorfd, STDERR_FILENO);

		int stdoutfd = open("/home/dev/pwrapper_out.txt", O_WRONLY|O_CREAT, 0666);
		dup2(stdoutfd, STDOUT_FILENO);
	#endif

	AutoreleasePool pool;
	CommandLine *commandLine;
	int exitCode;

	if ((argc==2) && strcmp(argv[1],"--version")==0) {
		printf("%s\n", version);
		return 0;
	}
	if (argc < 2)
		usage(argv[0]);

	pool = createAutoreleasePool();
	commandLine = createCommandLineFromArguments(argc - 1, argv + 1);
	if (!commandLine)
		usage(argv[0]);

	if (!commandLine->getExecutable(commandLine))
	{
		if (!commandLine->getGridMountPoint(commandLine))
			usage(argv[0]);
	}

	exitCode = wrapJob(commandLine);

	release(commandLine);
	release(pool);

	return exitCode;
}

void usage(const char *program)
{
	// Dummy comment
	fprintf(stderr,
		"USAGE:  %s [setup-info] <executable> [<args...>]\n"
		"            OR\n"
		"        %s -g<grid-mount>\n"
		"	     OR\n"
		"	 %s --version",
		program, program,program);
	fprintf(stderr, "\n");
	fprintf(stderr, "    WHERE setup-info includes:\n");
	fprintf(stderr, "        -Dvar=value any number of times to override "
		"environment variables.\n");
	fprintf(stderr, "        -g<grid-mount> the path to a directory onto which "
		"we will mount the grid file system.\n");
	fprintf(stderr, "        -U<resource-usage-file> the path to a file "
		"that will receive usage info.\n");
	fprintf(stderr, "        -d<working-dir> the path to change to before "
		"executing the job.\n");
	fprintf(stderr, "        -i<input-file> the path to a file from which "
		"standard in is read.\n");
	fprintf(stderr, "        -o<output-file> the path to a file into which "
		"standard out is placed.\n");
	fprintf(stderr, "        -e<error-file> the path to a file into which "
		"standard error is placed.\n");
	fprintf(stderr, "        -R Is a restart from persist job.\n");

	exit(-1);
}
