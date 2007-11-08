#include <string>

#include "ogrsh/Logging.hpp"

#include "jcomm/IPackable.hpp"
#include "jcomm/IOGRSHReadBuffer.hpp"
#include "jcomm/DefaultOGRSHReadBuffer.hpp"

namespace jcomm
{
	std::string DefaultOGRSHReadBuffer::readUTF()
		throw (IOException)
	{
		int length = _buffer.getInt();
		char *data = new char[length + 1];
		_buffer.get(data, 0, length);
		data[length] = (char)0;
		std::string str(data);
		delete []data;
		return str;
	}

	void DefaultOGRSHReadBuffer::verifyType(
		const std::string &expectedType)
	{
		std::string type = readUTF();
		verifyType(type, expectedType);
	}

	void DefaultOGRSHReadBuffer::verifyType(
		const std::string &type,
		const std::string &expectedType)
	{
		if (type == expectedType)
			return;

		OGRSH_FATAL("Expected type \"" << expectedType
			<< "\" but received type \"" << type << "\".");
		ogrsh::shims::real_exit(1);
	}

	DefaultOGRSHReadBuffer::DefaultOGRSHReadBuffer(ByteBuffer &buffer)
		: _buffer(buffer)
	{
	}

	void DefaultOGRSHReadBuffer::readBoolean(bool &target)
		throw (IOException)
	{
		verifyType("java.lang.Boolean");
		char c = _buffer.get();
		if (c == (char)0)
			target = false;
		else
			target = true;
	}

	void DefaultOGRSHReadBuffer::readChar(char &target)
		throw (IOException)
	{
		verifyType("java.lang.Byte");
		target = _buffer.get();
	}

	void DefaultOGRSHReadBuffer::readShort(short &target)
		throw (IOException)
	{
		verifyType("java.lang.Short");
		target = _buffer.getShort();
	}

	void DefaultOGRSHReadBuffer::readInt(int &target)
		throw (IOException)
	{
		verifyType("java.lang.Integer");
		target = _buffer.getInt();
	}

	void DefaultOGRSHReadBuffer::readLongLong(long long &target)
		throw (IOException)
	{
		verifyType("java.lang.Long");
		target = _buffer.getLongLong();
	}

	void DefaultOGRSHReadBuffer::readFloat(float &target)
		throw (IOException)
	{
		verifyType("java.lang.Float");
		target = _buffer.getFloat();
	}

	void DefaultOGRSHReadBuffer::readDouble(double &target)
		throw (IOException)
	{
		verifyType("java.lang.Double");
		target = _buffer.getDouble();
	}

	bool DefaultOGRSHReadBuffer::readString(std::string  &target)
		throw (IOException)
	{
		std::string type = readUTF();
		if (type == "null")
			return false;

		verifyType(type, "java.lang.String");
		target = readUTF();
		return true;
	}

	bool DefaultOGRSHReadBuffer::readPackable(IPackable &target)
		throw (IOException)
	{
		std::string type = readUTF();
		if (type == "null")
			return false;

		verifyType(type, target.getTypeName());
		target.unpack(*this);
		return true;
	}

	void DefaultOGRSHReadBuffer::readBytes(void **buf, ssize_t &length)
		throw (IOException)
	{
		std::string type = readUTF();
		if (type == "null")
		{
			length = 0;
			return;
		}

		verifyType(type, "byte-array");
		length = _buffer.getInt();
		_buffer.get(*((char**)buf), 0, length);
	}
}
