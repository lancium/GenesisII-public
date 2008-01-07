#include "jcomm/IPackable.hpp"
#include "jcomm/DirectoryEntry.hpp"

namespace jcomm
{
	DirectoryEntry::DirectoryEntry()
		: IPackable("edu.virginia.vcgr.ogrsh.server.handlers.DirectoryHandler$DirectoryEntry")
	{
		_inode = 0;
		_entryName = "";
		_entryType = 0;
	}

	void DirectoryEntry::pack(IOGRSHWriteBuffer &writeBuffer)
		const throw (IOException)
	{
		writeBuffer.writeLongLong(_inode);
		writeBuffer.writeString(_entryName);
		writeBuffer.writeInt(_entryType);
	}

	void DirectoryEntry::unpack(IOGRSHReadBuffer &readBuffer)
		throw (IOException)
	{
		readBuffer.readLongLong(_inode);
		readBuffer.readString(_entryName);
		readBuffer.readInt(_entryType);
	}
}
