#ifndef __VIRTUAL_EXECUTE_FUNCTIONS_HPP__
#define __VIRTUAL_EXECUTE_FUNCTIONS_HPP__

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <string>

#include "ogrsh/Path.hpp"
#include "ogrsh/ExecuteFunctions.hpp"

namespace ogrsh
{
	class VirtualExecuteFunctions : public ExecuteFunctions
	{
		private:
			MountTree *_mountTree;

		public:
			VirtualExecuteFunctions(MountTree *mountTree);

			virtual int execve(ExecuteState *eState, const Path &relativePath,
				char *const argv[], char *const envp[]);
	};
}

#endif
