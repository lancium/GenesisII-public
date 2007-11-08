#ifndef __STAT_BUFFER_HPP__
#define __STAT_BUFFER_HPP__

#include "jcomm/IPackable.hpp"
#include "jcomm/IOGRSHReadBuffer.hpp"
#include "jcomm/IOGRSHWriteBuffer.hpp"

namespace jcomm
{
	class StatBuffer : public IPackable
	{
		public:
			StatBuffer();

			long long st_ino;
			int st_mode;
			long long st_size;
			int st_blocksize;
			long long _st_atime;
			long long _st_mtime;
			long long _st_ctime;

			virtual void pack(IOGRSHWriteBuffer &writeBuffer)
				const throw (IOException);
			virtual void unpack(IOGRSHReadBuffer &readBuffer)
				throw (IOException);
	};
}

#endif
