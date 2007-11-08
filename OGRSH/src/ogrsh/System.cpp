#include <dlfcn.h>

#include "ogrsh/Logging.hpp"
#include "ogrsh/shims/System.hpp"

namespace ogrsh
{
	namespace shims
	{
		SHIM_DEF(void, exit, (int exitCode), (exitCode))
		{
			OGRSH_TRACE("exit(" << exitCode << ") intercepted.");
			real_exit(exitCode);
		}

		void startSystemShims()
		{
			START_SHIM(exit);
		}

		void stopSystemShims()
		{
			STOP_SHIM(exit);
		}
	}
}
