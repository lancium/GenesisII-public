#ifndef __OGRSH_SHIMS_JAVA_HPP__
#define __OGRSH_SHIMS_JAVA_HPP__

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
		SHIM_DECL(int, JVM_Open, (const char *filename, int flags, int mode));
		SHIM_DECL(int, JVM_Close, (int fd));
		SHIM_DECL(int, JVM_Read, (int fd, char *buf, int bytes));
		SHIM_DECL(int, JVM_Write, (int fd, char *buf, int bytes));
		SHIM_DECL(int, JVM_Available, (int fd, off64_t *available));
		SHIM_DECL(off64_t, JVM_Lseek, (int fd, off64_t length, int whence));
		SHIM_DECL(int, JVM_SetLength, (int fd, off64_t length));
		SHIM_DECL(int, JVM_Sync, (int fd));

		void startJavaShims();
		void stopJavaShims();
	}
}

#endif
