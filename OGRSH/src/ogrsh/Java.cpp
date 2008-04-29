#include <errno.h>
#include <dlfcn.h>
#include <sys/types.h>
#include <dirent.h>
#include <stdio.h>
#include <stdio_ext.h>

#include "ogrsh/Configuration.hpp"
#include "ogrsh/FileDescriptorTable.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/ProtectedTable.hpp"

#include "ogrsh/shims/Java.hpp"
#include "ogrsh/shims/File.hpp"

using namespace ogrsh;

namespace ogrsh
{
	namespace shims
	{
		SHIM_DEF(int, JVM_Open, (const char *filename, int flags, int mode),
			(filename, flags, mode))
		{
			OGRSH_TRACE("JVM_Open (" << filename << ", "
				<< flags << ", " << mode << ") called.");
			return open64(filename, flags, mode);
		}

		SHIM_DEF(int, JVM_Close, (int fd), (fd))
		{
			OGRSH_TRACE("JVM_Close (" << fd << ") called.");
			return close(fd);
		}

		SHIM_DEF(int, JVM_Read, (int fd, char *buf, int bytes),
			(fd, buf, bytes))
		{
			OGRSH_TRACE("JVM_Read (" << fd << ", ..., " << bytes
				<< ") called.");

			return read(fd, buf, bytes);
		}

		SHIM_DEF(int, JVM_Write, (int fd, char *buf, int bytes),
			(fd, buf, bytes))
		{
			OGRSH_TRACE("JVM_Write (" << fd << ", ..., " << bytes
				<< ") called.");
			
			return write(fd, buf, bytes);
		}

		SHIM_DEF(int, JVM_Available, (int fd, off64_t *available),
			(fd, available))
		{
			OGRSH_FATAL("JVM_Available (" << fd << ", ...) called.");
			ogrsh::shims::real_exit(1);
		}

		SHIM_DEF(off64_t, JVM_Lseek, (int fd, off64_t length, int whence),
			(fd, length, whence))
		{
			OGRSH_TRACE("JVM_Available (" << fd << ", " << length
				<< ") called.");

			return lseek64(fd, length, whence);
		}

		SHIM_DEF(int, JVM_SetLength, (int fd, off64_t length), (fd, length))
		{
			OGRSH_TRACE("JVM_SetLength (" << fd << ", " << length
				<< ") called.");

			return ftruncate64(fd, length);
		}

		SHIM_DEF(int, JVM_Sync, (int fd), (fd))
		{
			OGRSH_TRACE("JVM_Sync (" << fd << ") called.");

			return fsync(fd);
		}

		void startJavaShims()
		{
			START_SHIM(JVM_Open);
			START_SHIM(JVM_Close);
			START_SHIM(JVM_Read);
			START_SHIM(JVM_Write);
			START_SHIM(JVM_Available);
			START_SHIM(JVM_Lseek);
			START_SHIM(JVM_SetLength);
			START_SHIM(JVM_Sync);
		}

		void stopJavaShims()
		{
			STOP_SHIM(JVM_Open);
			STOP_SHIM(JVM_Close);
			STOP_SHIM(JVM_Read);
			STOP_SHIM(JVM_Write);
			STOP_SHIM(JVM_Available);
			STOP_SHIM(JVM_Lseek);
			STOP_SHIM(JVM_SetLength);
			STOP_SHIM(JVM_Sync);
		}
	}
}
