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

		SHIM_DECL(FILE*, fopen, (const char *path, const char *modes));
		SHIM_DECL(FILE*, fopen64, (const char *path, const char *modes));
		SHIM_DECL(FILE*, fdopen, (int fd, const char *modes));
		SHIM_DECL(int, fclose, (FILE *stream));
		SHIM_DECL(char*, fgets, (char *s, int n, FILE *stream));
		SHIM_DECL(int, fputs, (const char *s, FILE *stream));
		SHIM_DECL(char*, fgets_unlocked, (char *s, int n, FILE *stream));

		extern "C" {
			int fprintf(FILE*, const char *, ...);
			int __fprintf_chk(FILE*, int, const char *, ...);
		}		

		SHIM_DECL(int, vfprintf, (FILE*, const char *format, va_list ap));
		SHIM_DECL(int, fflush, (FILE*));
		SHIM_DECL(int, fflush_unlocked, (FILE*));
		SHIM_DECL(int, fseek, (FILE*, long offset, int whence));
		SHIM_DECL(int, fseeko, (FILE*, off_t offset, int whence));
		SHIM_DECL(long, ftell, (FILE*));
		SHIM_DECL(off_t, ftello, (FILE*));
		SHIM_DECL(size_t, fread,
			(void *ptr, size_t size, size_t nmemb, FILE *stream));
		SHIM_DECL(size_t, fwrite,
			(const void *ptr, size_t size, size_t nmemb, FILE *stream));
		SHIM_DECL(int, fputc, (int c, FILE *stream));
		SHIM_DECL(int, putc, (int c, FILE *stream));
		SHIM_DECL(int, getc, (FILE *stream));
		SHIM_DECL(int, fgetc, (FILE *stream));
		SHIM_DECL(int, feof, (FILE *stream));
		SHIM_DECL(int, ferror, (FILE *stream));
		SHIM_DECL(int, fileno, (FILE *stream));
		SHIM_DECL(int, fcntl, (int fd, int cmd, long arg));
		SHIM_DECL(int, setvbuf,
			(FILE *stream, char *buf, int mode, size_t size));
		SHIM_DECL(void, clearerr, (FILE *stream));
		SHIM_DECL(int, _IO_getc, (FILE *stream));
		SHIM_DECL(int, _IO_putc, (int c, FILE *stream));
		SHIM_DECL(int, _IO_feof, (FILE *stream));
		SHIM_DECL(int, _IO_ferror, (FILE *stream));

		int uber_real_fprintf(FILE*, const char *format, ...);

		void startFileShims();
		void stopFileShims();
	}
}

#endif
