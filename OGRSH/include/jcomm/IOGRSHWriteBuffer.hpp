#ifndef __IOGRSH_WRITE_BUFFER_HPP__
#define __IOGRSH_WRITE_BUFFER_HPP__

#include <string>

#include "jcomm/IPackable.hpp"
#include "jcomm/ByteBuffer.hpp"

namespace jcomm
{
	class IOGRSHWriteBuffer
	{
		public:
			virtual ~IOGRSHWriteBuffer();

			virtual IOGRSHWriteBuffer& operator= (
				const IOGRSHWriteBuffer&);

			virtual ByteBuffer compact() = 0;

			virtual void writeRaw(char *data, int offset, int length)
				throw (IOException) = 0;
			virtual void writeBoolean(bool b)
				throw (IOException) = 0;
			virtual void writeChar(char c)
				throw (IOException) = 0;
			virtual void writeShort(short int i)
				throw (IOException) = 0;
			virtual void writeInt(int i)
				throw (IOException) = 0;
			virtual void writeLongLong(long long int i)
				throw (IOException) = 0;
			virtual void writeFloat(float f)
				throw (IOException) = 0;
			virtual void writeDouble(double d)
				throw (IOException) = 0;
			virtual void writeString(const std::string &str)
				throw (IOException) = 0;
			virtual void writePackable(const IPackable&)
				throw (IOException) = 0;
			virtual void writePackable(const IPackable*)
				throw (IOException) = 0;

			virtual void writeBytes(const void *buf, int length)
				throw (IOException) = 0;
	};
}

#endif
