#include "jcomm/TestingClient.hpp"

using namespace jcomm;

int main(int argc, char **argv)
{
	fprintf(stderr, "Sizeof(long) = %d\n", sizeof(long));
	fprintf(stderr, "Sizeof(long long) = %d\n", sizeof(long long));
	if (argc != 4)
	{
		fprintf(stderr, "USAGE:  %s <host> <port> <secret>\n", argv[0]);
		return 1;
	}

	Socket *socket = new Socket(argv[1], atoi(argv[2]), argv[3]);
	TestingClient client(*socket);

	fprintf(stderr, "7 + 42 is %d\n", client.OGRSHAdder(7, 42));
	fprintf(stderr, "7 / 42 is %f\n", client.OGRSHDivide(7, 42));
	fprintf(stderr, "7 / 0 is %f\n", client.OGRSHDivide(7, 0));
	delete socket;

	return 0;
}
