#include <string>

#include "ogrsh/ACLFunctions.hpp"
#include "ogrsh/Logging.hpp"

namespace ogrsh
{
	ACLFunctions::ACLFunctions(const ACLFunctions&)
	{
		OGRSH_FATAL("Not allowed to copy acl functions.");
		ogrsh::shims::real_exit(1);
	}

	ACLFunctions& ACLFunctions::operator= (
		const ACLFunctions&)
	{
		OGRSH_FATAL("Not allowed to copy acl functions.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	ACLFunctions::ACLFunctions()
	{
	}

	ACLFunctions::~ACLFunctions()
	{
	}
}
