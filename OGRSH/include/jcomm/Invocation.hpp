#ifndef __INVOCATION_HPP__
#define __INVOCATION_HPP__

#include <string>

#include "jcomm/IOGRSHWriteBuffer.hpp"
#include "jcomm/IOGRSHReadBuffer.hpp"
#include "jcomm/Socket.hpp"
#include "jcomm/OGRSHException.hpp"
#include "jcomm/IOException.hpp"

namespace jcomm
{
	class Invocation
	{
		private:
			Socket _socket;
			std::string _invocationName;
			IOGRSHWriteBuffer *_writeBuffer;

			Invocation(const Invocation&);
			Invocation& operator= (const Invocation&);

		public:
			Invocation(Socket socket, const std::string &invocationName);
			~Invocation();

			IOGRSHReadBuffer* invoke() throw (OGRSHException, IOException);

			void addBoolean(bool data);
			void addChar(char data);
			void addShort(short data);
			void addInt(int data);
			void addLongLong(long long data);
			void addFloat(float data);
			void addDouble(double data);
			void addString(const std::string &data);
			void addString(const char *str);
			void addPackable(const IPackable&);
			void addPackable(const IPackable*);
			void addBytes(const void *buf, int length);
	};
}

#endif
