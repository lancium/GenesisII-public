#include <errno.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include "ogrsh/Logging.hpp"
#include "ogrsh/ProtectedTable.hpp"
#include "ogrsh/shims/File.hpp"

#include "jcomm/IOException.hpp"
#include "jcomm/Socket.hpp"
#include "jcomm/ByteBuffer.hpp"
#include "jcomm/DefaultOGRSHWriteBuffer.hpp"

namespace jcomm
{
	Socket::Socket(const std::string &address, short port,
		const std::string &secret) throw (IOException)
	{
		_referenceCount = new int;
		(*_referenceCount) = 1;

		_socket = -1;
		struct sockaddr_in serv_addr;
		bzero((char*)&serv_addr, sizeof(serv_addr));
		serv_addr.sin_family = AF_INET;
		serv_addr.sin_addr.s_addr = inet_addr(address.c_str());
		serv_addr.sin_port = htons(port);

		if ( (_socket = socket(AF_INET, SOCK_STREAM, 0)) < 0)
			throw IOException("Unable to create socket.");

		ogrsh::ProtectedTable::getInstance().protect(_socket);

		if (connect(_socket, (struct sockaddr*)&serv_addr,
			sizeof(serv_addr)) < 0)
			throw IOException("Unable to connect to server.");

		short one = (short)1;
		write((char*)&one, 2);
		int len = secret.length();
		write((char*)&len, 4);
		write(secret.c_str(), len);
	}

	Socket::Socket(const Socket &other)
	{
		_socket = other._socket;
		_referenceCount = other._referenceCount;
		(*_referenceCount)++;
	}

	Socket::~Socket() throw (IOException)
	{
		if (--(*_referenceCount) <= 0)
		{
			delete _referenceCount;
			if (_socket >= 0)
			{
				ogrsh::ProtectedTable::getInstance().unprotect(_socket);
				OGRSH_DEBUG("Closing Socket.");
				close(_socket);
			}
		}
	}

	Socket& Socket::operator= (const Socket &other)
	{
		if (this == &other)
			return *this;

		if (--(*_referenceCount) <= 0)
		{
			delete _referenceCount;
			if (_socket >= 0)
			{
				ogrsh::ProtectedTable::getInstance().unprotect(_socket);
				OGRSH_DEBUG("Closing Socket.");
				close(_socket);
			}
		}

		_socket = other._socket;
		_referenceCount = other._referenceCount;
		(*_referenceCount)++;

		return *this;
	}

	void Socket::write(const char *ddata, int len) throw (IOException)
	{
		char *data = (char*)ddata;

		while (len > 0)
		{
			int written = ogrsh::shims::real_write(_socket, data, len);
			if (written < 0)
			{
				if (errno == EINTR)
					continue;

				throw IOException("Unable to write to server.");
			}

			data += written;
			len -= written;
		}
	}

	void Socket::write(ByteBuffer &bb) throw (IOException)
	{
		while (bb.remaining())
		{
			int written = ogrsh::shims::real_write(
				_socket, bb._buffer + bb._position, bb.remaining());
			if (written < 0)
			{
				if (errno == EINTR)
					continue;

				throw IOException("Unable to write to server.");
			}

			bb._position += written;
		}
	}

	void Socket::read(char *data, int length) throw (IOException)
	{
		while (length > 0)
		{
			int r = ogrsh::shims::real_read(
				_socket, data, length);
			if (r < 0)
			{
				if (errno == EINTR)
					continue;

				throw IOException("Unable to read from server.");
			}

			data += r;
			length -= r;
		}
	}

	void Socket::read(ByteBuffer &bb) throw (IOException)
	{
		while (bb.remaining())
		{
			int r = ogrsh::shims::real_read(
				_socket, bb._buffer + bb._position, bb.remaining());
			if (r < 0)
			{
				if (errno == EINTR)
					continue;

				throw IOException("Unable to read from server.");
			}

			bb._position += r;
		}
	}
}
