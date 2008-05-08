#ifndef __DIRECTORY_CLIENT_HPP__
#define __DIRECTORY_CLIENT_HPP__

#include <sys/types.h>
#include <utime.h>

#include "jcomm/Socket.hpp"
#include "jcomm/IOException.hpp"
#include "jcomm/OGRSHException.hpp"
#include "jcomm/StatBuffer.hpp"
#include "jcomm/TimeValStructure.hpp"
#include "jcomm/DirectoryEntry.hpp"

namespace jcomm
{
	class DirectoryClient
	{
		private:
			Socket _socket;

		public:
			DirectoryClient(Socket socket);

			std::string opendir(const std::string &fullPath)
				throw (OGRSHException, IOException);
			DirectoryEntry* readdir(const std::string &directorySession)
				throw (OGRSHException, IOException);
			void rewinddir(const std::string &directorySession)
				throw (OGRSHException, IOException);
			int closedir(const std::string &directorySession)
				throw (OGRSHException, IOException);
			StatBuffer xstat(const std::string &fullPath)
				throw (OGRSHException, IOException);
			int utimes(const std::string &fullPath,
				const TimeValStructure &accessTime,
				const TimeValStructure &modTime)
				throw (OGRSHException, IOException);

			int link(const std::string &oldPath,
				const std::string &newPath)
				throw (OGRSHException, IOException);

			int chdir(const std::string &fullPath)
				throw (OGRSHException, IOException);
			int mkdir(const std::string &fullPath, int mode)
				throw (OGRSHException, IOException);
			int rmdir(const std::string &fullPath)
				throw (OGRSHException, IOException);
	};
}

#endif
