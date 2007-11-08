#include <errno.h>

#include <exception>
#include <string>

#include "ogrsh/Logging.hpp"

#include "jcomm/IOGRSHReadBuffer.hpp"
#include "jcomm/IOGRSHWriteBuffer.hpp"
#include "jcomm/OGRSHException.hpp"

namespace jcomm
{
	static const char *_TYPE_NAME
		= "edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException";

	const int OGRSHException::EXCEPTION_UNKNOWN = 0;
	const int OGRSHException::EXCEPTION_UNKNOWN_FUNCTION = 1;
	const int OGRSHException::EXCEPTION_CORRUPTED_REQUEST = 2;
	const int OGRSHException::EXCEPTION_DIVIDE_BY_ZERO = 3;
	const int OGRSHException::MALFORMED_URL = 4;
	const int OGRSHException::IO_EXCEPTION = 5;
	const int OGRSHException::UNKNOWN_SESSION = 6;
	const int OGRSHException::PATH_DOES_NOT_EXIST = 7;
	const int OGRSHException::PATH_ALREADY_EXISTS = 8;
	const int OGRSHException::PERMISSION_DENIED = 9;
	const int OGRSHException::NOT_A_DIRECTORY = 10;
	const int OGRSHException::DIRECTORY_NOT_EMPTY = 11;
	const int OGRSHException::_EBADF = 12;
	const int OGRSHException::_EISDIR = 13;

	OGRSHException::OGRSHException(IOGRSHReadBuffer &readBuffer)
		: IPackable(_TYPE_NAME)
	{
		readBuffer.readPackable(*this);
	}
			
	OGRSHException::OGRSHException(const std::string &message)
		: IPackable(_TYPE_NAME)
	{
		_message = message;
		_exceptionNumber = EXCEPTION_UNKNOWN;
	}

	OGRSHException::OGRSHException(const std::string &message,
		int exceptionNumber)
		: IPackable(_TYPE_NAME)
	{
		_message = message;
		_exceptionNumber = exceptionNumber;
	}

	OGRSHException::~OGRSHException() throw ()
	{
	}

	const char* OGRSHException::what() const throw()
	{
		return _message.c_str();
	}

	int OGRSHException::getExceptionNumber()
	{
		return _exceptionNumber;
	}

	void OGRSHException::pack(IOGRSHWriteBuffer &writeBuffer)
		const throw (IOException)
	{
		writeBuffer.writeInt(_exceptionNumber);
		writeBuffer.writeString(_message);
	}

	void OGRSHException::unpack(IOGRSHReadBuffer &readBuffer)
		throw (IOException)
	{
		readBuffer.readInt(_exceptionNumber);
		readBuffer.readString(_message);
	}

	void OGRSHException::setErrno()
	{
		OGRSH_DEBUG("Setting errno from exception(#"
			<< _exceptionNumber << "):  " << _message);

		switch (_exceptionNumber)
		{
			case OGRSHException::EXCEPTION_UNKNOWN :
				errno = -1;
				break;

			case OGRSHException::EXCEPTION_UNKNOWN_FUNCTION :
				errno = -1;
				break;

			case OGRSHException::EXCEPTION_CORRUPTED_REQUEST :
				errno = -1;
				break;

			case OGRSHException::EXCEPTION_DIVIDE_BY_ZERO :
				errno = -1;
				break;

			case OGRSHException::MALFORMED_URL :
				errno = -1;
				break;

			case OGRSHException::IO_EXCEPTION :
				errno = -1;
				break;

			case OGRSHException::UNKNOWN_SESSION :
				errno = -1;
				break;

			case OGRSHException::PATH_DOES_NOT_EXIST :
				errno = ENOENT;
				break;

			case OGRSHException::PATH_ALREADY_EXISTS :
				errno = EEXIST;
				break;

			case OGRSHException::PERMISSION_DENIED :
				errno = EACCES;
				break;

			case OGRSHException::NOT_A_DIRECTORY :
				errno = ENOTDIR;
				break;
			case OGRSHException::DIRECTORY_NOT_EMPTY :
				errno = ENOTEMPTY;
				break;
			case OGRSHException::_EBADF :
				errno = EBADF;
				break;
		}
	}
}
