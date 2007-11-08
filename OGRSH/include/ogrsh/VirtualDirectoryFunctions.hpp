#ifndef __VIRTUAL_DIRECTORY_FUNCTIONS_HPP__
#define __VIRTUAL_DIRECTORY_FUNCTIONS_HPP__

#include <string>

#include "ogrsh/DirectoryStream.hpp"
#include "ogrsh/DirectoryFunctions.hpp"
#include "ogrsh/MountTree.hpp"

namespace ogrsh
{
	class VirtualDirectoryFunctions : public DirectoryFunctions
	{
		private:
			MountTree *_mountTree;

		public:
			VirtualDirectoryFunctions(MountTree *mountTree);

			virtual int chdir(const Path &relativePath);
			virtual int mkdir(const Path &relativePath, mode_t mode);
			virtual int rmdir(const Path &relativePath);
			virtual int chmod(const Path &relativePath, mode_t mode);

			virtual int link(const Path &fullOldPath,
				const Path &fullNewPath);
			virtual int rename(const Path &fullOldPath,
				const Path &fullNewPath);

			virtual DirectoryStream* opendir(
				const Path &relativePath);
			virtual int __xstat(int version,
				const Path &relativePath, struct stat*);
			virtual int __xstat64(int version,
				const Path &relativePath, struct stat64*);
			virtual int __lxstat(int version,
				const Path &relativePath, struct stat*);
			virtual int __lxstat64(int version,
				const Path &relativePath, struct stat64*);
			virtual int readlink(const Path &relativePath,
				char *buf, size_t bufsize);
	};
}

#endif
