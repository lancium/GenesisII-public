#ifndef ___VIRTUAL_MOUNT_HPP__
#define ___VIRTUAL_MOUNT_HPP__

#include <string>

#include "ogrsh/Mount.hpp"
#include "ogrsh/MountTree.hpp"
#include "ogrsh/DirectoryFunctions.hpp"
#include "ogrsh/FileFunctions.hpp"
#include "ogrsh/ExecuteFunctions.hpp"
#include "ogrsh/ACLFunctions.hpp"

namespace ogrsh
{
	class VirtualMount : public Mount
	{
		private:
			DirectoryFunctions *_dirFunctions;
			ACLFunctions *_aclFunctions;
			FileFunctions *_fileFunctions;
			ExecuteFunctions *_executeFunctions;

		public:
			VirtualMount(MountTree *mountTree);
			virtual ~VirtualMount();

			virtual DirectoryFunctions* getDirectoryFunctions();
			virtual ACLFunctions* getACLFunctions();
			virtual FileFunctions* getFileFunctions();
			virtual ExecuteFunctions* getExecuteFunctions();
	};
}

#endif
