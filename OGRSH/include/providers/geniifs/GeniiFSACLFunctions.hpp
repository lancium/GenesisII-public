#ifndef __GENIIFS_ACL_FUNCTIONS_HPP__
#define __GENIIFS_ACL_FUNCTIONS_HPP__

#include <string>

#include "providers/geniifs/GeniiFSSession.hpp"
#include "ogrsh/ACLFunctions.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		class GeniiFSACLFunctions : public ogrsh::ACLFunctions
		{
			private:
				GeniiFSSession *_session;
				std::string _rnsSource;

			public:
				GeniiFSACLFunctions(GeniiFSSession *session,
					const std::string &rnsSource);

				virtual int acl_extended_file(const Path &relativePath);
				virtual int access(const Path &relativePath, int mode);
				virtual int eaccess(const Path &relativePath, int mode);
				virtual int euidaccess(const Path &relativePath, int mode);

				virtual acl_t acl_get_file(const Path &path, acl_type_t type);
		};
	}
}

#endif
