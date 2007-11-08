#ifndef __GENIIFS_DIRECTORY_STREAM_HPP__
#define __GENIIFS_DIRECTORY_STREAM_HPP__

#include <string>
#include <dirent.h>

#include "ogrsh/DirectoryStream.hpp"

#include "providers/geniifs/GeniiFSSession.hpp"
#include "providers/geniifs/GeniiFSDirectoryDescriptor.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		class GeniiFSMount;

		class GeniiFSDirectoryStream : public ogrsh::DirectoryStream
		{
			private:
				dirent _dent;
				dirent64 _dent64;

				GeniiFSSession *_session;
				GeniiFSMount *_mount;
				std::string _directoryKey;

			public:
				GeniiFSDirectoryStream(GeniiFSSession *session,
					GeniiFSMount *mount, const std::string &directoryKey,
					const std::string &fullpath);
				GeniiFSDirectoryStream(
					GeniiFSSession *session, GeniiFSMount *mount,
					const std::string &directoryKey,
					GeniiFSDirectoryDescriptor *desc);

				virtual ~GeniiFSDirectoryStream();

				virtual dirent* readdir();
				virtual dirent64* readdir64();

				virtual int dirfd();
		};
	}
}

#endif
