#include "jcomm/Socket.hpp"
#include "jcomm/IOException.hpp"
#include "jcomm/OGRSHException.hpp"
#include "jcomm/StatBuffer.hpp"
#include "jcomm/FileClient.hpp"
#include "jcomm/Invocation.hpp"

namespace jcomm
{
	FileClient::FileClient(Socket socket)
		: _socket(socket)
	{
	}

	std::string FileClient::open(const std::string &fullPath,
		int flags, mode_t mode)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "open");

		inv.addString(fullPath);
		inv.addInt(flags);
		inv.addInt(mode);

		std::string result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readString(result);
		delete buffer;
		return result;
	}

	std::string FileClient::duplicate(const std::string &fileDesc)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "duplicate");

		inv.addString(fileDesc);

		std::string result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readString(result);
		delete buffer;
		return result;
	}

	ssize_t FileClient::read(const std::string &fileDesc,
		void *buf, size_t length)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "read");

		inv.addString(fileDesc);
		inv.addInt(length);

		ssize_t ret;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readBytes(&buf, ret);
		delete buffer;
		return ret;
	}

	ssize_t FileClient::write(const std::string &fileDesc,
		const void *buf, size_t length)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "write");

		inv.addString(fileDesc);
		inv.addBytes(buf, length);

		int ret;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readInt(ret);
		delete buffer;
		return ret;
	}

	off64_t FileClient::lseek64(const std::string &fileDesc,
		off64_t offset, int whence)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "lseek64");

		inv.addString(fileDesc);
		inv.addLongLong(offset);
		inv.addInt(whence);

		long long int ret;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readLongLong(ret);
		delete buffer;
		return ret;
	}

	void FileClient::close(const std::string &fileDesc)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "close");

		inv.addString(fileDesc);

		IOGRSHReadBuffer *buffer = inv.invoke();
		delete buffer;
	}

	int FileClient::unlink(const std::string &fullPath)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "unlink");

		inv.addString(fullPath);

		int result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readInt(result);
		delete buffer;
		return result;
	}

	StatBuffer FileClient::fxstat(const std::string &fileDesc)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "fxstat");

		inv.addString(fileDesc);

		StatBuffer result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readPackable(result);
		delete buffer;
		return result;
	}

	int FileClient::truncate64(const std::string &fileDesc,
		off64_t length) throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "truncate");

		inv.addString(fileDesc);
		inv.addLongLong(length);

		int ret;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readInt(ret);
		delete buffer;
		return ret;
	}
}
