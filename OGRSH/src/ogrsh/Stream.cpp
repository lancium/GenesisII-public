#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "ogrsh/Logging.hpp"
#include "ogrsh/Stream.hpp"

#include "ogrsh/shims/File.hpp"

namespace ogrsh
{
	StreamBackend::~StreamBackend()
	{
	}

	const int Stream::_TMP_PRINT_BUFFER_SIZE = 256;

	void Stream::switchToReading()
	{
		if (_readBuffer != NULL)
			return;

		if (_writeBuffer != NULL)
		{
			internalFlush();
			free(_writeBuffer);
			_writeBuffer = NULL;
		}

		_readBuffer = (char*)malloc(_bufferSize);
		internalFill();
	}

	void Stream::switchToWriting()
	{
		if (_writeBuffer != NULL)
			return;

		if (_readBuffer != NULL)
		{
			_backend->seek(_bufferPtr - _bufferLimit, SEEK_CUR);
			free(_readBuffer);
			_readBuffer = NULL;
		}

		_writeBuffer = (char*)malloc(_bufferSize);
		_bufferPtr = 0;
		_bufferLimit = _bufferSize;
	}

	void Stream::internalFill()
	{
		_bufferLimit = _backend->fill(_readBuffer, _bufferSize);
		_bufferPtr = 0;
	}

	void Stream::internalFlush()
	{
		_backend->flush(_writeBuffer, _bufferPtr);
		_bufferPtr = 0;
	}

	void Stream::writeBytes(char *buffer, size_t size)
	{
		while (size > 0)
		{
			size_t left = _bufferLimit - _bufferPtr;
			if (left > size)
				left = size;

			memcpy(_writeBuffer + _bufferPtr, buffer, left);
			size -= left;
			_bufferPtr += left;
			buffer += left;

			if (size > 0)
				internalFlush();
		}
	}

	Stream::Stream(const Stream&)
	{
		OGRSH_FATAL("Not allowed to copy streams.");
		ogrsh::shims::real_exit(1);
	}

	Stream& Stream::operator=(const Stream&)
	{
		OGRSH_FATAL("Not allowed to copy streams.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	Stream::Stream(StreamBackend *backend, const int bufferSize)
	{
		tmpPrintBuffer = new char[_TMP_PRINT_BUFFER_SIZE];
		_bufferSize = bufferSize;

		_bufferPtr = 0;
		_bufferLimit = 0;
		_readBuffer = NULL;
		_writeBuffer = NULL;

		_backend = backend;
	}

	Stream::~Stream()
	{
		delete []tmpPrintBuffer;

		if (_readBuffer != NULL)
			free(_readBuffer);
		if (_writeBuffer != NULL)
		{
			internalFlush();
			free(_writeBuffer);
		}
	}

	char* Stream::fgets(char *s, int n)
	{
		register char *start;
		register char *end;
		register char *current;
		char *target = s;
		register int distance;
		n = n-1;
		switchToReading();

		if (_bufferPtr >= _bufferLimit)
			internalFill();
		if (_bufferPtr >= _bufferLimit)
			return NULL;

		while (true)
		{
			// First, try to go through the buffer looking for the '\n'
			
			start = _readBuffer + _bufferPtr;
			end = _readBuffer + _bufferLimit;

			distance = 0;
			for (current = start; current < end; current++)
			{
				distance++;
				if (distance >= n || *current == '\n')
				{
					// We've reached the end of what we can handle or we've
					// reached our target.
					memcpy(target, start, distance);
					n-=distance;
					target+=distance;
					*target = (char)0;
					_bufferPtr+=distance;
					return s;
				}
			}

			// The end of the buffer -- we need more
			distance = end - start;
			memcpy(target, start, distance);
			n-=distance;
			target+=distance;
			_bufferPtr+=distance;
			internalFill();
		}

		// Can't get here.
		return NULL;
	}

	int Stream::fprintf(const char *format, va_list args)
	{
		char *buffer;

		switchToWriting();

		int desired = vsnprintf(tmpPrintBuffer, _TMP_PRINT_BUFFER_SIZE,
			format, args);
		if (desired >= _TMP_PRINT_BUFFER_SIZE)
		{
			buffer = new char[desired + 1];
			desired = vsnprintf(buffer, _TMP_PRINT_BUFFER_SIZE, format, args);
			writeBytes(buffer, desired);
			delete []buffer;
		} else
		{
			writeBytes(tmpPrintBuffer, desired);
		}

		return desired;
	}

	int Stream::fflush()
	{
		internalFlush();
		return 0;
	}

	long Stream::ftell()
	{
		long positionDiff = 0;

		if (_writeBuffer != NULL)
		{
			positionDiff += _bufferPtr;
		} else if (_readBuffer != NULL)
		{
			positionDiff += (_bufferPtr - _bufferLimit);
		}

		return positionDiff + _backend->tell();
	}

	int Stream::fseek(long offset, int whence)
	{
		int ret;
		long positionDiff = 0;

		if (_writeBuffer != NULL)
		{
			positionDiff += _bufferPtr;
			internalFlush();
		} else if (_readBuffer != NULL)
		{
			positionDiff += (_bufferPtr - _bufferLimit);
		}

		switch (whence)
		{
			case SEEK_SET :
				ret = _backend->seek(offset, whence);
				break;
			case SEEK_CUR :
				ret = _backend->seek(positionDiff + offset, whence);
				break;
			case SEEK_END :
				ret = _backend->seek(offset, whence);
				break;
			default:
				return -1;
		}

		switchToWriting();
		return ret;
	}

	size_t Stream::fread(void *ptr, size_t size, size_t nmemb)
	{
		unsigned int sizeLeft;
		unsigned int numElementsLeft;

		switchToReading();

		if (_bufferPtr >= _bufferLimit)
			internalFill();

		if (_bufferPtr >= _bufferLimit)
			return 0;

		sizeLeft = _bufferLimit - _bufferPtr;

		if (sizeLeft < size)
		{
			// We have a short piece left.
			memmove(_readBuffer, _readBuffer + _bufferPtr, sizeLeft);
			_bufferLimit = _backend->fill(
				_readBuffer + sizeLeft, _bufferLimit - sizeLeft) + sizeLeft;
			_bufferPtr = 0;
			sizeLeft = _bufferLimit - _bufferPtr;
		}

		numElementsLeft = sizeLeft / size;
		if (numElementsLeft > nmemb)
			numElementsLeft = nmemb;
		memcpy(ptr, _readBuffer + _bufferPtr, numElementsLeft * size);
		_bufferPtr += (numElementsLeft * size);
		return numElementsLeft;
	}

	size_t Stream::fwrite(const void *ptr, size_t size, size_t nmemb)
	{
		switchToWriting();

		writeBytes((char*)ptr, size * nmemb);
		return nmemb;
	}

	int Stream::fgetc()
	{
		switchToReading();

		if (_bufferPtr >= _bufferLimit)
			internalFill();

		if (_bufferPtr >= _bufferLimit)
			return EOF;

		return _readBuffer[_bufferPtr++];
	}

	int Stream::feof()
	{
		switchToReading();

		if (_bufferPtr >= _bufferLimit)
			internalFill();

		if (_bufferPtr >= _bufferLimit)
			return 1;

		return 0;
	}

	int Stream::ferror()
	{
		switchToReading();

		if (_bufferPtr >= _bufferLimit)
			internalFill();

		if (_bufferPtr >= _bufferLimit)
			return 0;

		return 0;
	}

	int Stream::pending()
	{
		if (_readBuffer != NULL)
			return 0;

		return _bufferPtr;
	}
}
