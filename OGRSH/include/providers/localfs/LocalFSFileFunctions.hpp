#ifndef __LOCALFS_FILE_FUNCTIONS_HPP__
#define __LOCALFS_FILE_FUNCTIONS_HPP__

#include <string>

#include "ogrsh/FileDescriptor.hpp"
#include "ogrsh/FileFunctions.hpp"

namespace ogrsh
{
	namespace localfs
	{
		class LocalFSFileFunctions : public ogrsh::FileFunctions
		{
			private:
				std::string _localSource;

			public:
				LocalFSFileFunctions(const std::string &localSource);

				FileDescriptor* open64(const Path &relativePath,
					int flags, mode_t mode);
				FileDescriptor* creat(const Path &relativePath,
					mode_t mode);
				int unlink(const Path &relativePath);
		};
	}
}

#endif
