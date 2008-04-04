#include <stdio.h>

#include "JobDescription.h"
#include "HPCAppWriter.h"
#include "PairList.h"

static const char *HPC_NS = "http://schemas.ggf.org/jsdl/2006/07/jsdl-hpcpa";

static void writeArguments(FILE *stream, JobDescription jDesc);
static void writeEnvironment(FILE *stream, JobDescription jDesc);

int hpcAppWriter(FILE *stream, JobDescription jDesc)
{
	const char *tmp;

	fprintf(stream, "\t\t\t<hpcpa:HPCProfileApplication\n");
	fprintf(stream, "\t\t\t\txmlns:hpcpa=\"%s\">\n", HPC_NS);
	fprintf(stream, "\t\t\t\t<hpcpa:Executable>%s</hpcpa:Executable>\n",
		jDesc->getExecutable(jDesc));
	
	tmp = jDesc->getStdinRedirect(jDesc);
	if (tmp != NULL)
		fprintf(stream, "\t\t\t\t<hpcpa:Input>%s</hpcpa:Input>\n", tmp);

	tmp = jDesc->getStdoutRedirect(jDesc);
	if (tmp != NULL)
		fprintf(stream, "\t\t\t\t<hpcpa:Output>%s</hpcpa:Output>\n", tmp);

	tmp = jDesc->getStderrRedirect(jDesc);
	if (tmp != NULL)
		fprintf(stream, "\t\t\t\t<hpcpa:Error>%s</hpcpa:Error>\n", tmp);

	writeArguments(stream, jDesc);
	writeEnvironment(stream, jDesc);

	fprintf(stream, "\t\t\t</hpcpa:HPCProfileApplication>\n");
	return 0;
}

void writeArguments(FILE *stream, JobDescription jDesc)
{
	PairListIterator iter;

	iter = jDesc->getArguments(jDesc);
	while (iter->hasNext(iter))
	{
		StringPair pair = iter->next(iter);
		fprintf(stream, "\t\t\t\t<hpcpa:Argument>%s</hpcpa:Argument>\n",
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
			"\t\t\t\t<hpcpa:Environment name=\"%s\">%s</hpcpa:Environment>\n",
			pair->getFirst(pair), pair->getSecond(pair));
	}
	iter->destroy(iter);
}
