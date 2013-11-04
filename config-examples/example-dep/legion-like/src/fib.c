#include "fib.h"
#include "fileReader.h"

int doFib(int sequenceNumber)
{
	if (sequenceNumber == 0)
		return 1;
	if (sequenceNumber == 1)
		return 1;

	return doFib(sequenceNumber - 1) + doFib(sequenceNumber - 2);
}
