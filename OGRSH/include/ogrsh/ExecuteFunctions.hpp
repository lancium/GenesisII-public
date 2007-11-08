#ifndef __EXECUTE_FUNCTIONS_HPP__
#define __EXECUTE_FUNCTIONS_HPP__

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <string>

#include "ogrsh/Path.hpp"
#include "ogrsh/ExecuteState.hpp"

namespace ogrsh
{
	class ExecuteFunctions
	{
		private:
			ExecuteFunctions(const ExecuteFunctions&);
			ExecuteFunctions& operator= (const ExecuteFunctions&);

		protected:
			ExecuteFunctions();

		public:
			virtual ~ExecuteFunctions();

			virtual int execve(ExecuteState *eState, const Path &relativePath,
				char *const argv[], char *const envp[]) = 0;
	};
}

#endif
