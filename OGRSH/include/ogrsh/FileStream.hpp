#ifndef __FILE_STREAM_HPP__
#define __FILE_STREAM_HPP__

#include <stdarg.h>

#include "ogrsh/Stream.hpp"

namespace ogrsh
{

#define FILE_STREAM_MAGIC_NUMBER 0xCAFE01EE

	class FileStream
	{
		public:
			unsigned int _magicNumber;

		private:
			static const int _BUFFER_SIZE;

			int _fd;
			Stream *_stream;

			FileStream(const FileStream&);
			FileStream& operator= (const FileStream&);

		public:
			FileStream(const char *path, const char *mode);
			FileStream(int fd);
			~FileStream();

			char* fgets(char *s, int n);
			int fprintf(const char *format, va_list ap);
			int fflush();
			int fseek(long offset, int whence);
			long ftell();
			size_t fread(void *ptr, size_t size, size_t nmemb);
			size_t fwrite(const void *ptr, size_t size, size_t nmemb);
			int fgetc();
			int feof();
			int ferror();
			int fileno();

			int pending();
	};
}

#endif
