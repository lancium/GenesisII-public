#ifndef __IPACKABLE_HPP__
#define __IPACKABLE_HPP__

#include <string>

#include "jcomm/IOException.hpp"

namespace jcomm
{
	class IOGRSHWriteBuffer;
	class IOGRSHReadBuffer;

	class IPackable
	{
		private:
			std::string _typeName;

		public:
			IPackable(const std::string &typeName);
			IPackable(const IPackable &other);

			virtual ~IPackable();

			virtual IPackable& operator= (const IPackable &);

			virtual const std::string& getTypeName() const;

			virtual void pack(IOGRSHWriteBuffer &writeBuffer)
				const throw (IOException) = 0;
			virtual void unpack(IOGRSHReadBuffer &readBuffer)
				throw (IOException) = 0;
	};
}

#endif
