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

#include "ogrsh/shims/File.hpp"

using namespace ogrsh;

namespace ogrsh
{
	static int translateMode(const char *mode);
	static ssize_t IOCookieReadImpl(void*, char *buffer, size_t bytes);
	static ssize_t IOCookieWriteImpl(void*, const char *buffer, size_t bytes);
	static int IOCookieSeekImpl(void*, off64_t *pos, int whence);
	static int IOCookieCloseImpl(void*);

	namespace shims
	{
		SHIM_DEF(int, creat, (const char *pathname, mode_t mode),
			(pathname, mode))
		{
			return open(pathname, O_CREAT | O_WRONLY | O_TRUNC, mode);
		}

		SHIM_DEF(int, creat64, (const char *pathname, mode_t mode),
			(pathname, mode))
		{
			return open64(pathname, O_CREAT | O_WRONLY | O_TRUNC, mode);
		}

		SHIM_DEF(int, openat,
			(int fd, const char *pathname, int flags, mode_t mode),
			(fd, pathname, flags, mode))
		{
			return openat64(fd, pathname, flags, mode);
		}

		SHIM_DEF(int, openat64,
			(int fd, const char *pathname, int flags, mode_t mode),
			(fd, pathname, flags, mode))
		{
			OGRSH_TRACE("openat64(" << fd << ", \"" << pathname
				<< "\", " << flags << ", ...) called.");

			if ((pathname[0] == '/') || (fd == AT_FDCWD))
			{
				return open64(pathname, flags, mode);
			} else
			{
				FileDescriptor *desc =
					FileDescriptorTable::getInstance().lookup(fd);
				if (desc == NULL)
				{
					// It's not a file descrpitor of ours, so we pass
					// it through
					return ogrsh::shims::real_openat64(fd,
						pathname, flags, mode);
				} else
				{
					// It's one of ours, so we'll have to deal with it
					std::string fullVirtualPath =
						desc->getFullVirtualPath();
					if (fullVirtualPath.length() == 0)
					{
						OGRSH_FATAL("openat64 received a file descriptor "
							<< "with no path set.  Can't open...");
						ogrsh::shims::real_exit(1);
					}

					Path p = Path::getCurrentWorkingDirectory();
					p = p.lookup(fullVirtualPath + "/" + pathname);
					std::string newPath = (const std::string&)p;
					OGRSH_DEBUG("Converted openat64 path to \""
						<< newPath.c_str() << "\".");
					return open64(newPath.c_str(), flags, mode);
				}
			}

			return -1;
		}

		SHIM_DEF(int, open, (const char *path, int flags, mode_t mode),
			(path, flags, mode))
		{
			return open64(path, flags, mode);
		}

		SHIM_DEF(int, open64, (const char *path, int flags, mode_t mode),
			(path, flags, mode))
		{
			// We have to worry about execve weirdness here (see execve)
			ExecuteState state = ExecuteState::fromEnvironment();
			const char *virt = state.getVirtualPath();
			const char *real = state.getRealPath();
			if ( (virt != NULL) && (real != NULL) && (strcmp(real, path) == 0))
			{
				OGRSH_TRACE("Open64 saw an execute state of virtual["
					<< virt << "] and real[" << real << "].");
				path = virt;
			}

			OGRSH_TRACE("open64(\"" << path << "\") called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			FileDescriptor* desc = rootMount->getFileFunctions()->open64(
				fullPath, flags, mode);
			if (desc == NULL)
				return -1;

			int ret = FileDescriptorTable::getInstance().insert(desc);
			if (ret < 0)
				delete desc;

			return ret;
		}

		SHIM_DEF(int, close, (int fd), (fd))
		{
			OGRSH_DEBUG("close(" << fd << ") called.");
			if (fd < 0)
				return 0;

			if (ProtectedTable::getInstance().isProtected(fd))
			{
				OGRSH_DEBUG("Attempt made to close a protected file "
					<< "descriptor...ignoring.");
				return 0;
			}

			int ret = FileDescriptorTable::getInstance().close(fd);
			if (ret < 0 && errno == EBADF)
				return ogrsh::shims::real_close(fd);

			return ret;
		}

