#ifndef __OGRSH_SHIMS_SYSTEM_HPP__
#define __OGRSH_SHIMS_SYSTEM_HPP__

#include <stdlib.h>

#include "ogrsh/ShimMacros.hpp"

namespace ogrsh
{
	namespace shims
	{
		SHIM_DECL(void, exit, (int exitCode));

		void startSystemShims();
		void stopSystemShims();
	}
}

#endif
