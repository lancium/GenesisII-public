#include "jcomm/IPackable.hpp"
#include "jcomm/StatBuffer.hpp"

namespace jcomm
{
	StatBuffer::StatBuffer()
		: IPackable("edu.virginia.vcgr.ogrsh.server.dir.StatBuffer")
	{
		st_ino = 0;
		st_mode = 0;
		st_size = 0;
		st_blocksize = (1024 * 4);
		_st_atime = time(NULL);
		_st_mtime = time(NULL);
		_st_ctime = time(NULL);
	}

	void StatBuffer::pack(IOGRSHWriteBuffer &writeBuffer)
		const throw (IOException)
	{
		writeBuffer.writeLongLong(st_ino);
		writeBuffer.writeInt(st_mode);
		writeBuffer.writeLongLong(st_size);
		writeBuffer.writeInt(st_blocksize);
		writeBuffer.writeLongLong(_st_atime);
		writeBuffer.writeLongLong(_st_mtime);
		writeBuffer.writeLongLong(_st_ctime);
	}

	void StatBuffer::unpack(IOGRSHReadBuffer &readBuffer)
		throw (IOException)
	{
		readBuffer.readLongLong(st_ino);
		readBuffer.readInt(st_mode);
		readBuffer.readLongLong(st_size);
		readBuffer.readInt(st_blocksize);
		readBuffer.readLongLong(_st_atime);
		readBuffer.readLongLong(_st_mtime);
		readBuffer.readLongLong(_st_ctime);
	}
}
