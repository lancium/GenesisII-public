#ifndef __OGRSH_SHIMS_FILE_HPP__
#define __OGRSH_SHIMS_FILE_HPP__

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
		SHIM_DECL(int, creat, (const char *path, mode_t mode));
		SHIM_DECL(int, creat64, (const char *path, mode_t mode));
		SHIM_DECL(int, open, (const char *path, int flags, mode_t mode));
		SHIM_DECL(int, open64, (const char *path, int flags, mode_t mode));
		SHIM_DECL(int, openat,
			(int fd, const char *pathname, int flags, mode_t mode));
		SHIM_DECL(int, openat64,
			(int fd, const char *pathname, int flags, mode_t mode));
		SHIM_DECL(int, close, (int fd));
		SHIM_DECL(int, unlink, (const char *path));
		SHIM_DECL(int, unlinkat, (int dirfd, const char *path, int flags));

		SHIM_DECL(int, _llseek, (unsigned int fd, unsigned long offsethigh,
			unsigned long offsetlow, loff_t *result, unsigned int whence));
		SHIM_DECL(off_t, lseek, (int fd, off_t offset, int whence));
		SHIM_DECL(off64_t, lseek64, (int fd, off64_t offset, int whence));

		SHIM_DECL(ssize_t, read, (int fd, void *buf, size_t count));
		SHIM_DECL(ssize_t, write, (int fd, const void *buf, size_t count));
		SHIM_DECL(int, fsync, (int fd));
		SHIM_DECL(int, ftruncate, (int fd, off_t length));
		SHIM_DECL(int, ftruncate64, (int fd, off64_t length));

		SHIM_DECL(FILE*, fopen, (const char *path, const char *modes));
		SHIM_DECL(FILE*, fopen64, (const char *path, const char *modes));
		SHIM_DECL(FILE*, fdopen, (int fd, const char *modes));

		SHIM_DECL(int, fcntl, (int fd, int cmd, long arg));
		SHIM_DECL(int, fchmod, (int fd, mode_t mode));

		void startFileShims();
		void stopFileShims();
	}
}

#endif
