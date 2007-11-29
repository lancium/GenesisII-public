#ifndef __OGRSH_SHIMS_DIRECTORY_HPP__
#define __OGRSH_SHIMS_DIRECTORY_HPP__

#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#include <unistd.h>

#include "ogrsh/ShimMacros.hpp"

namespace ogrsh
{
	namespace shims
	{
		SHIM_DECL(int, utime, (const char *filename,
			const struct utimbuf *buf));
		SHIM_DECL(int, utimes, (const char *filename,
			const struct timeval *times));
		SHIM_DECL(int, chdir, (const char *path));
		SHIM_DECL(int, fchdir, (int fd));
		SHIM_DECL(int, mkdir, (const char *pathname, mode_t mode));
		SHIM_DECL(int, rmdir, (const char *pathname));
		SHIM_DECL(char*, getcwd, (char *buf, size_t size));
		SHIM_DECL(char*, get_current_dir_name, ());
		SHIM_DECL(int, rename, (const char *origP, const char *newP));
		SHIM_DECL(int, chmod, (const char *path, mode_t mode));

		SHIM_DECL(DIR*, opendir, (const char *path));
		SHIM_DECL(DIR*, fdopendir, (int fd));
		SHIM_DECL(int, closedir, (DIR *dir));
		SHIM_DECL(dirent*, readdir, (DIR *dir));
		SHIM_DECL(dirent64*, readdir64, (DIR *dir));
		SHIM_DECL(int, dirfd, (DIR *dir));

		SHIM_DECL(int, link, (const char *oldPath, const char *newPath));

		SHIM_DECL(int, __xstat,
			(int version, const char *path, struct stat *statbuf));
		SHIM_DECL(int, __xstat64,
			(int version, const char *path, struct stat64 *statbuf));
		SHIM_DECL(int, __lxstat,
			(int version, const char *path, struct stat *statbuf));
		SHIM_DECL(int, __lxstat64,
			(int version, const char *path, struct stat64 *statbuf));
		SHIM_DECL(int, readlink,
			(const char *path, char *buf, size_t bufsize));
		SHIM_DECL(int, __fxstat, (int version, int fd, struct stat *statbuf));
		SHIM_DECL(int, __fxstat64,
			(int version, int fd, struct stat64 *statbuf));
		SHIM_DECL(int, __fxstatat,
			(int version, int dirfd, const char *path, struct stat *statbuf,
				int flags));
		SHIM_DECL(int, __fxstatat64,
			(int version, int dirfd, const char *path, struct stat64 *statbuf,
				int flags));

		void startDirectoryShims();
		void stopDirectoryShims();
	}
}

#endif