		SHIM_DEF(int, unlink, (const char *path), (path))
		{
			OGRSH_TRACE("unlink(\"" << path << "\") called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getFileFunctions()->unlink(fullPath);
		}

		SHIM_DEF(int, unlinkat, (int dirfd, const char *path, int flags),
			(dirfd, path, flags))
		{
			OGRSH_TRACE("unlinkat(" << dirfd << ", \"" << path << "\", "
				<< flags << ") called.");

			if ((path[0] == '/') || (dirfd == AT_FDCWD))
			{
				Path fullPath =
					Path::getCurrentWorkingDirectory().lookup(path);
				Mount *rootMount =
					Configuration::getConfiguration().getRootMount();

				if (flags & AT_REMOVEDIR)
					return rootMount->getDirectoryFunctions()->rmdir(fullPath);
				else
					return rootMount->getFileFunctions()->unlink(fullPath);
			} else
			{
				FileDescriptor *desc =
					FileDescriptorTable::getInstance().lookup(dirfd);
				if (desc == NULL)
				{
					// It's not a file descrpitor of ours, so we pass
					// it through
					return ogrsh::shims::real_unlinkat(dirfd, path, flags);
				} else
				{
					// It's one of ours, so we'll have to deal with it
					std::string fullVirtualPath =
						desc->getFullVirtualPath();
					if (fullVirtualPath.length() == 0)
					{
						OGRSH_FATAL("unlinkat received a file descriptor "
							<< "with no path set.  Can't unlink...");
						ogrsh::shims::real_exit(1);
					}

					Path p = Path::getCurrentWorkingDirectory();
					p = p.lookup(fullVirtualPath + "/" + path);
					std::string newPath = (const std::string&)p;
					OGRSH_DEBUG("Converted unlinkat path to \""
						<< newPath.c_str() << "\".");
					return unlinkat(AT_FDCWD, newPath.c_str(), flags);
				}
			}

		}

		SHIM_DEF(ssize_t, read, (int fd, void *buf, size_t count),
			(fd, buf, count))
		{
			OGRSH_TRACE("read(" << fd << ", ..., " << count << ") called.");

			FileDescriptor* desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through.
				return ogrsh::shims::real_read(fd, buf, count);
			}

			return desc->read(buf, count);
		}

		SHIM_DEF(ssize_t, write, (int fd, const void *buf, size_t count),
			(fd, buf, count))
		{
			OGRSH_TRACE("write(" << fd << ", ..., " << count << ") called.");

			FileDescriptor* desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through.
				return ogrsh::shims::real_write(fd, buf, count);
			}

			return desc->write(buf, count);
		}

		SHIM_DEF(off_t, lseek, (int fd, off_t offset, int whence),
			(fd, offset, whence))
		{
			OGRSH_TRACE("lseek(" << fd << ", " << offset << ", "
				<< whence << ") called.");

			FileDescriptor *desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through
				return ogrsh::shims::real_lseek(fd, offset, whence);
			}

