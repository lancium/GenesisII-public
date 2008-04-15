#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include "StringUtils.h"
#include "JobDescription.h"
#include "POSIXAppWriter.h"
#include "HPCAppWriter.h"
#include "JSDLWriter.h"

static void usage(const char *progName, int doExit);

typedef const char* (*optionParserFunction)(
	JobDescription, const char *optionValue);

typedef struct OptionDescriptionType
{
	const char *optionPrefix;
	optionParserFunction function;
} OptionDescriptionType;

static const unsigned int BUFFER_SIZE = 1024 * 8;
static void copy(FILE *input, FILE *output);
static int skipJSDLHeader(FILE *input);
static int doMerge(const char *progName, int argc, char **argv);
static int doCreate(const char *progName, int argc, char **argv);

static int mergeInFile(FILE *output, const char *inputFile);

static const char* setJSDLApplication(JobDescription, const char *optionValue);
static const char* setJobName(JobDescription, const char *optionValue);
static const char* setOGRSHVersion(JobDescription, const char *optionValue);
static const char* setStdin(JobDescription, const char *optionValue);
static const char* setStdout(JobDescription, const char *optionValue);
static const char* setStderr(JobDescription, const char *optionValue);
static const char* setStagein(JobDescription, const char *optionValue);
static const char* setStageout(JobDescription, const char *optionValue);
static const char* setEnvironmentVariable(JobDescription,
	const char *optionValue);

static OptionDescriptionType options[] =
{
	{ "--jsdl-application=", setJSDLApplication },
	{ "--job-name=", setJobName },
	{ "--ogrsh-version=", setOGRSHVersion },
	{ "--stdin=", setStdin },
	{ "--stdout=", setStdout },
	{ "--stderr=", setStderr },
	{ "--stage-in=", setStagein },
	{ "--stage-out=", setStageout },
	{ "--D", setEnvironmentVariable }
};

static applicationWriterType appWriter = posixAppWriter;

int main(int argc, char **argv)
{
	if (argc < 2)
		usage(argv[0], 1);

	if (strcmp(argv[1], "--merge") == 0)
		return doMerge(argv[0], argc - 2, argv + 2);
	else
		return doCreate(argv[0], argc - 1, argv + 1);

	return 0;
}

int doCreate(const char *progName, int argc, char **argv)
{
	int ret;
	int option;
	int opDesc;
	JobDescription jDesc;
	const char *msg;
	int length;
	int handled;
	const char *jsdlFile;
	FILE *stream;

	if (argc < 2)
		usage(progName, 1);

	jsdlFile = argv[0];

	jDesc = createJobDescription();
	for (option = 1; option < argc; option++)
	{
		handled = 0;
		for (opDesc = 0;
			opDesc < (sizeof(options) / sizeof(OptionDescriptionType));
			opDesc++)
		{
			OptionDescriptionType oDescription = options[opDesc];
			length = strlen(oDescription.optionPrefix);
			if (strncmp(oDescription.optionPrefix, argv[option], length) == 0)
			{
				msg = oDescription.function(jDesc, argv[option] + length);
				if (msg != NULL)
				{
					fprintf(stderr, "Parse Error!  %s\n", msg);
					jDesc->destroy(jDesc);
					return 1;
				}

				handled = 1;
				break;
			}
		}

		if (handled == 0)
			break;
	}

	if (option == argc)
	{
		jDesc->destroy(jDesc);
		fprintf(stderr, "Missing executable!\n");
		usage(progName, 1);
	}

	jDesc->setExecutable(jDesc, argv[option]);
	for (option = option + 1; option < argc; option++)
		jDesc->addArgument(jDesc, argv[option]);

	msg = jDesc->verify(jDesc);
	if (msg != NULL)
	{
		fprintf(stderr, "Parse Error!  %s\n", msg);
		jDesc->destroy(jDesc);
		return 1;
	}

	stream = fopen(jsdlFile, "wt");
	if (stream == NULL)
	{
		fprintf(stderr, "Unable to open jsdl file %s for output.\n", jsdlFile);
		jDesc->destroy(jDesc);
		return 1;
	}
	ret = writeJSDL(stream, jDesc, appWriter);	
	fclose(stream);

	if (ret != 0)
		unlink(jsdlFile);

	jDesc->destroy(jDesc);

	return ret;
}

int doMerge(const char *progName, int argc, char **argv)
{
	FILE *output;
	int lcv;
	int ret;

	if (argc < 2)
		usage(progName, 1);

	output = fopen(argv[0], "wt");
	if (output == NULL)
	{
		fprintf(stderr, "Unable to open output file \"%s\"\n", argv[0]);
		return 1;
	}

	fprintf(output, "<jsdl-merge>\n");
	for (lcv = 1; lcv < argc; lcv++)
	{
		ret = mergeInFile(output, argv[lcv]);
		if (ret != 0)
		{
			fclose(output);
			unlink(argv[0]);
			return 1;
		}
		fprintf(output, "\n");
	}

	fprintf(output, "</jsdl-merge>\n");
	fclose(output);
	return 0;
}

