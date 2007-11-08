#ifndef __TESTING_CLIENT_HPP__
#define __TESTING_CLIENT_HPP__

#include "jcomm/Socket.hpp"
#include "jcomm/IOException.hpp"
#include "jcomm/OGRSHException.hpp"

namespace jcomm
{
	class TestingClient
	{
		private:
			Socket _socket;

		public:
			TestingClient(Socket socket);

			int OGRSHAdder(int a, int b)
				throw (OGRSHException, IOException);
			float OGRSHDivide(int a, int b)
				throw (OGRSHException, IOException);
	};
}

#endif
