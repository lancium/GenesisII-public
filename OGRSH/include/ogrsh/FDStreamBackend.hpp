#ifndef __FD_STREAM_BACKEND_HPP__
#define __FD_STREAM_BACKEND_HPP__

#include "ogrsh/Stream.hpp"

namespace ogrsh
{
	class FDStreamBackend : public StreamBackend
	{
		private:
			int _fd;

		public:
			FDStreamBackend(int fd);
			virtual ~FDStreamBackend();

			virtual off_t tell();
			virtual off_t seek(off_t offset, int whence);
			virtual int fill(char *buffer, int limit);
			virtual void flush(char *buffer, int size);
	};
}

#endif
