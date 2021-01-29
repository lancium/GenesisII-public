#include "CommandLine.h"

#include <regex.h>

#include "Memory.h"
#include "LinkedList.h"
#include "StringFunctions.h"
#include "HashMap.h"

static void commandLineImplDestructor(void *ptr);
static const char* getResourceUsageFileImpl(struct CommandLine*);
static const char* getGridMountPointImpl(struct CommandLine*);
static const char* getWorkingDirectoryImpl(struct CommandLine*);
static const char* getStandardInputImpl(struct CommandLine*);
static const char* getStandardOutputImpl(struct CommandLine*);
static const char* getStandardErrorImpl(struct CommandLine*);
static HashMap* getEnvironmentVariablesImpl(struct CommandLine*);
static const char* getExecutableImpl(struct CommandLine*);
static LinkedList* getArgumentsImpl(struct CommandLine*);
static const int getIsRestartImpl(struct CommandLine*);

typedef struct CommandLineImpl
{
	CommandLine iface;

	char *_resourceUsageFile;

	char *_gridMountPoint;
	char *_workingDirectory;

	char *_standardInput;
	char *_standardOutput;
	char *_standardError;

	//LAK 2021 Jan 27: Added the -R flag to specify that this job is a restart for a persisted job
	int _isRestart;

	HashMap *_environmentVariables;

	char *_executable;
	LinkedList *_arguments;
} CommandLineImpl;

static int parseCommandLine(CommandLineImpl *impl, int argc, char **argv);
static int parseEnvironmentVariable(HashMap *map, const char *text);

CommandLine* createCommandLineFromArguments(int argc, char **argv)
{
	CommandLineImpl *ret;

	ret = (CommandLineImpl*)allocate(sizeof(CommandLineImpl),
		commandLineImplDestructor);

	ret->iface.getResourceUsageFile = getResourceUsageFileImpl;
	ret->iface.getGridMountPoint = getGridMountPointImpl;
	ret->iface.getWorkingDirectory = getWorkingDirectoryImpl;
	ret->iface.getStandardInput = getStandardInputImpl;
	ret->iface.getStandardOutput = getStandardOutputImpl;
	ret->iface.getStandardError = getStandardErrorImpl;
	ret->iface.getEnvironmentVariables = getEnvironmentVariablesImpl;
	ret->iface.getExecutable = getExecutableImpl;
	ret->iface.getArguments = getArgumentsImpl;
	ret->iface.getIsRestart = getIsRestartImpl;

	ret->_resourceUsageFile = NULL;
	ret->_gridMountPoint = NULL;
	ret->_workingDirectory = NULL;
	ret->_standardInput = NULL;
	ret->_standardOutput = NULL;
	ret->_standardError = NULL;
	ret->_environmentVariables = createHashMap(8,
		defaultStringHashFunction, defaultStringEqualsFunction);
	ret->_executable = NULL;
	ret->_arguments = createLinkedList();
	
	//LAK: default to false
	ret->_isRestart = 0;

	if (parseCommandLine(ret, argc, argv))
	{
		release(ret);
		return NULL;
	}

	return (CommandLine*)ret;
}

void commandLineImplDestructor(void *ptr)
{
	CommandLineImpl *impl = (CommandLineImpl*)ptr;

	release(impl->_resourceUsageFile);
	release(impl->_gridMountPoint);
	release(impl->_workingDirectory);
	release(impl->_standardInput);
	release(impl->_standardOutput);
	release(impl->_standardError);
	release(impl->_environmentVariables);
	release(impl->_executable);
	release(impl->_arguments);
}

const char* getResourceUsageFileImpl(struct CommandLine *ptr)
{
	CommandLineImpl *impl = (CommandLineImpl*)ptr;

	return impl->_resourceUsageFile;
}

const char* getGridMountPointImpl(struct CommandLine *ptr)
{
	CommandLineImpl *impl = (CommandLineImpl*)ptr;

	return impl->_gridMountPoint;
}

const char* getWorkingDirectoryImpl(struct CommandLine *ptr)
{
	CommandLineImpl *impl = (CommandLineImpl*)ptr;

	return impl->_workingDirectory;
}

const char* getStandardInputImpl(struct CommandLine *ptr)
{
	CommandLineImpl *impl = (CommandLineImpl*)ptr;

	return impl->_standardInput;
}

const char* getStandardOutputImpl(struct CommandLine *ptr)
{
	CommandLineImpl *impl = (CommandLineImpl*)ptr;

	return impl->_standardOutput;
}

const char* getStandardErrorImpl(struct CommandLine *ptr)
{
	CommandLineImpl *impl = (CommandLineImpl*)ptr;

	return impl->_standardError;
}

HashMap* getEnvironmentVariablesImpl(struct CommandLine *ptr)
{
	CommandLineImpl *impl = (CommandLineImpl*)ptr;

	return impl->_environmentVariables;
}

const char* getExecutableImpl(struct CommandLine *ptr)
{
	CommandLineImpl *impl = (CommandLineImpl*)ptr;

	return impl->_executable;
}

