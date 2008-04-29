#ifndef __VIRTUAL_FILE_DESCRIPTOR_HPP__
#define __VIRTUAL_FILE_DESCRIPTOR_HPP__

#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "ogrsh/FileDescriptor.hpp"

namespace ogrsh
{
	class VirtualFileDescriptor : public FileDescriptor
	{
		public:
			VirtualFileDescriptor();
			virtual ~VirtualFileDescriptor();

			virtual ssize_t read(void *buf, size_t count);
			virtual ssize_t write(const void *buf, size_t count);

			virtual off64_t lseek64(off64_t offset, int whence);

			virtual int __fxstat(int version, struct stat *statbuf);
			virtual int __fxstat64(int version, struct stat64 *statbuf);

			virtual int fcntl(int cmd, long arg);
			virtual int fsync();
			virtual int ftruncate64(off64_t length);
			virtual int fchmod(mode_t mode);

			virtual DirectoryStream* opendir();
	};
}

#endif
