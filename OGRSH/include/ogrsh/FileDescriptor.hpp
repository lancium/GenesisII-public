#ifndef __FILE_DESCRIPTOR_HPP__
#define __FILE_DESCRIPTOR_HPP__

#include <string>

#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dirent.h>

namespace ogrsh
{
	class DirectoryStream;

	class FileDescriptor
	{
		private:
			std::string _fullVirtualPath;
			bool _mustClose;
			int _fd;

			FileDescriptor(const FileDescriptor&);
			FileDescriptor& operator= (const FileDescriptor&);

		protected:
			FileDescriptor(int fd = -1, bool mustClose = true);

		public:
			virtual ~FileDescriptor();

			std::string getFullVirtualPath() const;
			void setFullVirtualPath(const std::string &fullVirtualPath);
			int getFD() const;

			virtual ssize_t read(void *buf, size_t count) = 0;
			virtual ssize_t write(const void *buf, size_t count) = 0;

			virtual off64_t lseek64(off64_t offset, int whence) = 0;

			virtual int __fxstat(int version, struct stat *statbuf) = 0;
			virtual int __fxstat64(int version, struct stat64 *statbuf) = 0;

			virtual int fcntl(int cmd, long arg) = 0;
			virtual int fsync() = 0;
			virtual int fchmod(mode_t mode) = 0;

			virtual DirectoryStream* opendir() = 0;
	};
}

#endif
