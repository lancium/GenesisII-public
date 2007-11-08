#ifndef __DIRECTORY_ENTRY_HPP__
#define __DIRECTORY_ENTRY_HPP__

#include "jcomm/IPackable.hpp"
#include "jcomm/IOGRSHReadBuffer.hpp"
#include "jcomm/IOGRSHWriteBuffer.hpp"

namespace jcomm
{
	class DirectoryEntry : public IPackable
	{
		public:
			DirectoryEntry();

			std::string _entryName;
			int _entryType;

			virtual void pack(IOGRSHWriteBuffer &writeBuffer)
				const throw (IOException);
			virtual void unpack(IOGRSHReadBuffer &readBuffer)
				throw (IOException);
	};
}

#endif
