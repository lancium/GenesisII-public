#include <stdio.h>
#include <stdlib.h>

#include "fib.h"
#include "fileReader.h"

static void writeFile(char *filename, int value);

int main(int argc, char **argv)
{
	int sequenceNumber, fib;

	if (argc != 3)
	{
		fprintf(stderr, "USAGE:  %s <sequence-file-1> <out-file>\n",
			argv[0]);
		return 1;
	}

	sequenceNumber = readFile(argv[1]);

	fib = doFib(sequenceNumber);

	fprintf(stdout, "Fib[%d]:  %d\n", sequenceNumber, fib);
	writeFile(argv[2], fib);

	return 0;
}

void writeFile(char *filename, int value)
{
	FILE *file;
	
	file = fopen(filename, "w");
	if (!file)
	{
		fprintf(stderr, "Unable to open file \"%s\".\n", filename);
		exit(1);
	}

	fprintf(file, "%d", value);
	fclose(file);
}
