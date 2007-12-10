#ifndef __STREAM_HPP__
#define __STREAM_HPP__

#include <stdarg.h>

namespace ogrsh
{
	class StreamBackend
	{
		public:
			virtual ~StreamBackend();

			virtual long tell() = 0;
			virtual long seek(long offset, int whence) = 0;
			virtual int fill(char *buffer, int limit) = 0;
			virtual void flush(char *buffer, int size) = 0;
	};

	class Stream
	{
		private:
			int _bufferSize;

			static const int _TMP_PRINT_BUFFER_SIZE;
			char *tmpPrintBuffer;

			char *_readBuffer;
			char *_writeBuffer;

			int _bufferPtr;
			int _bufferLimit;

			void switchToReading();
			void switchToWriting();

			void internalFill();
			void internalFlush();

			void writeBytes(char *buffer, size_t size);

			StreamBackend *_backend;

		protected:
			Stream(const Stream&);
			Stream& operator=(const Stream&);

		public:
			Stream(StreamBackend *backend, const int bufferSize);
			~Stream();

			char* fgets(char *s, int n);
			int fprintf(const char *format, va_list args);
			int fflush();
			int fseek(long offset, int whence);
			long ftell();
			size_t fread(void *ptr, size_t size, size_t nmemb);
			size_t fwrite(const void *ptr, size_t size, size_t nmemb);
			int fgetc();
			int feof();
			int ferror();

			int pending();
	};
}

#endif
