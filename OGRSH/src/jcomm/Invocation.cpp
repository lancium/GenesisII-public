#include <string>

#include "ogrsh/Logging.hpp"

#include "jcomm/Invocation.hpp"
#include "jcomm/IOGRSHWriteBuffer.hpp"
#include "jcomm/IOGRSHReadBuffer.hpp"
#include "jcomm/DefaultOGRSHWriteBuffer.hpp"
#include "jcomm/DefaultOGRSHReadBuffer.hpp"
#include "jcomm/Socket.hpp"

namespace jcomm
{
/*
			std::string invocationName;
			Socket _socket;
			IOGRSHWriteBuffer *_writeBuffer;
*/

	Invocation::Invocation(Socket socket, const std::string &invocationName)
		: _socket(socket), _invocationName(invocationName)
	{
		_writeBuffer = new DefaultOGRSHWriteBuffer();
		_writeBuffer->writeString(_invocationName);
	}

	Invocation::Invocation(const Invocation &other)
		: _socket(other._socket)
	{
		OGRSH_FATAL("Not allowed to copy invocations.");
	}

	Invocation::~Invocation()
	{
		delete _writeBuffer;
	}

	Invocation& Invocation::operator=(const Invocation&)
	{
		OGRSH_FATAL("Not allowed to copy invocations.");

		return *this;
	}

	IOGRSHReadBuffer* Invocation::invoke()
		throw (OGRSHException, IOException)
	{
		ByteBuffer bb = _writeBuffer->compact();
		bb.flip();
		int remaining = bb.remaining();
		_socket.write((char*)&remaining, 4);
		_socket.write(bb);

		int size;
		_socket.read((char*)&size, 4);
		bb = ByteBuffer(size);
		_socket.read(bb);
		bb.flip();
		IOGRSHReadBuffer *read = new DefaultOGRSHReadBuffer(bb);
		std::string respType;
		read->readString(respType);
		if (respType == "response")
			return read;

		OGRSHException oe(*read);
		delete read;
		throw oe;
	}

	void Invocation::addBoolean(bool data)
	{
		_writeBuffer->writeBoolean(data);
	}

	void Invocation::addChar(char data)
	{
		_writeBuffer->writeChar(data);
	}

	void Invocation::addShort(short data)
	{
		_writeBuffer->writeShort(data);
	}

	void Invocation::addInt(int data)
	{
		_writeBuffer->writeInt(data);
	}

	void Invocation::addLongLong(long long data)
	{
		_writeBuffer->writeLongLong(data);
	}

	void Invocation::addFloat(float data)
	{
		_writeBuffer->writeFloat(data);
	}

	void Invocation::addDouble(double data)
	{
		_writeBuffer->writeDouble(data);
	}

	void Invocation::addString(const std::string &data)
	{
		_writeBuffer->writeString(data);
	}

	void Invocation::addString(const char *str)
	{
		if (str == NULL)
			addPackable(NULL);
		else
		{
			std::string data(str);
			addString(data);
		}
	}

	void Invocation::addPackable(const IPackable &data)
	{
		_writeBuffer->writePackable(data);
	}

	void Invocation::addPackable(const IPackable *data)
	{
		_writeBuffer->writePackable(data);
	}

	void Invocation::addBytes(const void *buf, int length)
	{
		_writeBuffer->writeBytes(buf, length);
	}
}
