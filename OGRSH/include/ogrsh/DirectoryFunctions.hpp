#ifndef __DIRECTORY_FUNCTIONS_HPP__
#define __DIRECTORY_FUNCTIONS_HPP__

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <string>

#include "ogrsh/DirectoryStream.hpp"
#include "ogrsh/Path.hpp"

namespace ogrsh
{
	class DirectoryFunctions
	{
		private:
			DirectoryFunctions(const DirectoryFunctions&);
			DirectoryFunctions& operator= (const DirectoryFunctions&);

		protected:
			DirectoryFunctions();

		public:
			virtual ~DirectoryFunctions();

			virtual int chdir(const Path &relativePath) = 0;
			virtual int mkdir(const Path &relativePath, mode_t mode) = 0;
			virtual int rmdir(const Path &relativePath) = 0;
			virtual int chmod(const Path &relativePath, mode_t mode) = 0;

			virtual int link(const Path &fullOldPath,
				const Path &fullNewPath) = 0;
			virtual int rename(const Path &fullOldPath,
				const Path &fullNewPath) = 0;

			virtual DirectoryStream* opendir(
				const Path &relativePath) = 0;
			virtual int __xstat(int version, const Path &relativePath,
				struct stat *statbuf) = 0;
			virtual int __xstat64(int version, const Path &relativePath,
				struct stat64 *statbuf) = 0;
			virtual int __lxstat(int version, const Path &relativePath,
				struct stat *statbuf) = 0;
			virtual int __lxstat64(int version, const Path &relativePath,
				struct stat64 *statbuf) = 0;
			virtual int readlink(const Path &relativePath, char *buf,
				size_t bufsize) = 0;
	};
}

#endif
