#include "fib.h"
#include "fileReader.h"

int doFib(int sequenceNumber)
{
	if (sequenceNumber == 0)
		return readFile("zero.dat");
	if (sequenceNumber == 1)
		return readFile("one.dat");

	return doFib(sequenceNumber - 1) + doFib(sequenceNumber - 2);
}
