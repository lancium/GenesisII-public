#ifndef __OGRSH_EXCEPTION_HPP__
#define __OGRSH_EXCEPTION_HPP__

#include <exception>
#include <string>

#include "jcomm/IPackable.hpp"

namespace jcomm
{
	class OGRSHException : public std::exception, public IPackable
	{
		public:
			static const int EXCEPTION_UNKNOWN;
			static const int EXCEPTION_UNKNOWN_FUNCTION;
			static const int EXCEPTION_CORRUPTED_REQUEST;
			static const int EXCEPTION_DIVIDE_BY_ZERO;
			static const int MALFORMED_URL;
			static const int IO_EXCEPTION;
			static const int UNKNOWN_SESSION;
			static const int PATH_DOES_NOT_EXIST;
			static const int PATH_ALREADY_EXISTS;
			static const int PERMISSION_DENIED;
			static const int NOT_A_DIRECTORY;
			static const int DIRECTORY_NOT_EMPTY;
			static const int _EBADF;
			static const int _EISDIR;
				
		private:
			std::string _message;
			int _exceptionNumber;

		public:
			OGRSHException(IOGRSHReadBuffer &);
			OGRSHException(const std::string &message);
			OGRSHException(const std::string &message,
				int exceptionNumber);
			virtual ~OGRSHException() throw();

			virtual const char* what() const throw();

			int getExceptionNumber();

			virtual void pack(IOGRSHWriteBuffer &writeBuffer)
				const throw (IOException);
			virtual void unpack(IOGRSHReadBuffer &readBuffer)
				throw (IOException);

			void setErrno();
	};
}

#endif
