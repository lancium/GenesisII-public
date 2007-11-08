#ifndef __LOCALFS_ACL_FUNCTIONS_HPP__
#define __LOCALFS_ACL_FUNCTIONS_HPP__

#include <string>

#include "ogrsh/ACLFunctions.hpp"

namespace ogrsh
{
	namespace localfs
	{
		class LocalFSACLFunctions : public ogrsh::ACLFunctions
		{
			private:
				std::string _localSource;

			public:
				LocalFSACLFunctions(const std::string &localSource);

				virtual int acl_extended_file(const Path &relativePath);
				virtual int access(const Path &relativePath, int mode);
				virtual int eaccess(const Path &relativePath, int mode);
				virtual int euidaccess(const Path &relativePath, int mode);

				virtual acl_t acl_get_file(const Path &path, acl_type_t type);
		};
	}
}

#endif
