#include <stdio.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "ogrsh/Logging.hpp"
#include "ogrsh/VirtualFileDescriptor.hpp"

namespace ogrsh
{
	VirtualFileDescriptor::VirtualFileDescriptor()
	{
	}

	VirtualFileDescriptor::~VirtualFileDescriptor()
	{
	}

	ssize_t VirtualFileDescriptor::read(void *buf, size_t count)
	{
		OGRSH_FATAL("Not allowed read from VirtualFileDescriptors.");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	ssize_t VirtualFileDescriptor::write(const void *buf, size_t count)
	{
		OGRSH_FATAL("Not allowed write to VirtualFileDescriptors.");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	off64_t VirtualFileDescriptor::lseek64(off64_t offset, int whence)
	{
		OGRSH_FATAL("Not allowed write to VirtualFileDescriptors.");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	int VirtualFileDescriptor::__fxstat(int version, struct stat *statbuf)
	{
		statbuf->st_dev = 0;		// TODO -- we need a better system
		statbuf->st_ino = 0;		// TODO -- we need a better system
		statbuf->st_mode = S_IFDIR | S_IRUSR | S_IXUSR |
			S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH;
		statbuf->st_nlink = 0;
		statbuf->st_uid = 0;
		statbuf->st_gid = 0;
		statbuf->st_rdev = 0;
		statbuf->st_size = 0;
		statbuf->st_blksize = (1024 * 4);
		statbuf->st_blocks = 1;
		statbuf->st_atime = time(NULL);
		statbuf->st_mtime = time(NULL);
		statbuf->st_ctime = time(NULL);

		return 0;
	}

	int VirtualFileDescriptor::__fxstat64(int version, struct stat64 *statbuf)
	{
		statbuf->st_dev = 0;		// TODO -- we need a better system
		statbuf->st_ino = 0;		// TODO -- we need a better system
		statbuf->st_mode = S_IFDIR | S_IRUSR | S_IXUSR |
			S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH;
		statbuf->st_nlink = 0;
		statbuf->st_uid = 0;
		statbuf->st_gid = 0;
		statbuf->st_rdev = 0;
		statbuf->st_size = 0;
		statbuf->st_blksize = (1024 * 4);
		statbuf->st_blocks = 1;
		statbuf->st_atime = time(NULL);
		statbuf->st_mtime = time(NULL);
		statbuf->st_ctime = time(NULL);

		return 0;
	}

	int VirtualFileDescriptor::fcntl(int cmd, long arg)
	{
		OGRSH_FATAL(
			"Not allowed to call fcntl on VirtualFileDescriptor instanecs.");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	int VirtualFileDescriptor::fsync()
	{
		errno = EROFS;
		return -1;
	}

	DirectoryStream* VirtualFileDescriptor::opendir()
	{
		OGRSH_FATAL(
			"Not allowed to call opendir on a filedescriptor in "
			<< "VirtualFileDescriptor instances.");
		ogrsh::shims::real_exit(1);

		return NULL;
	}
}