LinkedList* getArgumentsImpl(struct CommandLine *ptr)
{
	CommandLineImpl *impl = (CommandLineImpl*)ptr;

	return impl->_arguments;
}

const int getIsRestartImpl(struct CommandLine *ptr)
{
	CommandLineImpl *impl = (CommandLineImpl*)ptr;

	return impl->_isRestart;
}

int parseStringToBoolean(const char* string)
{
	if(strncmp(string, "true", 4) == 0)
	{
		return 1;
	}
	//anything else we just consider false
	return 0;
}

int parseCommandLine(CommandLineImpl *impl, int argc, char **argv)
{
	while (argc > 0)
	{
		if (startsWith(*argv, "-D"))
		{
			if (parseEnvironmentVariable(impl->_environmentVariables,
				*argv + 2))
				return -1;
		} else if (startsWith(*argv, "-U"))
			impl->_resourceUsageFile = createStringFromCopy(*argv + 2);
		else if (startsWith(*argv, "-g"))
			impl->_gridMountPoint = createStringFromCopy(*argv + 2);
		else if (startsWith(*argv, "-d"))
			impl->_workingDirectory = createStringFromCopy(*argv + 2);
		else if (startsWith(*argv, "-i"))
			impl->_standardInput = createStringFromCopy(*argv + 2);
		else if (startsWith(*argv, "-o"))
			impl->_standardOutput = createStringFromCopy(*argv + 2);
		else if (startsWith(*argv, "-e"))
			impl->_standardError = createStringFromCopy(*argv + 2);
		else if (startsWith(*argv, "-R"))
			impl->_isRestart = parseStringToBoolean(*argv + 2);
		else
			break;

		argc--;
		argv++;
	}

	if (argc == 0)
	{
		if (impl->_gridMountPoint == NULL)
		{
			fprintf(stderr, "No executable specified.\n");
			return -1;
		} else
			return 0;
	}

	//check if the executable is a singularity image; if so, we need to switch to a singularity command
	regex_t regex;
	int reti;
	reti = regcomp(&regex, ".simg$|.sif$|.img$", REG_EXTENDED);
	if (reti)
	{
		fprintf(stderr, "Could not compile regex\n");
	}
	/* Execute regular expression */
	reti = regexec(&regex, *argv, 0, NULL, 0);
	if (!reti)
	{
		// OLD: should build equivalent of : singularity checkpoint job_run <container> <working dir> --nv -c --ipc --pid -B .:/tmp -W /tmp -H /tmp $image $@
		
		if(impl->_isRestart)
		{
			//We are restarting from a persisted job, we should get the image name from the CL and then restart
			impl->_executable = createStringFromCopy("singularity");

			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("checkpoint")));
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("job_restart")));

			//the first argument is the name of the image (executable place), the second is the path to the image. We need to skip over the name and use the path instead.
			argc--;
			argv++;

			//this is the container image path
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy(*argv)));

			//we don't want to add the above argument again, so move the argument pointer forward
			argc--;
			argv++;

			//add the directory name
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy(strrchr(impl->_workingDirectory, '/')+1)));

			//we now throw away all other arguments
			argc = 0;
		}
		else
		{
			//This is NOT a restart, we need to start from scratch
			// NEW: should build equivalent of : singularity checkpoint job_start --nv -c --ipc --pid -B .:/tmp -W /tmp -H /tmp $image $instance_name $@

			impl->_executable = createStringFromCopy("singularity");

			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("checkpoint")));
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("job_run")));

			//impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("--nv")));
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("-c")));
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("--ipc")));
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("--pid")));
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("-B")));
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy(".:/tmp")));
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("-W")));
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("/tmp")));
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("-H")));
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy("/tmp")));

		
			//the first argument is the name of the image (executable place), the second is the path to the image. We need to skip over the name and use the path instead.
			argc--;
			argv++;

			//this is the container image path
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy(*argv)));

			//we don't want to add the above argument again, so move the argument pointer forward
			argc--;
			argv++;

			//add the directory name
			impl->_arguments->addLast(impl->_arguments, autorelease(createStringFromCopy(strrchr(impl->_workingDirectory, '/')+1)));
		}
	}
	else if (reti == REG_NOMATCH) {
		impl->_executable = createStringFromCopy(*argv);
		argc--;
		argv++;
	}
	else {
		fprintf(stderr, "Regex match failed: %s\n", *argv);
	}
	regfree(&regex);

	while (argc > 0)
	{
		impl->_arguments->addLast(impl->_arguments,
			autorelease(createStringFromCopy(*argv)));

		argc--;
		argv++;
	}

	return 0;
}

int parseEnvironmentVariable(HashMap *map, const char *text)
{
	SSIZE_T index = indexOf(text, '=');
	if (index < 0)
	{
		fprintf(stderr, "Invalid environment variable declared:  \"%s\".\n",
			text);
		return -1;
	}

	map->put(map, createStringFromSubstring(text, 0, index),
		createStringFromSubstring(text, index + 1, -1));

	return 0;
}
