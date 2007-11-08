#ifndef __GENIIFS_MOUNT_HPP__
#define __GENIIFS_MOUNT_HPP__

#include <string>

#include "ogrsh/Mount.hpp"
#include "ogrsh/DirectoryFunctions.hpp"
#include "ogrsh/FileFunctions.hpp"
#include "ogrsh/ExecuteFunctions.hpp"
#include "ogrsh/ACLFunctions.hpp"

#include "providers/geniifs/GeniiFSSession.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		class GeniiFSMount : public ogrsh::Mount
		{
			private:
				ogrsh::DirectoryFunctions *_dirFunctions;
				ogrsh::ACLFunctions *_aclFunctions;
				ogrsh::FileFunctions *_fileFunctions;
				ogrsh::ExecuteFunctions *_executeFunctions;

			public:
				GeniiFSMount(GeniiFSSession*, const std::string &location,
					const std::string &sourceLocation);
				virtual ~GeniiFSMount();

				virtual ogrsh::DirectoryFunctions* getDirectoryFunctions();
				virtual ogrsh::ACLFunctions* getACLFunctions();
				virtual ogrsh::FileFunctions* getFileFunctions();
				virtual ogrsh::ExecuteFunctions* getExecuteFunctions();
		};
	}
}

#endif
