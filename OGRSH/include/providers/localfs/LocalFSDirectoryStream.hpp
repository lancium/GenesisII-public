#ifndef __LOCAL_FS_DIRECTORY_STREAM_HPP__
#define __LOCAL_FS_DIRECTORY_STREAM_HPP__

#include <string>

#include "ogrsh/DirectoryStream.hpp"
#include "providers/localfs/LocalFSFileDescriptor.hpp"

namespace ogrsh
{
	namespace localfs
	{
		class LocalFSDirectoryStream : public ogrsh::DirectoryStream
		{
			private:
				DIR *_dir;

			public:
				LocalFSDirectoryStream(const std::string&);
				LocalFSDirectoryStream(LocalFSFileDescriptor*);

				virtual ~LocalFSDirectoryStream();

				virtual dirent* readdir();
				virtual dirent64* readdir64();

				virtual int dirfd();

				bool isValid();
		};
	}
}

#endif
