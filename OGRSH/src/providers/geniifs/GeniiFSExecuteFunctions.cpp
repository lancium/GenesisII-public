#include <string>

#include <errno.h>

#include "ogrsh/Logging.hpp"

#include "ogrsh/ExecuteFunctions.hpp"

#include "providers/geniifs/GeniiFSExecuteFunctions.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		GeniiFSExecuteFunctions::GeniiFSExecuteFunctions(
			GeniiFSSession *session, GeniiFSMount *mount,
				const std::string &rnsSource)
			: _rnsSource(rnsSource)
		{
			_session = session;
			_mount = mount;
		}

		int GeniiFSExecuteFunctions::execve(ExecuteState *eState,
			const ogrsh::Path &relativePath,
			char *const argv[], char *const envp[])
		{
			OGRSH_FATAL("Attempt to execute a file in GenesisII space.");

			errno = ENOEXEC;
			return -1;
		}
	}
}
