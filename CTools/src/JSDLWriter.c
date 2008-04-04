#include <stdio.h>

#include "JobDescription.h"
#include "JSDLWriter.h"

static const char *JSDL_NS = "http://schemas.ggf.org/jsdl/2005/11/jsdl";
static const char *OGRSH_NS = "http://vcgr.cs.virginia.edu/ogrsh";

static void writeJobIdentification(FILE *stream, const char *jobName);
static int writeApplication(FILE *stream, JobDescription jDesc,
	applicationWriterType appWriter);
static void writeResources(FILE *stream, const char *ogrsh);
static void writeDataStage(FILE *stream, const char *filename,
	const char *stageIn, const char *stageOut);

int writeJSDL(FILE *stream, JobDescription jDesc,
	applicationWriterType appWriter)
{
	int ret;
	PairListIterator iter;
	StringPair pair;

	fprintf(stream, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	fprintf(stream, "<jsdl:JobDefinition xmlns=\"http://www.example.org/\"\n");
	fprintf(stream, "\txmlns:jsdl=\"%s\"\n", JSDL_NS);
	fprintf(stream, "\txmlns:ogrsh=\"%s\">\n", OGRSH_NS);
	fprintf(stream, "\n");
	fprintf(stream, "\t<jsdl:JobDescription>\n");

	writeJobIdentification(stream, jDesc->getJobName(jDesc));
	ret = writeApplication(stream, jDesc, appWriter);
	if (ret != 0)
		return  ret;
	writeResources(stream, jDesc->getRequiredOGRSHVersion(jDesc));

	iter = jDesc->getStageInIterator(jDesc);
	while (iter->hasNext(iter))
	{
		pair = iter->next(iter);
		writeDataStage(stream, pair->getFirst(pair),
			pair->getSecond(pair), NULL);
	}
	iter->destroy(iter);

	iter = jDesc->getStageOutIterator(jDesc);
	while (iter->hasNext(iter))
	{
		pair = iter->next(iter);
		writeDataStage(stream, pair->getFirst(pair),
			NULL, pair->getSecond(pair));
	}
	iter->destroy(iter);

	fprintf(stream, "\t</jsdl:JobDescription>\n");
	fprintf(stream, "</jsdl:JobDefinition>");

	return 0;
}

void writeJobIdentification(FILE *stream, const char *jobName)
{
	if (jobName == NULL)
		return;

	fprintf(stream, "\t\t<jsdl:JobIdentification>\n");
	fprintf(stream, "\t\t\t<jsdl:JobName>%s</jsdl:JobName>\n", jobName);
	fprintf(stream, "\t\t</jsdl:JobIdentification>\n");
	fprintf(stream, "\n");
}

int writeApplication(FILE *stream, JobDescription jDesc,
	applicationWriterType appWriter)
{
	int ret;

	fprintf(stream, "\t\t<jsdl:Application>\n");
	ret = appWriter(stream, jDesc);
	if (ret != 0)
		return ret;
	fprintf(stream, "\t\t</jsdl:Application>\n");

	return 0;
}

void writeResources(FILE *stream, const char *ogrsh)
{
	if (ogrsh == NULL)
		return;

	fprintf(stream, "\t\t<jsdl:Resources>\n");
	fprintf(stream, "\t\t\t<ogrsh:OGRSHVersion>%s</ogrsh:OGRSHVersion>\n",
		ogrsh);
	fprintf(stream, "\t\t</jsdl:Resources>\n");
}

void writeDataStage(FILE *stream, const char *filename,
	const char *stageIn, const char *stageOut)
{
	fprintf(stream, "\t\t<jsdl:DataStaging>\n");
	fprintf(stream, "\t\t\t<jsdl:FileName>%s</jsdl:FileName>\n", filename);
	fprintf(stream, "\t\t\t<jsdl:CreationFlag>overwrite</jsdl:CreationFlag>\n");
	fprintf(stream,
		"\t\t\t<jsdl:DeleteOnTermination>true</jsdl:DeleteOnTermination>\n");
	if (stageIn)
	{
		fprintf(stream, "\t\t\t<jsdl:Source>\n");
		fprintf(stream, "\t\t\t\t<jsdL:URI>%s</jsdl:URI>\n", stageIn);
		fprintf(stream, "\t\t\t</jsdl:Source>\n");
	} else if (stageOut)
	{
		fprintf(stream, "\t\t\t<jsdl:Target>\n");
		fprintf(stream, "\t\t\t\t<jsdL:URI>%s</jsdl:URI>\n", stageOut);
		fprintf(stream, "\t\t\t</jsdl:Target>\n");
	}
	fprintf(stream, "\t\t</jsdl:DataStaging>\n");
}
