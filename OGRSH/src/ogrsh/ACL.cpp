#include <dlfcn.h>
#include <sys/types.h>
#include <acl/libacl.h>

#include "ogrsh/Configuration.hpp"
#include "ogrsh/Logging.hpp"

#include "ogrsh/shims/ACL.hpp"

using namespace ogrsh;

namespace ogrsh
{
	namespace shims
	{
		SHIM_DEF(int, acl_free, (void *objp), (objp))
		{
			OGRSH_TRACE("acl_free(...) called.  Warning, this MUST be "
				<< "re-done if we ever support ACL in grid file system.");

			return real_acl_free(objp);
		}

		SHIM_DEF(int, acl_extended_file, (const char *path), (path))
		{
			OGRSH_TRACE("acl_extended_file(\"" << path << "\") called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getACLFunctions()->acl_extended_file(fullPath);
		}

		SHIM_DEF(int, access, (const char *path, int mode), (path, mode))
		{
			OGRSH_TRACE("access(\"" << path << "\", ...) called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getACLFunctions()->access(fullPath, mode);
		}

		SHIM_DEF(int, eaccess, (const char *path, int mode), (path, mode))
		{
			OGRSH_TRACE("eaccess(\"" << path << "\", ...) called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getACLFunctions()->eaccess(fullPath, mode);
		}

		SHIM_DEF(int, euidaccess, (const char *path, int mode), (path, mode))
		{
			OGRSH_TRACE("euidaccess(\"" << path << "\", ...) called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getACLFunctions()->euidaccess(fullPath, mode);
		}

		SHIM_DEF(acl_t, acl_get_file, (const char *path, acl_type_t type),
			(path, type))
		{
			OGRSH_TRACE("acl_get_file(\"" << path << "\", ...) called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getACLFunctions()->acl_get_file(fullPath, type);
		}

		void startACLShims()
		{
			START_SHIM(acl_get_file);
			START_SHIM(acl_extended_file);
			START_SHIM(acl_free);
			START_SHIM(access);
			START_SHIM(eaccess);
			START_SHIM(euidaccess);
		}

		void stopACLShims()
		{
			STOP_SHIM(acl_get_file);
			STOP_SHIM(acl_extended_file);
			STOP_SHIM(acl_free);
			STOP_SHIM(euidaccess);
			STOP_SHIM(eaccess);
			STOP_SHIM(access);
		}
	}
}

