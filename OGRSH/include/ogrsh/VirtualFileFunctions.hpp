#ifndef __VIRTUAL_FILE_FUNCTIONS_HPP__
#define __VIRTUAL_FILE_FUNCTIONS_HPP__

#include <string>

#include "ogrsh/FileDescriptor.hpp"
#include "ogrsh/FileFunctions.hpp"
#include "ogrsh/MountTree.hpp"

namespace ogrsh
{
	class VirtualFileFunctions : public FileFunctions
	{
		private:
			MountTree *_mountTree;

		public:
			VirtualFileFunctions(MountTree *mountTree);

			virtual FileDescriptor* open64(const Path &relativePath,
				int flags, mode_t mode);
			virtual FileDescriptor* creat(const Path &relativePath,
				mode_t mode);

			virtual int unlink(const Path &relativePath);
	};
}

#endif
