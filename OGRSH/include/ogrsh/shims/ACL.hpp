#ifndef __OGRSH_SHIMS_ACL_HPP__
#define __OGRSH_SHIMS_ACL_HPP__

#include <sys/types.h>
#include <acl/libacl.h>

#include "ogrsh/ShimMacros.hpp"

namespace ogrsh
{
	namespace shims
	{
		SHIM_DECL(int, acl_extended_file, (const char *path));
		SHIM_DECL(int, access, (const char *path, int mode));
		SHIM_DECL(int, eaccess, (const char *path, int mode));
		SHIM_DECL(int, euidaccess, (const char *path, int mode));
		SHIM_DECL(acl_t, acl_get_file, (const char *path, acl_type_t type));
		SHIM_DECL(int, acl_free, (void *objP));

		void startACLShims();
		void stopACLShims();
	}
}

#endif
