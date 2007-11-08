#ifndef __DIRECTORY_STREAM_HPP__
#define __DIRECTORY_STREAM_HPP__

#include <dirent.h>

#include "ogrsh/FileDescriptor.hpp"

namespace ogrsh
{
	class DirectoryStream
	{
		private:
			FileDescriptor *_fd;
			
			DirectoryStream(const DirectoryStream&);
			DirectoryStream& operator= (const DirectoryStream&);

		protected:
			DirectoryStream(FileDescriptor*);

		public:
			virtual ~DirectoryStream();

			void setFileDescriptor(FileDescriptor*);
			FileDescriptor* getFileDescriptor();

			virtual dirent* readdir() = 0;
			virtual dirent64* readdir64() = 0;

			virtual int dirfd() = 0;
	};
}

#endif
