#ifndef __ACL_FUNCTIONS_HPP__
#define __ACL_FUNCTIONS_HPP__

#include <sys/types.h>
#include <acl/libacl.h>

#include <string>

#include "ogrsh/Path.hpp"

namespace ogrsh
{
	class ACLFunctions
	{
		private:
			ACLFunctions(const ACLFunctions&);
			ACLFunctions& operator= (const ACLFunctions&);

		protected:
			ACLFunctions();

		public:
			virtual ~ACLFunctions();

			virtual int access(const Path &relativePath, int mode) = 0;
			virtual int eaccess(const Path &relativePath, int mode) = 0;
			virtual int euidaccess(const Path &relativePath, int mode) = 0;

			virtual int acl_extended_file(const Path &relativePath) = 0;
			virtual acl_t acl_get_file(const Path &path, acl_type_t type) = 0;
	};
}

#endif
