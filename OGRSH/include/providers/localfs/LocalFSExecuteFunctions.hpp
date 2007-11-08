#ifndef __LOCALFS_EXECUTE_FUNCTIONS_HPP__
#define __LOCALFS_EXECUTE_FUNCTIONS_HPP__

#include <string>

#include "ogrsh/ExecuteFunctions.hpp"

namespace ogrsh
{
	namespace localfs
	{
		class LocalFSExecuteFunctions : public ogrsh::ExecuteFunctions
		{
			private:
				std::string _localSource;

			public:
				LocalFSExecuteFunctions(const std::string &localSource);

				virtual int execve(ExecuteState *eState,
					const Path &relativePath,
					char *const argv[], char *const envp[]);
		};
	}
}

#endif
