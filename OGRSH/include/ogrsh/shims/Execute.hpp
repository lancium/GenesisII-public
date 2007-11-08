#ifndef __OGRSH_SHIMS_EXECUTE_HPP__
#define __OGRSH_SHIMS_EXECUTE_HPP__

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdio.h>

#include <stdarg.h>

#include "ogrsh/ShimMacros.hpp"

namespace ogrsh
{
	namespace shims
	{
		SHIM_DECL(int, execve, (const char *filename,
			char *const argv[], char *const envp[]));

		void startExecuteShims();
		void stopExecuteShims();
	}
}

#endif
