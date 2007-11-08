#include <string>

#include <errno.h>

#include "ogrsh/Logging.hpp"

#include "providers/geniifs/GeniiFSACLFunctions.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		GeniiFSACLFunctions::GeniiFSACLFunctions(
			GeniiFSSession *session, const std::string &rnsSource)
			: _rnsSource(rnsSource)
		{
			_session = session;
		}

		acl_t GeniiFSACLFunctions::acl_get_file(const Path &relativePath,
			acl_type_t type)
		{
			// TODO -- For now, we don't support acls in the file system
			errno = ENOTSUP;
			return NULL;
		}

		int GeniiFSACLFunctions::acl_extended_file(const Path &relativePath)
		{
			// TODO -- For now, we don't support acls in the file system
			return 0;
		}

		int GeniiFSACLFunctions::access(const Path &relativePath, int mode)
		{
			// TODO -- For right now, pass everything through.
			return 0;
		}

		int GeniiFSACLFunctions::eaccess(const Path &relativePath, int mode)
		{
			// TODO -- For right now, pass everything through.
			return 0;
		}

		int GeniiFSACLFunctions::euidaccess(const Path &relativePath, int mode)
		{
			// TODO -- For right now, pass everything through.
			return 0;
		}
	}
}
