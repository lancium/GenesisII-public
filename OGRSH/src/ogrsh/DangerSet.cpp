#include <unistd.h>
#include <sys/types.h>
#include <dirent.h>
#include <acl/libacl.h>
#include <errno.h>

#include "ogrsh/Logging.hpp"

extern "C" {
	// ACCESS CONTROL
	int acl_entries(acl_t)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method acl_entries(...)");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	// DIRECTORY MANAGEMENT
	char* getwd(char *buf)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method getwd(...)");
		ogrsh::shims::real_exit(1);

		return NULL;
	}

	void rewinddir(DIR*)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method rewinddir(...)");
		ogrsh::shims::real_exit(1);
	}

/*
	int scandir(const char *dir, struct dirent ***namelist,
		int (*filter)(const struct dirent *),
		int(*compar)(const struct dirent **, const struct dirent **))
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method scandir(\""
			<< dir << "\", ...).");
		ogrsh::shims::real_exit(1);

		return -1;
	}
*/

	void seekdir(DIR*, off_t)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method seekdir(...)");
		ogrsh::shims::real_exit(1);
	}

	off_t telldir(DIR*)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method telldir(...)");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	/* File Manip */
	int chown(const char *path, uid_t owner, gid_t group)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method chown(\""
			<< path << "\", ...)");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	int lchown(const char *path, uid_t owner, gid_t group)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method lchown(\""
			<< path << "\", ...)");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	int lchmod(const char *path, mode_t mode)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method lchmod(\""
			<< path << "\", ...)");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	int truncate(const char *path, off_t length)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method truncate(\""
			<< path << "\", " << length << ")");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	int ftruncate(int fd, off_t length)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method ftruncate("
			<< fd << ", " << length << ")");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	int fstatat(int dirfd, const char *pathname, struct stat *buf,
		int flags)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method fstatat("
			<< dirfd << ", \"" << pathname << "\", ..., "
			<< flags << ").");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	int fstatat64(int dirfd, const char *pathname, struct stat64 *buf,
		int flags)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method fstatat64("
			<< dirfd << ", \"" << pathname << "\", ..., "
			<< flags << ").");
		ogrsh::shims::real_exit(1);

		return 0;
	}

/*
	int eaccess(const char *name, int type)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method eaccess(\""
				<< name << "\", " << type << ").");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	int euidaccess(const char *name, int type)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method euidaccess(\""
				<< name << "\", " << type << ").");
		ogrsh::shims::real_exit(1);

		return 0;
	}
*/

	/* FILE STREAMS */
	FILE* freopen(const char *path, const char *mode, FILE *orig)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method freopen(\""
				<< path << "\", \"" << mode << "\", ...).");
		ogrsh::shims::real_exit(1);

		return NULL;
	}

/*
	FILE* fdopen(int fd, const char *mode)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method fdopen("
				<< fd << ", \"" << mode << "\").");
		ogrsh::shims::real_exit(1);

		return NULL;
	}
	int fflush_unlocked(FILE *file)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method fflush_unlocked(...).");
		ogrsh::shims::real_exit(1);

		return 1;
	}
*/

	int fscanf(FILE *stream, const char *format, ...)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method fscanf(..., \""
			<< format << "\", ...).");
		ogrsh::shims::real_exit(1);

		return 1;
	}

	int vfscanf(FILE *stream, const char *format, va_list ap)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method vfscanf(..., \""
			<< format << "\", ...).");
		ogrsh::shims::real_exit(1);

		return 1;
	}

	int fexecve(int fd, char *const argv[], char *const envp[])
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method fexecve(..., \""
				<< argv[0] << "\", ...)");
		ogrsh::shims::real_exit(1);

		return 1;
	}

/*
	int fcntl(int fd, int cmd, ...)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method fcntl("
				<< fd << ", " << cmd << ", ...)");
		ogrsh::shims::real_exit(1);

		return 1;
	}

	int fileno(FILE *stream)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method fileno(...)");
		ogrsh::shims::real_exit(1);

		return -1;
	}
*/

	int ungetc(int c, FILE *stream)
	{
		OGRSH_FATAL(
			"Attempt to use un-intercepted method ungetc('"
			<< (char)c << "', ...).");
		ogrsh::shims::real_exit(1);

		return EOF;
	}

	int fcloseall()
	{
		OGRSH_FATAL("Attempt to use un-intercepted method fcloseall().");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	void setbuf(FILE *stream, char *buf)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method setbuf(...).");
		ogrsh::shims::real_exit(1);
	}

	void setbuffer(FILE *stream, char *buf, size_t size)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method setbuffer(...).");
		ogrsh::shims::real_exit(1);
	}

	void setlinebuf(FILE *stream)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method setlinebuf(...).");
		ogrsh::shims::real_exit(1);
	}

	int fputws(const wchar_t *ws, FILE *stream)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method fputws(...).");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	wint_t fputwc(wchar_t wc, FILE *stream)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method fputwc(...).");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	wint_t putwc(wchar_t wc, FILE *stream)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method putwc(...).");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	wint_t fgetwc(FILE *stream)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method fgetwc(...).");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	wint_t getwc(FILE *stream)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method getwc(...).");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	wchar_t* fgetws(wchar_t *ws, int n, FILE *stream)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method fgetws(...).");
		ogrsh::shims::real_exit(1);

		return NULL;
	}

	size_t __fbufsize(FILE *stream)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method __fbufsize(...).");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	int __flbf(FILE *stream)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method __flbf(...).");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	int __freadable(FILE *stream)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method __freadable(...).");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	int __fwritable(FILE *stream)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method __fwriteable(...).");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	int __freading(FILE *stream)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method __freading(...).");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	int __fwriting(FILE *stream)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method __fwriting(...).");
		ogrsh::shims::real_exit(1);

		return 0;
	}

	void _flushlbf(void)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method __flushlbf(...).");
		ogrsh::shims::real_exit(1);
	}

	void __fpurge(FILE *stream)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method __fpurge(...).");
		ogrsh::shims::real_exit(1);
	}

	void flockfile(FILE *filehandle)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method flockfile(...).");
		ogrsh::shims::real_exit(1);
	}

	int ftrylockfile(FILE *filehandle)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method ftrylockfile(...).");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	void funlockfile(FILE *filehandle)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method funlockfile(...).");
		ogrsh::shims::real_exit(1);
	}

	int fpurge(FILE *stream)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method fpurge(...).");
		ogrsh::shims::real_exit(1);

		return 1;
	}
}
