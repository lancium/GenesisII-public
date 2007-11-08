#ifndef __GENIIFS_EXECUTE_FUNCTIONS_HPP__
#define __GENIIFS_EXECUTE_FUNCTIONS_HPP__

#include <string>

#include "ogrsh/ExecuteFunctions.hpp"

#include "providers/geniifs/GeniiFSSession.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		class GeniiFSMount;

		class GeniiFSExecuteFunctions : public ogrsh::ExecuteFunctions
		{
			private:
				GeniiFSSession *_session;
				GeniiFSMount *_mount;
				std::string _rnsSource;

			public:
				GeniiFSExecuteFunctions(GeniiFSSession *session,
					GeniiFSMount *mount,
					const std::string &rnsSource);

				virtual int execve(ExecuteState *eState,
					const Path &relativePath,
					char *const argv[], char *const envp[]);
		};
	}
}

#endif