			return desc->lseek64(offset, whence);
		}

		SHIM_DEF(off64_t, lseek64, (int fd, off64_t offset, int whence),
			(fd, offset, whence))
		{
			OGRSH_TRACE("lseek64(" << fd << ", " << offset << ", "
				<< whence << ") called.");

			FileDescriptor *desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through
				return ogrsh::shims::real_lseek64(fd, offset, whence);
			}

			return desc->lseek64(offset, whence);
		}

		SHIM_DEF(int, _llseek, (unsigned int fd, unsigned long offsethigh,
			unsigned long offsetlow, loff_t *result, unsigned int whence),
			(fd, offsethigh, offsetlow, result, whence))
		{
			off64_t offset;
			off64_t res;

			OGRSH_TRACE("_llseek(" << fd << ", " << offsethigh << ", "
				<< offsetlow << ", ..., " << whence << ") called.");

			offset = offsethigh;
			offset <<= 32;
			offset |= offsetlow;
			res = lseek64((int)fd, offset, (int)whence);
			if (res >= 0)
			{
				*result = res;
				return 0;
			}

			return -1;
		}

		SHIM_DEF(int, fcntl, (int fd, int cmd, long arg), (fd, cmd, arg))
		{
			OGRSH_TRACE("fcntl(" << fd << ", " << cmd << ", ...) called.");

			FileDescriptor *desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through
				return ogrsh::shims::real_fcntl(fd, cmd, arg);
			}

			return desc->fcntl(cmd, arg);
		}

		SHIM_DEF(int, fsync, (int fd), (fd))
		{
			OGRSH_TRACE("fsync(" << fd << ") called.");

			FileDescriptor *desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through
				return ogrsh::shims::real_fsync(fd);
			}

			return desc->fsync();
		}

		SHIM_DEF(int, fchmod, (int fd, mode_t mode), (fd, mode))
		{
			OGRSH_TRACE("fchmod(" << fd << ", " << mode << ") called.");

			FileDescriptor *desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through
				return ogrsh::shims::real_fchmod(fd, mode);
			}

			return desc->fchmod(mode);
		}

		SHIM_DEF(FILE*, fopen, (const char *path, const char *mode),
			(path, mode))
		{
			OGRSH_TRACE("fopen(\"" << path << "\", \"" << mode
				<< "\") called.");

			return fopen64(path, mode);
		}

		SHIM_DEF(FILE*, fopen64, (const char *path, const char *mode),
			(path, mode))
		{
			OGRSH_TRACE("fopen64(\"" << path << "\", \"" << mode
				<< "\") called.");

			int fd;
			FILE *ret;

			fd = open64(path, translateMode(mode), 0666);
			if (fd < 0)
				return NULL;

			ret = fdopen(fd, mode);
			if (ret == NULL)
				close(fd);

			return ret;
		}

		SHIM_DEF(FILE*, fdopen, (int fd, const char *mode),
			(fd, mode))
		{
			OGRSH_TRACE("fdopen(" << fd << ", \"" << mode
				<< "\") called.");

			int *fdPtr;
			FILE *ret;
			_IO_cookie_io_functions_t ioFunctions;

			fdPtr = (int*)malloc(sizeof(int));
			*fdPtr = fd;

			ioFunctions.read = IOCookieReadImpl;
			ioFunctions.write = IOCookieWriteImpl;
			ioFunctions.seek = IOCookieSeekImpl;
			ioFunctions.close = IOCookieCloseImpl;

			ret = fopencookie(fdPtr, mode, ioFunctions);
			if (ret == NULL)
			{
				free(fdPtr);
				return NULL;
			}

			ret->_fileno = fd;
			return ret;
		}

		void startFileShims()
		{
			START_SHIM(openat);
			START_SHIM(openat64);
			START_SHIM(open);
			START_SHIM(open64);
			START_SHIM(creat);
			START_SHIM(creat64);
			START_SHIM(close);
			START_SHIM(unlink);
			START_SHIM(unlinkat);
			START_SHIM(read);
			START_SHIM(write);
			START_SHIM(_llseek);
			START_SHIM(lseek64);
			START_SHIM(lseek);

			START_SHIM(fopen);
			START_SHIM(fopen64);
			START_SHIM(fdopen);
			START_SHIM(fcntl);
			START_SHIM(fsync);
			START_SHIM(fchmod);
		}

		void stopFileShims()
		{
			STOP_SHIM(fchmod);
			STOP_SHIM(fsync);
			STOP_SHIM(fcntl);
			STOP_SHIM(fdopen);
			STOP_SHIM(fopen64);
			STOP_SHIM(fopen);

			STOP_SHIM(lseek);
			STOP_SHIM(lseek64);
			STOP_SHIM(_llseek);
			STOP_SHIM(write);
			STOP_SHIM(read);
			STOP_SHIM(creat64);
			STOP_SHIM(creat);
			STOP_SHIM(open64);
			STOP_SHIM(open);
			STOP_SHIM(openat64);
			STOP_SHIM(openat);
			STOP_SHIM(unlinkat);
			STOP_SHIM(unlink);
			STOP_SHIM(close);
		}
	}

	int translateMode(const char *mode)
	{
		int lcv;
		char rwa;
		int hasPlus = 0;
		int ret = 0x0;

		for (lcv = 0; mode[lcv] != (char)0; lcv++)
		{
			switch (mode[lcv])
			{
				case 'r' :
				case 'w' :
				case 'a' :
					rwa = mode[lcv];
					break;
				case '+' :
					hasPlus = 1;
					break;
			}
		}

		if (rwa == 'r')
			ret = (hasPlus ? O_RDWR : O_RDONLY);
		else
		{
			ret = O_CREAT;
			if (hasPlus)
				ret |= O_RDWR;
			else
				ret |= O_WRONLY;

			ret |= ( (rwa == 'w') ? O_TRUNC : O_APPEND);
		}

		return ret;
	}

	ssize_t IOCookieReadImpl(void *cookie, char *buffer, size_t bytes)
	{
		int fd = *((int*)cookie);
		return read(fd, buffer, bytes);
	}

	ssize_t IOCookieWriteImpl(void *cookie, const char *buffer, size_t bytes)
	{
		int fd = *((int*)cookie);
		return write(fd, buffer, bytes);
	}

	int IOCookieSeekImpl(void *cookie, off64_t *pos, int whence)
	{
		off64_t ret;
		int fd = *((int*)cookie);

		ret = lseek64(fd, *pos, whence);
		if (ret < 0)
			return -1;

		*pos = ret;
		return 0;
	}

	int IOCookieCloseImpl(void *cookie)
	{
		int fd = *((int*)cookie);
		free(cookie);
		return close(fd);
	}
}
