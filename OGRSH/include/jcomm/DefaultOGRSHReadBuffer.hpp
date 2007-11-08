#ifndef __IOGRSH_DEFAULT_BUFFER_HPP__
#define __IOGRSH_DEFAULT_BUFFER_HPP__

#include <string>

#include "jcomm/IPackable.hpp"
#include "jcomm/ByteBuffer.hpp"
#include "jcomm/IOGRSHReadBuffer.hpp"

namespace jcomm
{
	class DefaultOGRSHReadBuffer : public IOGRSHReadBuffer
	{
		private:
			ByteBuffer _buffer;

			std::string readUTF() throw (IOException);
			void verifyType(const std::string &expectedType);
			void verifyType(const std::string &seenType,
				const std::string &expectedType);

		public:
			DefaultOGRSHReadBuffer(ByteBuffer &buffer);

			virtual void readBoolean(bool&) throw (IOException);
			virtual void readChar(char&) throw (IOException);
			virtual void readShort(short&) throw (IOException);
			virtual void readInt(int&) throw (IOException);
			virtual void readLongLong(long long&) throw (IOException);
			virtual void readFloat(float&) throw (IOException);
			virtual void readDouble(double&) throw (IOException);

			// Returns true if the object was read, false if it was a null.
			virtual bool readString(std::string &) throw (IOException);
			virtual bool readPackable(IPackable&) throw (IOException);

			virtual void readBytes(void **buf, ssize_t &length)
				throw (IOException);
	};
}

#endif
