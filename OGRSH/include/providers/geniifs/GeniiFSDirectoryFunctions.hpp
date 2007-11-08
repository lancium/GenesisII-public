#ifndef __GENIIFS_DIRECTORY_FUNCTIONS_HPP__
#define __GENIIFS_DIRECTORY_FUNCTIONS_HPP__

#include <string>

#include "ogrsh/DirectoryStream.hpp"
#include "ogrsh/DirectoryFunctions.hpp"

#include "providers/geniifs/GeniiFSSession.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		class GeniiFSMount;

		class GeniiFSDirectoryFunctions : public ogrsh::DirectoryFunctions
		{
			private:
				GeniiFSSession *_session;
				std::string _rnsSource;
				GeniiFSMount *_mount;

			public:
				GeniiFSDirectoryFunctions(GeniiFSSession *session,
					GeniiFSMount *mount, const std::string &rnsSource);

				virtual int chdir(const ogrsh::Path &relativePath);
				virtual int mkdir(const ogrsh::Path &relativePath, mode_t mode);
				virtual int rmdir(const ogrsh::Path &relativePath);
				virtual int chmod(const Path &relativePath, mode_t mode);

				virtual int link(const ogrsh::Path &fullOldPath,
					const ogrsh::Path &fullNewPath);
				virtual int rename(const ogrsh::Path &fullOldPath,
					const ogrsh::Path &fullNewPath);

				virtual ogrsh::DirectoryStream* opendir(
					const ogrsh::Path &relativePath);
				virtual int __xstat(int version,
					const ogrsh::Path &relativePath, struct stat *statbuf);
				virtual int __xstat64(int version,
					const ogrsh::Path &relativePath, struct stat64 *statbuf);
				virtual int __lxstat(int version,
					const ogrsh::Path &relativePath, struct stat *statbuf);
				virtual int __lxstat64(int version,
					const ogrsh::Path &relativePath, struct stat64 *statbuf);
				virtual int readlink(const Path &relativePath,
					char *buf, size_t bufsize);
		};
	}
}

#endif
