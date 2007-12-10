#include <dlfcn.h>

#include "ogrsh/Logging.hpp"
#include "ogrsh/shims/System.hpp"

namespace ogrsh
{
	namespace shims
	{
		void startFileShims();
		void stopFileShims();

		SHIM_DEF(void, exit, (int exitCode), (exitCode))
		{
			OGRSH_TRACE("exit(" << exitCode << ") intercepted.");
			real_exit(exitCode);
		}

		SHIM_DEF(struct passwd*, getpwnam, (const char *name), (name))
		{
			struct passwd *ret;

			OGRSH_TRACE("getpwnam(\"" << name << "\" called.");

			stopFileShims();
			ret = real_getpwnam(name);
			startFileShims();

			return ret;
		}

		SHIM_DEF(struct passwd*, getpwuid, (uid_t uid), (uid))
		{
			struct passwd *ret;

			OGRSH_TRACE("getpwuid(" << uid << ") called.");

			stopFileShims();
			ret = real_getpwuid(uid);
			startFileShims();

			return ret;
		}

		void startSystemShims()
		{
			START_SHIM(exit);
			START_SHIM(getpwnam);
			START_SHIM(getpwuid);
		}

		void stopSystemShims()
		{
			STOP_SHIM(getpwuid);
			STOP_SHIM(getpwnam);
			STOP_SHIM(exit);
		}
	}
}
