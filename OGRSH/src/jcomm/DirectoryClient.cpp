#include "jcomm/DirectoryClient.hpp"

#include "jcomm/Socket.hpp"
#include "jcomm/Invocation.hpp"
#include "jcomm/IOException.hpp"
#include "jcomm/OGRSHException.hpp"

namespace jcomm
{
	DirectoryClient::DirectoryClient(Socket socket)
		: _socket(socket)
	{
	}

	int DirectoryClient::chdir(const std::string &fullPath)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "chdir");

		inv.addString(fullPath);

		int result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readInt(result);
		delete buffer;
		return result;
	}

	int DirectoryClient::mkdir(const std::string &fullPath,
		int mode) throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "mkdir");

		inv.addString(fullPath);
		inv.addInt(mode);
		
		int result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readInt(result);
		delete buffer;
		return result;
	}

	int DirectoryClient::rmdir(const std::string &fullPath)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "rmdir");

		inv.addString(fullPath);
		
		int result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readInt(result);
		delete buffer;
		return result;
	}

	std::string DirectoryClient::opendir(const std::string &fullPath)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "opendir");

		inv.addString(fullPath);

		std::string result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readString(result);
		delete buffer;
		return result;
	}

	DirectoryEntry* DirectoryClient::readdir(const std::string &session)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "readdir");

		inv.addString(session);

		DirectoryEntry *result = new DirectoryEntry();
		IOGRSHReadBuffer *buffer = inv.invoke();
		if (!buffer->readPackable(*result))
		{
			delete result;
			result = NULL;
		}

		delete buffer;
		return result;
	}

	void DirectoryClient::rewinddir(const std::string &session)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "rewinddir");

		inv.addString(session);

		IOGRSHReadBuffer *buffer = inv.invoke();
		delete buffer;
	}

	int DirectoryClient::closedir(const std::string &session)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "closedir");

		inv.addString(session);

		int result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readInt(result);
		delete buffer;
		return result;
	}

	StatBuffer DirectoryClient::xstat(const std::string &fullPath)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "xstat");

		inv.addString(fullPath);

		StatBuffer result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readPackable(result);
		delete buffer;
		return result;
	}

	int DirectoryClient::utimes(const std::string &fullPath,
		const TimeValStructure &accessTime,
		const TimeValStructure &modTime)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "utimes");

		inv.addString(fullPath);
		inv.addPackable(accessTime);
		inv.addPackable(modTime);

		int result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readInt(result);
		delete buffer;
		return result;
	}

	int DirectoryClient::link(const std::string &oldPath,
		const std::string &newPath)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "link");

		inv.addString(oldPath);
		inv.addString(newPath);

		int result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readInt(result);
		delete buffer;
		return result;
	}
}
