#include <sys/types.h>
#include <sys/stat.h>

#include <fcntl.h>
#include <stdarg.h>

#include "ogrsh/FileStream.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/FDStreamBackend.hpp"

namespace ogrsh
{
	const int FileStream::_BUFFER_SIZE = 1024 * 1024;

	FileStream::FileStream(const FileStream&)
	{
		OGRSH_FATAL("Not allowed to copy File Streams.");
		ogrsh::shims::real_exit(1);
	}

	FileStream& FileStream::operator= (const FileStream&)
	{
		OGRSH_FATAL("Not allowed to copy File Streams.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	FileStream::FileStream(const char *path, const char *mode)
	{
		int lcv;
		char c;
		bool isRead = false;
		bool isWrite = false;
		int imode = 0x0;

		_magicNumber = FILE_STREAM_MAGIC_NUMBER;

		for (lcv = 0; mode[lcv] != (char)0; lcv++)
		{
			c = mode[lcv];
			switch (c)
			{
				case 'r' :
					isRead = true;
					break;

				case 'w' :
					isWrite = true;
					imode |= O_TRUNC;
					imode |= O_CREAT;
					break;

				case 'a' :
					isWrite = true;
					imode |= O_CREAT;
					imode |= O_APPEND;
					break;

				case '+' :
					if (isWrite)
						isRead = true;
					else
						isWrite = true;
					break;
				default :
					/* Don't know how to handle this flag.  Let's skip. */
					break;
			}
		}

		if (isRead)
		{
			if (isWrite)
			{
				imode |= O_RDWR;
			} else
			{
				imode |= O_RDONLY;
			}
		} else
		{
			imode |= O_WRONLY;
		}

		_fd = open(path, imode, 0644);
		_stream = new Stream(new FDStreamBackend(_fd), 1024 * 1024);
	}

	FileStream::FileStream(int fd)
	{
		_fd = fd;
		_magicNumber = FILE_STREAM_MAGIC_NUMBER;
		_stream = new Stream(new FDStreamBackend(_fd), 1024 * 1024);
	}

	FileStream::~FileStream()
	{
		delete _stream;
		close(_fd);
	}

	char* FileStream::fgets(char *s, int n)
	{
		return _stream->fgets(s, n);
	}

	int FileStream::fprintf(const char *format, va_list ap)
	{
		return _stream->fprintf(format, ap);
	}

	int FileStream::fflush()
	{
		return _stream->fflush();
	}

	int FileStream::fseek(long offset, int whence)
	{
		return _stream->fseek(offset, whence);
	}

	long FileStream::ftell()
	{
		return _stream->ftell();
	}

	size_t FileStream::fread(void *ptr, size_t size, size_t nmemb)
	{
		return _stream->fread(ptr, size, nmemb);
	}

	size_t FileStream::fwrite(const void *ptr, size_t size, size_t nmemb)
	{
		return _stream->fwrite(ptr, size, nmemb);
	}

	int FileStream::fgetc()
	{
		return _stream->fgetc();
	}

	int FileStream::feof()
	{
		return _stream->feof();
	}

	int FileStream::ferror()
	{
		return _stream->ferror();
	}

	int FileStream::fileno()
	{
		return _fd;
	}
}
