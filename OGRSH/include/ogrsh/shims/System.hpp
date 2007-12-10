#ifndef __OGRSH_SHIMS_SYSTEM_HPP__
#define __OGRSH_SHIMS_SYSTEM_HPP__

#include <stdlib.h>
#include <pwd.h>
#include <sys/types.h>

#include "ogrsh/ShimMacros.hpp"

namespace ogrsh
{
	namespace shims
	{
		SHIM_DECL(void, exit, (int exitCode));
		SHIM_DECL(struct passwd*, getpwnam, (const char *name));
		SHIM_DECL(struct passwd*, getpwuid, (uid_t uid));

		void startSystemShims();
		void stopSystemShims();
	}
}

#endif
