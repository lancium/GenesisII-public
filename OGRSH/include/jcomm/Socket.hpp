#ifndef __OGRSH_SOCKET_HPP__
#define __OGRSH_SOCKET_HPP__

#include "jcomm/IOException.hpp"
#include "jcomm/ByteBuffer.hpp"

namespace jcomm
{
	class Socket
	{
		private:
			int _socket;
			int *_referenceCount;

		public:
			Socket(const std::string &address, short port,
				const std::string &secret) throw (IOException);
			Socket(const Socket&);

			virtual ~Socket() throw (IOException);

			virtual Socket& operator= (const Socket&);

			void write(const char *data, int length) throw (IOException);
			void write(ByteBuffer &bb) throw (IOException);

			void read(char *data, int length) throw (IOException);
			void read(ByteBuffer &bb) throw (IOException);
	};
}

#endif
