#ifndef __GENII_FS_FILE_DESCRIPTOR_HPP__
#define __GENII_FS_FILE_DESCRIPTOR_HPP__

#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "ogrsh/FileDescriptor.hpp"

#include "providers/geniifs/GeniiFSSession.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		class GeniiFSMount;

		class GeniiFSFileDescriptor : public FileDescriptor
		{
			private:
				std::string _fileDesc;
				GeniiFSSession *_session;
				GeniiFSMount *_mount;

			public:
				GeniiFSFileDescriptor(GeniiFSSession *session,
					GeniiFSMount *mount,
					const std::string &fileDesc);
				virtual ~GeniiFSFileDescriptor();

				virtual ssize_t read(void *buf, size_t count);
				virtual ssize_t write(const void *buf, size_t count);

				virtual off64_t lseek64(off64_t offset, int whence);

				virtual int __fxstat(int version, struct stat *statbuf);
				virtual int __fxstat64(int version, struct stat64 *statbuf);

				virtual int fcntl(int cmd, long arg);

				virtual ogrsh::DirectoryStream* opendir();
		};
	}
}

#endif
