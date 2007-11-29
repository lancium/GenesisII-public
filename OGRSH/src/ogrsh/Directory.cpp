#include <errno.h>
#include <dlfcn.h>
#include <sys/types.h>
#include <dirent.h>

#include "ogrsh/Configuration.hpp"
#include "ogrsh/FileDescriptorTable.hpp"
#include "ogrsh/Logging.hpp"

#include "ogrsh/shims/Directory.hpp"

using namespace ogrsh;

namespace ogrsh
{
	namespace shims
	{
		SHIM_DEF(int, utime, (const char *filename, const struct utimbuf *buf),
			(filename, buf))
		{
			OGRSH_TRACE("utime(\"" << filename << "\", ...) called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(filename);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getDirectoryFunctions()->utime(fullPath, buf);
		}

		SHIM_DEF(int, utimes,
			(const char *filename, const struct timeval *times),
			(filename, times))
		{
			OGRSH_TRACE("utimes(\"" << filename << "\", ...) called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(filename);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getDirectoryFunctions()->utimes(fullPath, times);
		}

		SHIM_DEF(int, chmod, (const char *path, mode_t mode), (path, mode))
		{
			OGRSH_TRACE("chmod(\"" << path << "\", " << mode << ") called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getDirectoryFunctions()->chmod(fullPath, mode);
		}

		SHIM_DEF(int, rename, (const char *oldP, const char *newP),
			(oldP, newP))
		{
			OGRSH_TRACE("rename(\"" << oldP << "\", \"" << newP
				<< "\") called.");

			Path fullOldPath = Path::getCurrentWorkingDirectory().lookup(oldP);
			Path fullNewPath = Path::getCurrentWorkingDirectory().lookup(newP);

			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getDirectoryFunctions()->rename(fullOldPath,
				fullNewPath);
		}

		SHIM_DEF(int, chdir, (const char *path), (path))
		{
			OGRSH_TRACE("chdir(\"" << path << "\") called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			int ret = rootMount->getDirectoryFunctions()->chdir(fullPath);
			if (ret == 0)
				Path::setCurrentWorkingDirectory(fullPath);

			return ret;
		}

		SHIM_DEF(int, fchdir, (int fd), (fd))
		{
			OGRSH_TRACE("fchdir(" << fd << ") called.");

			FileDescriptor *desc =
				FileDescriptorTable::getInstance().lookup(fd);
			if (desc == NULL)
			{
				// It's not a file descrpitor of ours
				OGRSH_FATAL("fchdir received a file descriptor which "
					<< "OGRSH didn't open.");
				ogrsh::shims::real_exit(1);
			} else
			{
				// It's one of ours, so we'll have to deal with it
				std::string fullVirtualPath =
					desc->getFullVirtualPath();
				if (fullVirtualPath.length() == 0)
				{
					OGRSH_FATAL("fchdir received a file descriptor "
						<< "with no path set.  Can't stat...");
					ogrsh::shims::real_exit(1);
				}

				return chdir(fullVirtualPath.c_str());
			}

			// Can't get here.
			return -1;
		}

		SHIM_DEF(int, mkdir, (const char *pathname, mode_t mode),
			(pathname, mode))
		{
			OGRSH_TRACE("mkdir(\"" << pathname << "\", " << mode
				<< ") called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(pathname);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			int ret = rootMount->getDirectoryFunctions()->mkdir(fullPath, mode);
			return ret;
		}

		SHIM_DEF(int, rmdir, (const char *pathname), (pathname))
		{
			OGRSH_TRACE("rmdir(\"" << pathname << "\") called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(pathname);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			int ret = rootMount->getDirectoryFunctions()->rmdir(fullPath);
			return ret;
		}

		SHIM_DEF(char*, get_current_dir_name, (), ())
		{
			OGRSH_TRACE("get_current_dir_name() called.");

			char *ret = (char*)malloc(1024);
			getcwd(ret, 1024);
			return ret;
		}

		SHIM_DEF(char*, getcwd, (char *buf, size_t size), (buf, size))
		{
			OGRSH_TRACE("getcwd(..., " << size << ") called.");

			std::string path =
				(const std::string&)Path::getCurrentWorkingDirectory();
			size_t length = path.length();

			if (buf == NULL)
			{
				// No buffer, we'll create one
				if (size == 0)
				{
					size = length + 1;
					buf = (char*)malloc(sizeof(char) * size);
				} else
				{
					if (size <= length)
					{
						errno = ERANGE;
						return NULL;
					} else
					{
						buf = (char*)malloc(sizeof(char) * size);
					}
				}
			}

			// We are guaranteed to have a buffer now
			if (size <= length)
			{
				errno = ERANGE;
				return NULL;
			}

			strcpy(buf, path.c_str());
			return buf;
		}

		SHIM_DEF(int, link, (const char *oldPath, const char *newPath),
			(oldPath, newPath))
		{
			OGRSH_TRACE("link(\"" << oldPath << "\", \""
				<< newPath << "\") called.");

			Path fullOldPath = Path::getCurrentWorkingDirectory().lookup(
				oldPath);
			Path fullNewPath = Path::getCurrentWorkingDirectory().lookup(
				newPath);

			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getDirectoryFunctions()->link(
				fullOldPath, fullNewPath);
		}

		SHIM_DEF(DIR*, opendir, (const char *name), (name))
		{
			OGRSH_TRACE("opendir(\"" << name << "\") called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(name);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return (DIR*)(rootMount->getDirectoryFunctions()->opendir(
				fullPath));
		}

		SHIM_DEF(DIR*, fdopendir, (int fd), (fd))
		{
			OGRSH_TRACE("fdopendir(" << fd << ") called.");
		
			if (fd < 0)
				return NULL;

			FileDescriptor *desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through
				return ogrsh::shims::real_fdopendir(fd);
			}

			return (DIR*)(desc->opendir());
		}

		SHIM_DEF(int, closedir, (DIR *dir), (dir))
		{
			OGRSH_TRACE("closedir() called.");

			DirectoryStream *realDir = (DirectoryStream*)dir;
			delete realDir;
			return 0;
		}

		SHIM_DEF(dirent*, readdir, (DIR *dir), (dir))
		{
			OGRSH_TRACE("readdir() called.");

			DirectoryStream *realDir = (DirectoryStream*)dir;
			return realDir->readdir();
		}

		SHIM_DEF(dirent64*, readdir64, (DIR *dir), (dir))
		{
			OGRSH_TRACE("readdir64() called.");

			DirectoryStream *realDir = (DirectoryStream*)dir;
			return realDir->readdir64();
		}

		SHIM_DEF(int, dirfd, (DIR *dir), (dir))
		{
			OGRSH_TRACE("dirfd(...) called.");

			DirectoryStream *realDir = (DirectoryStream*)dir;
			return realDir->dirfd();
		}

		SHIM_DEF(int, __xstat,
			(int version, const char *path, struct stat *statbuf),
			(version, path, statbuf))
		{
			OGRSH_TRACE("__xstat(" << path << ", ...) called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getDirectoryFunctions()->__xstat(
				version, fullPath, statbuf);
		}

		SHIM_DEF(int, __xstat64,
			(int version, const char *path, struct stat64 *statbuf),
			(version, path, statbuf))
		{
			OGRSH_TRACE("__xstat64(" << path << ", ...) called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getDirectoryFunctions()->__xstat64(
				version, fullPath, statbuf);
		}

		SHIM_DEF(int, __fxstat,
			(int version, int fd, struct stat *statbuf),
			(version, fd, statbuf))
		{
			OGRSH_TRACE("__fxstat(" << version << ", " << fd <<
				", ...) called.");

			FileDescriptor* desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through.
				return ogrsh::shims::real___fxstat(version, fd, statbuf);
			}

			return desc->__fxstat(version, statbuf);
		}

		SHIM_DEF(int, __fxstat64,
			(int version, int fd, struct stat64 *statbuf),
			(version, fd, statbuf))
		{
			OGRSH_TRACE("__fxstat64(" << version << ", " << fd <<
				", ...) called.");

			FileDescriptor* desc = FileDescriptorTable::getInstance().lookup(
				fd);
			if (desc == NULL)
			{
				// It's not one we have control of -- pass it through.
				return ogrsh::shims::real___fxstat64(version, fd, statbuf);
			}

			return desc->__fxstat64(version, statbuf);
		}

		SHIM_DEF(int, __fxstatat,
			(int version, int dirfd,
				const char *path, struct stat *statbuf, int flags),
			(version, dirfd, path, statbuf, flags))
		{
			OGRSH_TRACE("__fxstatat(" << path << ", ...) called.");

			if (dirfd != AT_FDCWD)
			{
				OGRSH_FATAL("__fxstatat with dirfd NOT EQUAL to AT_FDCWD "
					<< "is not implemented.");
				ogrsh::shims::real_exit(1);
			}

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();

			if (flags & AT_SYMLINK_NOFOLLOW)
				return rootMount->getDirectoryFunctions()->__lxstat(
					version, fullPath, statbuf);
			else
				return rootMount->getDirectoryFunctions()->__xstat(
					version, fullPath, statbuf);
		}

		SHIM_DEF(int, __fxstatat64,
			(int version, int dirfd,
				const char *path, struct stat64 *statbuf, int flags),
			(version, dirfd, path, statbuf, flags))
		{
			OGRSH_DEBUG("__fxstatat64(" << version << ", " << dirfd
				<< ", \"" << path << "\", ...) called.");

			if ( (path[0] == '/') || (dirfd == AT_FDCWD))
			{
				Path fullPath =
					Path::getCurrentWorkingDirectory().lookup(path);
				Mount *rootMount =
					Configuration::getConfiguration().getRootMount();

				if (flags & AT_SYMLINK_NOFOLLOW)
					return rootMount->getDirectoryFunctions()->__lxstat64(
						version, fullPath, statbuf);
				else
					return rootMount->getDirectoryFunctions()->__xstat64(
						version, fullPath, statbuf);
			} else
			{
				FileDescriptor *desc =
					FileDescriptorTable::getInstance().lookup(dirfd);
				if (desc == NULL)
				{
					// It's not a file descrpitor of ours, so we pass
					// it through
					return ogrsh::shims::real___fxstatat64(version, dirfd,
						path, statbuf, flags);
				} else
				{
					// It's one of ours, so we'll have to deal with it
					std::string fullVirtualPath =
						desc->getFullVirtualPath();
					if (fullVirtualPath.length() == 0)
					{
						OGRSH_FATAL("__fxstatat64 received a file descriptor "
							<< "with no path set.  Can't stat...");
						ogrsh::shims::real_exit(1);
					}

					OGRSH_DEBUG("Combining \"" <<
						fullVirtualPath << "\" with \"/\" and \""
						<< path << "\".");
						
					Path p = Path::getCurrentWorkingDirectory();
					p = p.lookup(fullVirtualPath + "/" + path);
					std::string newPath = (const std::string&)p;
					OGRSH_DEBUG("Converted __fxstatat64 path to \""
						<< newPath.c_str() << "\".");
					return __fxstatat64(version,
						AT_FDCWD, newPath.c_str(), statbuf, flags);
				}
			}
		}

		SHIM_DEF(int, __lxstat,
			(int version, const char *path, struct stat *statbuf),
			(version, path, statbuf))
		{
			OGRSH_TRACE("__lxstat(" << path << ", ...) called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getDirectoryFunctions()->__lxstat(
				version, fullPath, statbuf);
		}

		SHIM_DEF(int, __lxstat64,
			(int version, const char *path, struct stat64 *statbuf),
			(version, path, statbuf))
		{
			OGRSH_TRACE("__lxstat64(" << path << ", ...) called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getDirectoryFunctions()->__lxstat64(
				version, fullPath, statbuf);
		}

		SHIM_DEF(int, readlink,
			(const char *path, char *buf, size_t bufsize),
			(path, buf, bufsize))
		{
			OGRSH_TRACE("readlink(" << path << ", ...) called.");

			Path fullPath = Path::getCurrentWorkingDirectory().lookup(path);
			Mount *rootMount = Configuration::getConfiguration().getRootMount();
			return rootMount->getDirectoryFunctions()->readlink(
				fullPath, buf, bufsize);
		}

		void startDirectoryShims()
		{
			START_SHIM(utime);
			START_SHIM(utimes);
			START_SHIM(rename);
			START_SHIM(chdir);
			START_SHIM(fchdir);
			START_SHIM(chmod);
			START_SHIM(mkdir);
			START_SHIM(rmdir);
			START_SHIM(getcwd);
			START_SHIM(get_current_dir_name);
			START_SHIM(opendir);
			START_SHIM(fdopendir);
			START_SHIM(closedir);
			START_SHIM(readdir);
			START_SHIM(readdir64);
			START_SHIM(dirfd);
			START_SHIM(__xstat);
			START_SHIM(__xstat64);
			START_SHIM(__fxstat);
			START_SHIM(__fxstat64);
			START_SHIM(__fxstatat);
			START_SHIM(__fxstatat64);
			START_SHIM(__lxstat);
			START_SHIM(__lxstat64);
			START_SHIM(readlink);
			START_SHIM(link);
		}

		void stopDirectoryShims()
		{
			STOP_SHIM(fchdir);
			STOP_SHIM(chdir);
			STOP_SHIM(mkdir);
			STOP_SHIM(chmod);
			STOP_SHIM(rmdir);
			STOP_SHIM(get_current_dir_name);
			STOP_SHIM(getcwd);
			STOP_SHIM(dirfd);
			STOP_SHIM(fdopendir);
			STOP_SHIM(opendir);
			STOP_SHIM(closedir);
			STOP_SHIM(readdir);
			STOP_SHIM(readdir64);
			STOP_SHIM(__fxstatat);
			STOP_SHIM(__fxstatat64);
			STOP_SHIM(__fxstat);
			STOP_SHIM(__fxstat64);
			STOP_SHIM(__xstat);
			STOP_SHIM(__xstat64);
			STOP_SHIM(__lxstat);
			STOP_SHIM(__lxstat64);
			STOP_SHIM(link);
			STOP_SHIM(readlink);
			STOP_SHIM(rename);
			STOP_SHIM(utime);
			STOP_SHIM(utimes);
		}
	}
}
