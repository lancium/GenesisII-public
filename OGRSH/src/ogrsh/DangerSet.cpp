#include <unistd.h>
#include <sys/types.h>
#include <dirent.h>
#include <acl/libacl.h>
#include <errno.h>

#include "ogrsh/Logging.hpp"

extern "C" {

	int __open64(const char *name, int openMode, int filePerm)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method __open64(\""
			<< name << "\", " << openMode << ", " << filePerm << ")");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	int __open(const char *name, int openMode, int filePerm)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method __open(\""
			<< name << "\", " << openMode << ", " << filePerm << ")");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	int _open64(const char *name, int openMode, int filePerm)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method _open64(\""
			<< name << "\", " << openMode << ", " << filePerm << ")");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	int _open(const char *name, int openMode, int filePerm)
	{
		OGRSH_FATAL("Attempt to use un-intercepted method _open(\""
			<< name << "\", " << openMode << ", " << filePerm << ")");
		ogrsh::shims::real_exit(1);

		return -1;
	}

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

	int fcloseall()
	{
		OGRSH_FATAL("Attempt to use un-intercepted method fcloseall().");
		ogrsh::shims::real_exit(1);

		return -1;
	}

	void _flushlbf(void)
	{
		OGRSH_FATAL("Attempting to use un-intercepted method __flushlbf(...).");
		ogrsh::shims::real_exit(1);
	}

	int __libc_write(int, const void*, size_t)
	{
		OGRSH_FATAL(
			"Attempting to use un-intercepted method __libc_write(...).");
		ogrsh::shims::real_exit(1);

		return -1;
	}
}
