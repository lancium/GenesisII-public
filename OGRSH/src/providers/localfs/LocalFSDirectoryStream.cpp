#include <string>

#include "ogrsh/DirectoryStream.hpp"
#include "ogrsh/shims/Directory.hpp"

#include "providers/localfs/LocalFSDirectoryStream.hpp"
#include "providers/localfs/LocalFSFileDescriptor.hpp"

namespace ogrsh
{
	namespace localfs
	{
		LocalFSDirectoryStream::LocalFSDirectoryStream(const std::string &path)
			: DirectoryStream(NULL)
		{
			_dir = ogrsh::shims::real_opendir(path.c_str());
			if (_dir != NULL)
			{
				setFileDescriptor(new LocalFSFileDescriptor(
					ogrsh::shims::real_dirfd(_dir), false));
			}
		}

		LocalFSDirectoryStream::LocalFSDirectoryStream(
			LocalFSFileDescriptor *fd) : DirectoryStream(fd)
		{
			_dir = ogrsh::shims::real_fdopendir(fd->getFD());
		}

		LocalFSDirectoryStream::~LocalFSDirectoryStream()
		{
			if (_dir != NULL)
				ogrsh::shims::real_closedir(_dir);
		}

		dirent* LocalFSDirectoryStream::readdir()
		{
			return ogrsh::shims::real_readdir(_dir);
		}

		dirent64* LocalFSDirectoryStream::readdir64()
		{
			return ogrsh::shims::real_readdir64(_dir);
		}

		int LocalFSDirectoryStream::dirfd()
		{
			return getFileDescriptor()->getFD();
		}

		bool LocalFSDirectoryStream::isValid()
		{
			return _dir != NULL;
		}
	}
}
