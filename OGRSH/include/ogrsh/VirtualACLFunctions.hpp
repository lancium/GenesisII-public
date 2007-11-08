#ifndef __VIRTUAL_ACL_FUNCTIONS_HPP__
#define __VIRTUAL_ACL_FUNCTIONS_HPP__

#include <string>

#include "ogrsh/ACLFunctions.hpp"
#include "ogrsh/MountTree.hpp"

namespace ogrsh
{
	class VirtualACLFunctions : public ACLFunctions
	{
		private:
			MountTree *_mountTree;

		public:
			VirtualACLFunctions(MountTree *mountTree);

			virtual int acl_extended_file(const Path &relativePath);
			virtual int access(const Path &relativePath, int mode);
			virtual int eaccess(const Path &relativePath, int mode);
			virtual int euidaccess(const Path &relativePath, int mode);

			virtual acl_t acl_get_file(const Path &path, acl_type_t type);
	};
}

#endif
