#include <errno.h>
#include <dlfcn.h>
#include <sys/types.h>
#include <dirent.h>

#include "ogrsh/Configuration.hpp"
#include "ogrsh/ExecuteState.hpp"
#include "ogrsh/Logging.hpp"

#include "ogrsh/shims/Execute.hpp"

using namespace ogrsh;

namespace ogrsh
{
	namespace shims
	{
		SHIM_DEF(int, execve, (const char *filename, char *const argv[],
			char *const envp[]), (filename, argv, envp))
		{
			OGRSH_TRACE("execve(\"" << filename << "\", \""
				<< argv[0] << "\", ...) called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(filename);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();

			ExecuteState *eState = new ExecuteState();
			eState->setVirtualPath(((const std::string&)fullPath).c_str());
			int value = rootMount->getExecuteFunctions()->execve(eState,
				fullPath, argv, envp);
			delete eState;
			return value;
		}

		void startExecuteShims()
		{
			START_SHIM(execve);
		}

		void stopExecuteShims()
		{
			STOP_SHIM(execve);
		}
	}
}
