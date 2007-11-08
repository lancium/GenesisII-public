#include "ogrsh/Configuration.hpp"
#include "ogrsh/EnvironmentVariables.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/Version.hpp"
#include "ogrsh/Random.hpp"

#include "ogrsh/shims/ACL.hpp"
#include "ogrsh/shims/Directory.hpp"
#include "ogrsh/shims/System.hpp"
#include "ogrsh/shims/File.hpp"
#include "ogrsh/shims/Execute.hpp"

namespace ogrsh
{
	static void fix_environment();

	void __attribute__((constructor)) initializeLibrary()
	{
		OGRSH_TRACE("Initializing OGRSH");

		initializeGenerators();

		fix_environment();

		Configuration::createConfiguration();

		ogrsh::shims::startSystemShims();
		ogrsh::shims::startDirectoryShims();
		ogrsh::shims::startACLShims();
		ogrsh::shims::startFileShims();
		ogrsh::shims::startExecuteShims();
	}

	void __attribute__((destructor)) finalizeLibrary()
	{
		OGRSH_TRACE("Finalizing OGRSH");

		Configuration::destroyConfiguration();

		ogrsh::shims::stopExecuteShims();
		ogrsh::shims::stopFileShims();
		ogrsh::shims::stopACLShims();
		ogrsh::shims::stopDirectoryShims();
		ogrsh::shims::stopSystemShims();
	}

	void fix_environment()
	{
		const char *ogrshConfig;
		char *variable;
		int length;
		int eIndex;

		// This is really stupid, but it turns out that sometimes (perhaps
		// always), there is a weird interaction between getenv and setenv
		// on UNIX systems where once you setenv, you can no longer getenv
		// on anything that was brought in with the program when it was
		// exec'd.  I suspect that this has to do with the legacy UNIX systems
		// only having getenv and putenv, the latter of which is mostly
		// useless.  Now we have setenv, but most likely there is some
		// initialization that we are bypassing by using it in an interception
		// or shim library.  However, to avoid unnecessary complications for
		// OS's where this isn't a problem (incase they exist), I'll go through
		// some effort here to determine whether or not things are broken.

		ogrshConfig = getenv("OGRSH_CONFIG");
		if (ogrshConfig == NULL)
		{
			OGRSH_FATAL(
				"Missing required environment variable \"OGRSH_CONFIG\".");

			// Using this exit here is safe since the shims haven't been
			// started yet.
			exit(1);
		}

		setenv(OGRSH_VERSION_VAR_NAME, OGRSH_VERSION, 1);
		ogrshConfig = getenv("OGRSH_CONFIG");
		if (ogrshConfig == NULL)
		{
			OGRSH_DEBUG("Looks like this operating system exhibits the "
				<< "broken environment behavior.  Attempting to fix.");

			char **iter = environ;
			while (*iter != NULL)
			{
				length = strlen(*iter);
				variable = new char[length + 1];
				strcpy(variable, *iter);
				for (eIndex = 0; eIndex < length; eIndex++)
				{
					if (variable[eIndex] == '=')
					{
						variable[eIndex] = (char)0;
						break;
					}
				}

				if (eIndex >= length)
				{
					OGRSH_FATAL("Found an environment variable ("
						<< *iter << ") which isn't of the form var=value.");
					exit(1);
				}

				setenv(variable, variable + eIndex + 1, 1);
				iter++;
			}
		}
	}
}
