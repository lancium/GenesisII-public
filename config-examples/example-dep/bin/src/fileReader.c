#include "fileReader.h"

#include <stdlib.h>
#include <stdio.h>

int readFile(char *filename)
{
	FILE *file;
	int result;

	file = fopen(filename, "r");
	if (!file)
	{
		fprintf(stderr, "Unable to open file \"%s\".\n", filename);
		exit (1);
	}

	if (fscanf(file, "%d", &result) != 1)
	{
		fclose(file);
		fprintf(stderr, "Unable to read integer from file \"%s\".\n",
			filename);
		exit (1);
	}
	fclose(file);

	return result;
}
