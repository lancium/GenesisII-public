#include <stdio.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "ogrsh/FileDescriptor.hpp"
#include "ogrsh/Logging.hpp"

#include "jcomm/FileClient.hpp"

#include "providers/geniifs/GeniiFSFileDescriptor.hpp"
#include "providers/geniifs/GeniiFSMount.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		GeniiFSFileDescriptor::GeniiFSFileDescriptor(
			GeniiFSSession *session, GeniiFSMount *mount, 
				const std::string &fd)
			: ogrsh::FileDescriptor(), _fileDesc(fd)
		{
			_session = session;
			_mount = mount;
		}

		GeniiFSFileDescriptor::~GeniiFSFileDescriptor()
		{
			OGRSH_DEBUG("Calling close on open GeniFS File \""
				<< _fileDesc << "\".");

			jcomm::FileClient client(*(_session->getSocket()));
			client.close(_fileDesc);
		}

		ssize_t GeniiFSFileDescriptor::read(void *buf, size_t count)
		{
			OGRSH_TRACE("GeniiFSFileDescriptor::read(" << _fileDesc
				<< ", ..., "
				<< count << ").");

			jcomm::FileClient client(*(_session->getSocket()));
			return client.read(_fileDesc, buf, count);
		}

		ssize_t GeniiFSFileDescriptor::write(const void *buf, size_t count)
		{
			OGRSH_TRACE("GeniiFSFileDescriptor::write(" << _fileDesc
				<< ", ..., "
				<< count << ").");

			jcomm::FileClient client(*(_session->getSocket()));
			return client.write(_fileDesc, buf, count);
		}

		off64_t GeniiFSFileDescriptor::lseek64(off64_t offset, int whence)
		{
			OGRSH_TRACE("GeniiFSFileDescriptor::lseek64(" << _fileDesc
				<< ", " << offset << ", " << whence << "). called.");

			jcomm::FileClient client(*(_session->getSocket()));
			return client.lseek64(_fileDesc, offset, whence);
		}

		int GeniiFSFileDescriptor::__fxstat(int version, struct stat *statbuf)
		{
			OGRSH_TRACE("GeniiFSFileDescriptor::__fxstat(" << version
				<< ", " << _fileDesc << ", ...) called.");

			jcomm::FileClient client(*(_session->getSocket()));

			try
			{
				jcomm::StatBuffer statBuf = client.fxstat(_fileDesc);

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

		int GeniiFSFileDescriptor::__fxstat64(int version,
			struct stat64 *statbuf)
		{
			OGRSH_TRACE("GeniiFSFileDescriptor::__fxstat64(" << version
				<< ", " << _fileDesc << ", ...) called.");

			jcomm::FileClient client(*(_session->getSocket()));

			try
			{
				jcomm::StatBuffer statBuf = client.fxstat(_fileDesc);

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

		int GeniiFSFileDescriptor::fcntl(int cmd, long arg)
		{
			OGRSH_TRACE("GeniiFSFileDescriptor::fcntl(" << _fileDesc
				<< ", " << cmd << ", ...) called.");

			switch (cmd)
			{
				case F_GETFD :
					return FD_CLOEXEC;
					break;
				default :
					OGRSH_FATAL("GeniiFSFileDescriptor::fcntl(cmd = "
						<< cmd << ") is unimplemented.");
					ogrsh::shims::real_exit(1);
			}

			return -1;
		}

		int GeniiFSFileDescriptor::fsync()
		{
			OGRSH_TRACE("GeniiFSFileDescriptor::fsync(" <<
				_fileDesc << ") called.");

			// Genesis II files are automatically flushed at this level.
			return 0;
		}

		ogrsh::DirectoryStream* GeniiFSFileDescriptor::opendir()
		{
			OGRSH_FATAL("GeniiFSFileDescriptor::opendir() -- This operation "
				<< "can be called if a web instance implements BOTH rns and "
				<< "byteio, but we aren't going to handle that case "
				<< "right now.");
			ogrsh::shims::real_exit(1);

			return NULL;
		}
	}
}
