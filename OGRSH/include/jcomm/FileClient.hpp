#ifndef __FILE_CLIENT_HPP__
#define __FILE_CLIENT_HPP__

#include "jcomm/Socket.hpp"
#include "jcomm/IOException.hpp"
#include "jcomm/OGRSHException.hpp"
#include "jcomm/StatBuffer.hpp"

namespace jcomm
{
	class FileClient
	{
		private:
			Socket _socket;

		public:
			FileClient(Socket socket);

			std::string open(const std::string &fullPath,
				int flags, mode_t mode)
				throw (OGRSHException, IOException);

			std::string duplicate(const std::string &fileDesc)
				throw (OGRSHException, IOException);

			ssize_t read(const std::string &fileDesc,
				void *buf, size_t length)
				throw (OGRSHException, IOException);
			ssize_t write(const std::string &fileDesc,
				const void *buf, size_t length)
				throw (OGRSHException, IOException);
			off64_t lseek64(const std::string &fileDesc,
				off64_t offset, int whence)
				throw (OGRSHException, IOException);
			int truncate64(const std::string &fileDesc,
				off64_t length) throw (OGRSHException, IOException);

			void close(const std::string &fileDesc)
				throw (OGRSHException, IOException);
			int unlink(const std::string &fullPath)
				throw (OGRSHException, IOException);

			StatBuffer fxstat(const std::string &fileDesc)
				throw (OGRSHException, IOException);
	};
}

#endif
