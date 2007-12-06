#ifndef __TIMEVAL_STRUCTURE_HPP__
#define __TIMEVAL_STRUCTURE_HPP__

#include "jcomm/IPackable.hpp"
#include "jcomm/IOGRSHReadBuffer.hpp"
#include "jcomm/IOGRSHWriteBuffer.hpp"

namespace jcomm
{
	class TimeValStructure : public IPackable
	{
		public:
			TimeValStructure();

			long long seconds;
			long long microseconds;

			virtual void pack(IOGRSHWriteBuffer &writeBuffer)
				const throw (IOException);
			virtual void unpack(IOGRSHReadBuffer &readBuffer)
				throw (IOException);
	};
}

#endif
