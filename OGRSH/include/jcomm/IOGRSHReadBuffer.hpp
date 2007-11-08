#ifndef __IOGRSH_READ_BUFFER_HPP__
#define __IOGRSH_READ_BUFFER_HPP__

#include <string>

#include "jcomm/IPackable.hpp"

namespace jcomm
{
	class IOGRSHReadBuffer
	{
		public:
			virtual ~IOGRSHReadBuffer();

			virtual IOGRSHReadBuffer& operator= (
				const IOGRSHReadBuffer&);

			virtual void readBoolean(bool&) throw (IOException) = 0;
			virtual void readChar(char&) throw (IOException) = 0;
			virtual void readShort(short&) throw (IOException) = 0;
			virtual void readInt(int&) throw (IOException) = 0;
			virtual void readLongLong(long long&) throw (IOException) = 0;
			virtual void readFloat(float&) throw (IOException) = 0;
			virtual void readDouble(double&) throw (IOException) = 0;

			// Returns true if the object was read, false if it was a null.
			virtual bool readString(std::string &) throw (IOException) = 0;
			virtual bool readPackable(IPackable&) throw (IOException) = 0;

			virtual void readBytes(void **buf, ssize_t &length)
				throw (IOException) = 0;
	};
}

#endif
