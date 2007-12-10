#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "ogrsh/FileDescriptor.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/shims/File.hpp"
#include "ogrsh/shims/Directory.hpp"

#include "providers/localfs/LocalFSFileDescriptor.hpp"
#include "providers/localfs/LocalFSDirectoryStream.hpp"

namespace ogrsh
{
	namespace localfs
	{
		LocalFSFileDescriptor::LocalFSFileDescriptor(int fd, bool mustClose)
			: ogrsh::FileDescriptor(fd, mustClose)
		{
		}

		ssize_t LocalFSFileDescriptor::read(void *buf, size_t count)
		{
			OGRSH_TRACE("LocalFSFileDescriptor::read(" << getFD() << ", ..., "
				<< count << ") called.");

			ssize_t ret = ogrsh::shims::real_read(getFD(), buf, count);
			OGRSH_TRACE("LocalFSFileDescriptor::read -- read "
				<< ret << " bytes.");
			return ret;
		}

		ssize_t LocalFSFileDescriptor::write(const void *buf, size_t count)
		{
			OGRSH_TRACE("LocalFSFileDescriptor::write(" << getFD() << ", ..., "
				<< count << ") called.");
			
			return ogrsh::shims::real_write(getFD(), buf, count);
		}

		off64_t LocalFSFileDescriptor::lseek64(off64_t offset, int whence)
		{
			OGRSH_TRACE("LocalFSFileDescriptor::lseek64(" << getFD() <<
				", " << offset << ", " << whence << ") called.");
			
			return ogrsh::shims::real_lseek64(getFD(), offset, whence);
		}

		int LocalFSFileDescriptor::__fxstat(int version, struct stat *statbuf)
		{
			OGRSH_TRACE("LocalFSFileDescriptor::__fxstat(" << getFD()
				<< ", ...) called.");

			return ogrsh::shims::real___fxstat(version, getFD(), statbuf);
		}

		int LocalFSFileDescriptor::__fxstat64(int version,
			struct stat64 *statbuf)
		{
			OGRSH_TRACE("LocalFSFileDescriptor::__fxstat64(" << getFD()
				<< ", ...) called.");

			return ogrsh::shims::real___fxstat64(version, getFD(), statbuf);
		}

		int LocalFSFileDescriptor::fcntl(int cmd, long arg)
		{
			int fd = getFD();

			OGRSH_TRACE("LocalFSFileDescriptor::fcntl(" << fd
				<< ", " << cmd << ", ...) called.");

			int ret = ogrsh::shims::real_fcntl(fd, cmd, arg);
			OGRSH_TRACE("LocalFSFileDescriptor::fcntl returning " << ret);
			return ret;
		}

		int LocalFSFileDescriptor::fsync()
		{
			int fd = getFD();

			OGRSH_TRACE("LocalFSFileDescriptor::fsync(" << fd << ") called.");

			int ret = ogrsh::shims::real_fsync(fd);
			OGRSH_TRACE("LocalFSFileDescriptor::fsync returning " << ret);
			return ret;
		}

		int LocalFSFileDescriptor::fchmod(mode_t mode)
		{
			int fd = getFD();

			OGRSH_TRACE("LocalFSFileDescriptor::fchmod(" << mode
				<< ") called.");

			return ogrsh::shims::real_fchmod(fd, mode);
		}

		ogrsh::DirectoryStream* LocalFSFileDescriptor::opendir()
		{
			OGRSH_TRACE("LocalFSFileDescriptor::opendir() called.");

			LocalFSDirectoryStream *ret =
				new LocalFSDirectoryStream(this);
			if (!ret->isValid())
			{
				delete ret;
				ret = NULL;
			}

			return ret;
		}
	}
}
