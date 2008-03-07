#include "ogrsh/Logging.hpp"

#include "jcomm/ByteBuffer.hpp"

namespace jcomm
{
	ByteBuffer::ByteBuffer(int capacity)
	{
		_buffer = new char[capacity];
		_referenceCount = new int;
		(*_referenceCount) = 1;
		_limit = _capacity = capacity;
		_position = 0;
	}

	ByteBuffer::ByteBuffer(const ByteBuffer  &other)
	{
		_buffer = other._buffer;
		_limit = other._limit;
		_position = other._position;
		_capacity = other._capacity;
		_referenceCount = other._referenceCount;
		(*_referenceCount)++;
	}

	ByteBuffer::ByteBuffer(const char *data, int capacity)
	{
		_buffer = new char[capacity];
		memcpy(_buffer, data, capacity);
		_referenceCount = new int;
		(*_referenceCount) = 1;
		_limit = _capacity = capacity;
		_position = 0;
	}

	ByteBuffer::~ByteBuffer()
	{
		(*_referenceCount)--;
		if (*_referenceCount == 0)
		{
			delete _referenceCount;
			delete []_buffer;
		}
	}

	int ByteBuffer::flowException(bool isUnderflow) const
	{
		if (isUnderflow)
		{
			OGRSH_FATAL("ByteBuffer Underflow Exception occurred.");
			abort();
		} else
		{
			OGRSH_FATAL("ByteBuffer Overflow Exception occurred.");
			abort();
		}

		ogrsh::shims::real_exit(1);
		return 0;
	}

	ByteBuffer& ByteBuffer::operator= (const ByteBuffer &other)
	{
		(*(other._referenceCount))++;
		(*_referenceCount)--;
		if (*_referenceCount == 0)
		{
			delete _referenceCount;
			delete []_buffer;
		}

		_referenceCount = other._referenceCount;
		_buffer = other._buffer;
		_limit = other._limit;
		_position = other._position;
		_capacity = other._capacity;

		return *this;
	}

	char ByteBuffer::get()
	{
		readVerify(1);
		return _buffer[_position++];
	}

	ByteBuffer& ByteBuffer::get(char *dest, int offset, int length)
	{
		readVerify(length);
		memmove(dest + offset, _buffer + _position, length);
		_position += length;
		return *this;
	}

	short int ByteBuffer::getShort()
	{
		readVerify(2);
		short int tmp;
		memcpy(&tmp, _buffer + _position, 2);
		_position += 2;
		return tmp;
	}

	int ByteBuffer::getInt()
	{
		readVerify(4);
		int tmp;
		memcpy(&tmp, _buffer + _position, 4);
		_position += 4;
		return tmp;
	}

	long long ByteBuffer::getLongLong()
	{
		readVerify(8);
		long long int tmp;
		memcpy(&tmp, _buffer + _position, 8);
		_position += 8;
		return tmp;
	}

	float ByteBuffer::getFloat()
	{
		readVerify(4);
		float tmp;
		memcpy(&tmp, _buffer + _position, 4);
		_position += 4;
		return tmp;
	}

	double ByteBuffer::getDouble()
	{
		readVerify(8);
		double tmp;
		memcpy(&tmp, _buffer + _position, 8);
		_position += 8;
		return tmp;
	}

	void ByteBuffer::put(char c)
	{
		writeVerify(1);
		memcpy(_buffer + _position, &c, 1);
		_position += 1;
	}

	void ByteBuffer::put(const char *src, int offset, int length)
	{
		writeVerify(length);
		memcpy(_buffer + _position, src + offset, length);
		_position += length;
	}

	void ByteBuffer::put(ByteBuffer &buffer)
	{
		put(buffer._buffer, buffer._position, buffer.remaining());
		buffer._position += buffer.remaining();
	}

	void ByteBuffer::putShort(short s)
	{
		writeVerify(2);
		memcpy(_buffer + _position, &s, 2);
		_position += 2;
	}

	void ByteBuffer::putInt(int i)
	{
		writeVerify(4);
		memcpy(_buffer + _position, &i, 4);
		_position += 4;
	}

	void ByteBuffer::putLongLong(long long l)
	{
		writeVerify(8);
		memcpy(_buffer + _position, &l, 8);
		_position += 8;
	}

	void ByteBuffer::putFloat(float f)
	{
		writeVerify(4);
		memcpy(_buffer + _position, &f, 4);
		_position += 4;
	}

	void ByteBuffer::putDouble(double d)
	{
		writeVerify(8);
		memcpy(_buffer + _position, &d, 8);
		_position += 8;
	}
}
