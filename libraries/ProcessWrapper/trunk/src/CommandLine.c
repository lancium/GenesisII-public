#include "CommandLine.h"

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

typedef struct CommandLineImpl
{
	CommandLine iface;

	char *_resourceUsageFile;

	char *_gridMountPoint;
	char *_workingDirectory;

	char *_standardInput;
	char *_standardOutput;
	char *_standardError;

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

	impl->_executable = createStringFromCopy(*argv);
	argc--;
	argv++;

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
