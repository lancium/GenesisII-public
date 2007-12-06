#include "jcomm/IPackable.hpp"
#include "jcomm/TimeValStructure.hpp"

namespace jcomm
{
	TimeValStructure::TimeValStructure()
		: IPackable("edu.virginia.vcgr.ogrsh.server.dir.TimeValStructure")
	{
		seconds = 0;
		microseconds = 0;
	}

	void TimeValStructure::pack(IOGRSHWriteBuffer &writeBuffer)
		const throw (IOException)
	{
		writeBuffer.writeLongLong(seconds);
		writeBuffer.writeLongLong(microseconds);
	}

	void TimeValStructure::unpack(IOGRSHReadBuffer &readBuffer)
		throw (IOException)
	{
		readBuffer.readLongLong(seconds);
		readBuffer.readLongLong(microseconds);
	}
}
