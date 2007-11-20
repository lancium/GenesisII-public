#include <list>

#include "ogrsh/Logging.hpp"

#include "jcomm/IOGRSHWriteBuffer.hpp"
#include "jcomm/ByteBuffer.hpp"
#include "jcomm/DefaultOGRSHWriteBuffer.hpp"

namespace jcomm
{
	const int DefaultOGRSHWriteBuffer::_CAPACITY = 1024;

	DefaultOGRSHWriteBuffer::DefaultOGRSHWriteBuffer(
		const DefaultOGRSHWriteBuffer&)
	{
		OGRSH_FATAL("Not allowed to copy DefaultOGRSHWriteBuffers.");
		ogrsh::shims::real_exit(1);
	}

	DefaultOGRSHWriteBuffer& DefaultOGRSHWriteBuffer::operator= (
		const DefaultOGRSHWriteBuffer&)
	{
		OGRSH_FATAL("Not allowed to copy DefaultOGRSHWriteBuffers.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	void DefaultOGRSHWriteBuffer::ensure(int size)
	{
		if (_current->remaining() < size)
		{
			_current = new ByteBuffer(
				(size < _CAPACITY) ? _CAPACITY : size);
			_buffers.push_back(_current);
		}
	}

	void DefaultOGRSHWriteBuffer::writeUTF(const std::string &str)
	{
		const char *data = str.c_str();
		int length = str.length();
		ensure(4);
		_current->putInt(length);
		int offset = 0;
		int toWrite = _current->remaining();
		if (toWrite > length)
			toWrite = length;
		_current->put(data, offset, toWrite);
		length -= toWrite;
		offset += toWrite;
		if (length > 0)
		{
			ensure(length);
			_current->put(data, offset, length);
		}
	}

	DefaultOGRSHWriteBuffer::DefaultOGRSHWriteBuffer()
	{
		_current = new ByteBuffer(_CAPACITY);
		_buffers.push_back(_current);
	}

	DefaultOGRSHWriteBuffer::~DefaultOGRSHWriteBuffer()
	{
		std::list<ByteBuffer*>::iterator iter;
		for (iter = _buffers.begin(); iter != _buffers.end(); iter++)
		{
			_current = *iter;
			delete _current;
		}
	}

	ByteBuffer DefaultOGRSHWriteBuffer::compact()
	{
		int totalSize = 0;
		std::list<ByteBuffer*>::iterator iter;
		for (iter = _buffers.begin(); iter != _buffers.end(); iter++)
		{
			totalSize += (*iter)->position();
		}

		ByteBuffer ret(totalSize);
		for (iter = _buffers.begin(); iter != _buffers.end(); iter++)
		{
			ByteBuffer tmp = (*iter)->duplicate();
			tmp.flip();
			ret.put(tmp);
		}

		return ret;
	}

	void DefaultOGRSHWriteBuffer::writeRaw(char *data,
		int offset, int length) throw (IOException)
	{
		ensure(length);
		_current->put(data, offset, length);
	}

	void DefaultOGRSHWriteBuffer::writeBoolean(bool b) throw (IOException)
	{
		writeUTF("java.lang.Boolean");
		ensure(1);
		_current->put(b ? (char)1 : (char)0);
	}

	void DefaultOGRSHWriteBuffer::writeChar(char c) throw (IOException)
	{
		writeUTF("java.lang.Byte");
		ensure(1);
		_current->put(c);
	}

	void DefaultOGRSHWriteBuffer::writeShort(short int i)
		throw (IOException)
	{
		writeUTF("java.lang.Short");
		ensure(2);
		_current->putShort(i);
	}

	void DefaultOGRSHWriteBuffer::writeInt(int i) throw (IOException)
	{
		writeUTF("java.lang.Integer");
		ensure(4);
		_current->putInt(i);
	}

	void DefaultOGRSHWriteBuffer::writeLongLong(long long int i)
		throw (IOException)
	{
		writeUTF("java.lang.Long");
		ensure(8);
		_current->putLongLong(i);
	}

	void DefaultOGRSHWriteBuffer::writeFloat(float f) throw (IOException)
	{
		writeUTF("java.lang.Float");
		ensure(4);
		_current->putFloat(f);
	}

	void DefaultOGRSHWriteBuffer::writeDouble(double d) throw (IOException)
	{
		writeUTF("java.lang.Double");
		ensure(8);
		_current->putDouble(d);
	}

	void DefaultOGRSHWriteBuffer::writeString(const std::string &str)
		throw (IOException)
	{
		writeUTF("java.lang.String");
		writeUTF(str);
	}

	void DefaultOGRSHWriteBuffer::writePackable(const IPackable &packable)
		throw (IOException)
	{
		writePackable(&packable);
	}

	void DefaultOGRSHWriteBuffer::writePackable(const IPackable *packable)
		throw (IOException)
	{
		if (packable == NULL)
		{
			writeUTF("null");
		} else
		{
			writeUTF(packable->getTypeName());
			packable->pack(*this);
		}
	}

	void DefaultOGRSHWriteBuffer::writeBytes(const void *buf, int length)
		throw (IOException)
	{
		if (buf == NULL)
		{
			writeUTF("null");
		} else
		{
			writeUTF("byte-array");
			ensure(4);
			_current->putInt(length);
			ensure(length);
			_current->put((const char*)buf, 0, length);
		}
	}
}
