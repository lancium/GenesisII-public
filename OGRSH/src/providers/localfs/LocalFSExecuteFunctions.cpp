#include <string>

#include "ogrsh/ExecuteFunctions.hpp"
#include "ogrsh/Logging.hpp"

#include "ogrsh/shims/Execute.hpp"

#include "providers/localfs/LocalFSExecuteFunctions.hpp"

namespace ogrsh
{
	namespace localfs
	{
		LocalFSExecuteFunctions::LocalFSExecuteFunctions(
			const std::string &localSource)
			: _localSource(localSource)
		{
		}

		int LocalFSExecuteFunctions::execve(ExecuteState *eState,
			const Path &relativePath, char *const argv[], char *const envp[])
		{
			OGRSH_DEBUG("LocalFSExecuteFunctions::execve(\""
				<< (const std::string&)relativePath << "\", ...) called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			OGRSH_DEBUG("Attempting to call execve(\""
				<< fullPath.c_str() << "\", ...).");

			// Before we actually do the execute, we need to set
			// an environment variable indicating that we have
			// executed a certain REAL path, and the virtual path to
			// which it belongs.  This is because if the thing we are
			// executing is a script, we'll come back in a little bit and
			// read it which will fail because we intercept read.
	
			eState->setRealPath(fullPath.c_str());
			return ogrsh::shims::real_execve(fullPath.c_str(),
				argv, eState->addReplaceEnvironment(envp));
		}
	}
}
