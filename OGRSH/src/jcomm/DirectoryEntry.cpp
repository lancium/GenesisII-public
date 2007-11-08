#include "jcomm/IPackable.hpp"
#include "jcomm/DirectoryEntry.hpp"

namespace jcomm
{
	DirectoryEntry::DirectoryEntry()
		: IPackable("edu.virginia.vcgr.ogrsh.server.handlers.DirectoryHandler$DirectoryEntry")
	{
		_entryName = "";
		_entryType = 0;
	}

	void DirectoryEntry::pack(IOGRSHWriteBuffer &writeBuffer)
		const throw (IOException)
	{
		writeBuffer.writeString(_entryName);
		writeBuffer.writeInt(_entryType);
	}

	void DirectoryEntry::unpack(IOGRSHReadBuffer &readBuffer)
		throw (IOException)
	{
		readBuffer.readString(_entryName);
		readBuffer.readInt(_entryType);
	}
}
