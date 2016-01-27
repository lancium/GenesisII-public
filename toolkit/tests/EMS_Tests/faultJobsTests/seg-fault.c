#include <stdio.h>
#include <stdlib.h>

int main()
{
	int *i;
	int a = 10;
	*i = a;
	printf("Should not print this, seg fault\n");
}