void usage(const char *progName, int doExit)
{
	fprintf(stderr,
		"USAGE:  %s --merge <merge-target> <input-jsdl1>...<input-jsdln>\n"
		"\t\tOR\n"
		"\t%s <jsdl-file> [--jsdl--application={posix | hpc}]\n"
		"\t\t[--job-name=<name>] [run-options] <executable> [program args]\n\n"
		"\tWHERE run-options are:\n"
		"\t\t--ogrsh-version={x86 | x86-64}\n"
		"\t\t--stdin=<in-filename>\n"
		"\t\t--stdout=<out-filename>\n"
		"\t\t--stderr=<err-filename>\n"
		"\t\t--stage-in=filename/uri\n"
		"\t\t--stage-out=filename/uri\n"
		"\t\t--Denvironment-variable=variable-value\n",
		progName, progName);
	if (doExit)
		exit(1);
}

const char* setJSDLApplication(JobDescription jDesc, const char *optionValue)
{
	if (strcmp(optionValue, "posix") == 0)
	{
		appWriter = posixAppWriter;
	} else if (strcmp(optionValue, "hpc") == 0)
	{
		appWriter = hpcAppWriter;
	} else
		return "jsdl-application MUST be one of posix or hpc.";

	return NULL;
}

const char* setJobName(JobDescription jDesc, const char *optionValue)
{
	jDesc->setJobName(jDesc, optionValue);
	return NULL;
}

const char* setOGRSHVersion(JobDescription jDesc, const char *optionValue)
{
	if (strcmp(optionValue, "x86") && strcmp(optionValue, "x86-64"))
		return "OGRSH Version MUST be one of x86, or x86-64.";

	jDesc->setRequiredOGRSHVersion(jDesc, optionValue);
	return NULL;
}

const char* setStdin(JobDescription jDesc, const char *optionValue)
{
	jDesc->setStdinRedirect(jDesc, optionValue);
	return NULL;
}

const char* setStdout(JobDescription jDesc, const char *optionValue)
{
	jDesc->setStdoutRedirect(jDesc, optionValue);
	return NULL;
}

const char* setStderr(JobDescription jDesc, const char *optionValue)
{
	jDesc->setStderrRedirect(jDesc, optionValue);
	return NULL;
}

const char* setStagein(JobDescription jDesc, const char *optionValue)
{
	char *duplicate;
	char *secondHalf;

	duplicate = duplicateString(optionValue);

	for (secondHalf = duplicate; *secondHalf != (char)0; secondHalf++)
	{
		if (*secondHalf == '/')
		{
			*secondHalf = (char)0;
			secondHalf++;
			break;
		}
	}

	if (*secondHalf == (char)0)
		return "Data stage option not in valid format.";

	jDesc->addStageIn(jDesc, duplicate, secondHalf);
	free(duplicate);
	return NULL;
}

const char* setStageout(JobDescription jDesc, const char *optionValue)
{
	char *duplicate;
	char *secondHalf;

	duplicate = duplicateString(optionValue);

	for (secondHalf = duplicate; *secondHalf != (char)0; secondHalf++)
	{
		if (*secondHalf == '/')
		{
			*secondHalf = (char)0;
			secondHalf++;
			break;
		}
	}

	if (*secondHalf == (char)0)
		return "Data stage option not in valid format.";

	jDesc->addStageOut(jDesc, duplicate, secondHalf);
	free(duplicate);
	return NULL;
}

const char* setEnvironmentVariable(JobDescription jDesc,
	const char *optionValue)
{
	char *duplicate;
	char *secondHalf;

	duplicate = duplicateString(optionValue);

	for (secondHalf = duplicate; *secondHalf != (char)0; secondHalf++)
	{
		if (*secondHalf == '=')
		{
			*secondHalf = (char)0;
			secondHalf++;
			break;
		}
	}

	if (*secondHalf == (char)0)
		return "Environment variable option not in valid format.";

	jDesc->addEnvironmentPair(jDesc, duplicate, secondHalf);
	free(duplicate);
	return NULL;
}

int mergeInFile(FILE *output, const char *inputFile)
{
	FILE *input;
	int c;
	int lastChar = EOF;
	int ret;

	input = fopen(inputFile, "rt");
	if (input == NULL)
	{
		fprintf(stderr, "Unable to open input file \"%s\".\n", inputFile);
		return 1;
	}

	while ( (c = fgetc(input)) != EOF)
	{
		if (lastChar == EOF)
		{
			lastChar = c;
			continue;
		}

		if (isspace(c))
		{
			lastChar = c;
			continue;
		}

		if (lastChar == '<' && c == '?')
		{
			ret = skipJSDLHeader(input);
			if (ret != 0)
				return ret;
		} else
		{
			if (lastChar != EOF && !isspace(lastChar))
				fprintf(output, "%c", lastChar);
			fprintf(output, "%c", c);
		}

		break;
	}

	copy(input, output);

	fclose(input);
	return 0;
}

int skipJSDLHeader(FILE *input)
{
	int lastChar = EOF;
	int c;

	while (1)
	{
		if ( (c = fgetc(input)) == EOF)
		{
			fprintf(stderr, "Unable to parse input JSDL file.\n");
			return 1;
		}

		if (lastChar == EOF)
		{
			lastChar = c;
			continue;
		}

		if ((lastChar == '?') && (c == '>'))
			return 0;
		lastChar = c;
	}
}

void copy(FILE *input, FILE *output)
{
	void *data;
	size_t read;

	data = malloc(BUFFER_SIZE);

	while (1)
	{
		read = fread(data, 1, BUFFER_SIZE, input);
		fwrite(data, 1, read, output);
		if (read < BUFFER_SIZE)
			if (feof(input))
				break;
	}

	free(data);
}
