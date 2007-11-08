#include <stdio.h>
#include <stdlib.h>

int main(int argc, char **argv)
{
	fprintf(stdout, "Size of char is %ld\n", sizeof(char));
	fprintf(stdout, "Size of short int is %ld\n", sizeof(short int));
	fprintf(stdout, "Size of int is %ld\n", sizeof(int));
	fprintf(stdout, "Size of long int is %ld\n", sizeof(long int));
	fprintf(stdout, "Size of long long int is %ld\n", sizeof(long long int));
	return 0;
}
