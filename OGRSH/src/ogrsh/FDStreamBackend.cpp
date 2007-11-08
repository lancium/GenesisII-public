#include <unistd.h>

#include "ogrsh/Logging.hpp"
#include "ogrsh/Stream.hpp"
#include "ogrsh/FDStreamBackend.hpp"
#include "ogrsh/shims/File.hpp"

namespace ogrsh
{
	long FDStreamBackend::tell()
	{
//		return ogrsh::shims::real_lseek(_fd, 0, SEEK_CUR);
		return lseek(_fd, 0, SEEK_CUR);
	}

	long FDStreamBackend::seek(long offset, int whence)
	{
//		return ogrsh::shims::real_lseek(_fd, offset, whence);
		return lseek(_fd, offset, whence);
	}

	int FDStreamBackend::fill(char *buffer, int limit)
	{
		return read(_fd, buffer, limit);
	}

	void FDStreamBackend::flush(char *buffer, int size)
	{
		write(_fd, buffer, size);
	}

	FDStreamBackend::FDStreamBackend(int fd)
	{
		_fd = fd;
	}

	FDStreamBackend::~FDStreamBackend()
	{
	}
}
