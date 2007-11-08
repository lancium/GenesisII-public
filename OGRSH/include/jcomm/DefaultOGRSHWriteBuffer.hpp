#ifndef __DEFAULT_OGRSH_WRITE_BUFFER_HPP__
#define __DEFAULT_OGRSH_WRITE_BUFFER_HPP__

#include <list>

#include "jcomm/IOGRSHWriteBuffer.hpp"
#include "jcomm/ByteBuffer.hpp"

namespace jcomm
{
	class DefaultOGRSHWriteBuffer : public IOGRSHWriteBuffer
	{
		private:
			static const int _CAPACITY;
			std::list<ByteBuffer*> _buffers;
			ByteBuffer *_current;

			void ensure(int size);

			DefaultOGRSHWriteBuffer(const DefaultOGRSHWriteBuffer&);
			virtual DefaultOGRSHWriteBuffer& operator= (
				const DefaultOGRSHWriteBuffer&);
		public:
			DefaultOGRSHWriteBuffer();
			virtual ~DefaultOGRSHWriteBuffer();

			virtual ByteBuffer compact();

			virtual void writeBoolean(bool b)
				throw (IOException);
			virtual void writeChar(char c)
				throw (IOException);
			virtual void writeShort(short int i)
				throw (IOException);
			virtual void writeInt(int i)
				throw (IOException);
			virtual void writeLongLong(long long int i)
				throw (IOException);
			virtual void writeFloat(float f)
				throw (IOException);
			virtual void writeDouble(double d)
				throw (IOException);
			virtual void writeString(const std::string &str)
				throw (IOException);
			virtual void writePackable(const IPackable&)
				throw (IOException);
			virtual void writePackable(const IPackable*)
				throw (IOException);
			virtual void writeBytes(const void *buf, int length)
				throw (IOException);

			void writeUTF(const std::string&);
	};
}

#endif
