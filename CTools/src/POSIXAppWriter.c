#include <stdio.h>

#include "JobDescription.h"
#include "POSIXAppWriter.h"
#include "PairList.h"

static const char *POSIX_NS = "http://schemas.ggf.org/jsdl/2005/11/jsdl-posix";

static void writeArguments(FILE *stream, JobDescription jDesc);
static void writeEnvironment(FILE *stream, JobDescription jDesc);

int posixAppWriter(FILE *stream, JobDescription jDesc)
{
	const char *tmp;

	fprintf(stream, "\t\t\t<posix:POSIXApplication\n");
	fprintf(stream, "\t\t\t\txmlns:posix=\"%s\">\n", POSIX_NS);
	fprintf(stream, "\t\t\t\t<posix:Executable>%s</posix:Executable>\n",
		jDesc->getExecutable(jDesc));
	
	tmp = jDesc->getStdinRedirect(jDesc);
	if (tmp != NULL)
		fprintf(stream, "\t\t\t\t<posix:Input>%s</posix:Input>\n", tmp);

	tmp = jDesc->getStdoutRedirect(jDesc);
	if (tmp != NULL)
		fprintf(stream, "\t\t\t\t<posix:Output>%s</posix:Output>\n", tmp);

	tmp = jDesc->getStderrRedirect(jDesc);
	if (tmp != NULL)
		fprintf(stream, "\t\t\t\t<posix:Error>%s</posix:Error>\n", tmp);

	writeArguments(stream, jDesc);
	writeEnvironment(stream, jDesc);

	fprintf(stream, "\t\t\t</posix:POSIXApplication>\n");
	return 0;
}

void writeArguments(FILE *stream, JobDescription jDesc)
{
	PairListIterator iter;

	iter = jDesc->getArguments(jDesc);
	while (iter->hasNext(iter))
	{
		StringPair pair = iter->next(iter);
		fprintf(stream, "\t\t\t\t<posix:Argument>%s</posix:Argument>\n",
			pair->getFirst(pair));
	}
	iter->destroy(iter);
}

void writeEnvironment(FILE *stream, JobDescription jDesc)
{
	PairListIterator iter;

	iter = jDesc->getEnvironmentIterator(jDesc);
	while (iter->hasNext(iter))
	{
		StringPair pair = iter->next(iter);
		fprintf(stream,
			"\t\t\t\t<posix:Environment name=\"%s\">%s</posix:Environment>\n",
			pair->getFirst(pair), pair->getSecond(pair));
	}
	iter->destroy(iter);
}
