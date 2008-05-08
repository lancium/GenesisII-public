#include <stdio.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "ogrsh/FileDescriptor.hpp"
#include "ogrsh/Logging.hpp"

#include "jcomm/DirectoryClient.hpp"

#include "providers/geniifs/GeniiFSDirectoryDescriptor.hpp"
#include "providers/geniifs/GeniiFSDirectoryStream.hpp"
#include "providers/geniifs/GeniiFSMount.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		GeniiFSDirectoryDescriptor::GeniiFSDirectoryDescriptor(
			GeniiFSSession *session, GeniiFSMount *mount, 
				const std::string &fullpath)
			: ogrsh::FileDescriptor(), _fullpath(fullpath)
		{
			_session = session;
			_mount = mount;
		}

		GeniiFSDirectoryDescriptor::~GeniiFSDirectoryDescriptor()
		{
		}

		FileDescriptor* GeniiFSDirectoryDescriptor::dup(int newfd)
		{
			OGRSH_FATAL("GeniiFSDirectoryDescriptor::dup(" << getFD()
				<< ", " << newfd << ") not implemented.");
			ogrsh::shims::real_exit(1);

			return NULL;
		}

		ssize_t GeniiFSDirectoryDescriptor::read(void *buf, size_t count)
		{
			OGRSH_FATAL("GeniiFSDirectoryDescriptor::read(" << _fullpath
				<< ", ..., "
				<< count << ") not permitted");
			ogrsh::shims::real_exit(1);

			return -1;
		}

		ssize_t GeniiFSDirectoryDescriptor::write(const void *buf, size_t count)
		{
			OGRSH_FATAL("GeniiFSDirectoryDescriptor::write(" << _fullpath
				<< ", ..., "
				<< count << ") not permitted");
			ogrsh::shims::real_exit(1);

			return -1;
		}

		off64_t GeniiFSDirectoryDescriptor::lseek64(off64_t offset, int whence)
		{
			OGRSH_FATAL("GeniiFSDirectoryDescriptor::lseek64(" << _fullpath
				<< ", " << offset << ", " << whence << ") not permitted.");
			ogrsh::shims::real_exit(1);

			return -1;
		}

		int GeniiFSDirectoryDescriptor::__fxstat(int version, struct stat *statbuf)
		{
			OGRSH_TRACE("GeniiFSDirectoryDescriptor::__fxstat(" << version
				<< ", " << _fullpath << ", ...) called.");

			jcomm::DirectoryClient client(*(_session->getSocket()));

			try
			{
				jcomm::StatBuffer statBuf = client.xstat(_fullpath);

				statbuf->st_dev = _mount->getDeviceID();
				statbuf->st_ino = statBuf.st_ino;
				statbuf->st_mode = statBuf.st_mode;
				statbuf->st_nlink = 0;
				statbuf->st_uid = 0;
				statbuf->st_gid = 0;
				statbuf->st_rdev = 0;
				statbuf->st_size = statBuf.st_size;
				statbuf->st_blksize = statBuf.st_blocksize;
				statbuf->st_blocks = (statBuf.st_size +511) / 512;
				statbuf->st_atime = statBuf._st_atime;
				statbuf->st_mtime = statBuf._st_mtime;
				statbuf->st_ctime = statBuf._st_ctime;
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return -1;
			}

			return 0;
		}

		int GeniiFSDirectoryDescriptor::__fxstat64(int version,
			struct stat64 *statbuf)
		{
			OGRSH_TRACE("GeniiFSDirectoryDescriptor::__fxstat64(" << version
				<< ", " << _fullpath << ", ...) called.");

			jcomm::DirectoryClient client(*(_session->getSocket()));

			try
			{
				jcomm::StatBuffer statBuf = client.xstat(_fullpath);

				statbuf->st_dev = _mount->getDeviceID();
				statbuf->st_ino = statBuf.st_ino;
				statbuf->st_mode = statBuf.st_mode;
				statbuf->st_nlink = 0;
				statbuf->st_uid = 0;
				statbuf->st_gid = 0;
				statbuf->st_rdev = 0;
				statbuf->st_size = statBuf.st_size;
				statbuf->st_blksize = statBuf.st_blocksize;
				statbuf->st_blocks = (statBuf.st_size +511) / 512;
				statbuf->st_atime = statBuf._st_atime;
				statbuf->st_mtime = statBuf._st_mtime;
				statbuf->st_ctime = statBuf._st_ctime;
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return -1;
			}

			return 0;
		}

		int GeniiFSDirectoryDescriptor::fcntl(int cmd, long arg)
		{
			OGRSH_TRACE("GeniiFSDirectoryDescriptor::fcntl(" << _fullpath
				<< ", " << cmd << ", ...) called.");

			switch (cmd)
			{
				case F_GETFD :
					return FD_CLOEXEC;
					break;
				default :
					OGRSH_FATAL("GeniiFSDirectoryDescriptor::fcntl(cmd = "
						<< cmd << ") is unimplemented.");
					ogrsh::shims::real_exit(1);
			}

			return -1;
		}

		int GeniiFSDirectoryDescriptor::fsync()
		{
			OGRSH_TRACE("GeniiFSDirectoryDescriptor::fsync(" << _fullpath
				<< ") called.");

			errno = EINVAL;
			return -1;
		}

		int GeniiFSDirectoryDescriptor::ftruncate64(off64_t length)
		{
			OGRSH_TRACE("GeniiFSDirectoryDescriptor::ftruncate64(" << _fullpath
				<< ", " << length << ") called.");

			errno = EINVAL;
			return -1;
		}

		int GeniiFSDirectoryDescriptor::fchmod(mode_t mode)
		{
			// TODO We probalby ought to implement this in genesis II.
			OGRSH_TRACE("GeniiFSDirectoryDescriptor::fchmod(" << _fullpath
				<< ", " << mode << ") called.");

			errno = EPERM;
			return -1;
		}

		ogrsh::DirectoryStream* GeniiFSDirectoryDescriptor::opendir()
		{
			OGRSH_TRACE("GeniiFSDirectoryDescriptor::opendir() called.");

			jcomm::DirectoryClient dc(*_session->getSocket());

			try
			{
				std::string directoryKey = dc.opendir(_fullpath);

				return new GeniiFSDirectoryStream(_session, _mount,
					directoryKey, this);
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return NULL;
			}

			return NULL;
		}
	}
}
