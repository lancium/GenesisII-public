#include "FileSystemUtils.jh"
#include "ExceptionUtils.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <errno.h>

static const char* IO_EXCEPTION_CLASS = "java.io.IOException";

JNIEXPORT void JNICALL
	Java_edu_virginia_vcgr_genii_client_io_FileSystemUtils_chmod
		(JNIEnv *env, jclass cls, jstring filepath, jint mode)
{
	const char *msg;
	const char *sFilepath = (*env)->GetStringUTFChars(env, filepath, NULL);
	if (sFilepath == NULL)
		return;

	if (chmod(sFilepath, mode) < 0)
	{
		switch (errno)
		{
			case EPERM :
				msg = "Permission denied for chmod command.";
			break;

			case EROFS :
				msg = "The named file resides on a read-only file system.";
				break;

			case ENAMETOOLONG :
              	msg = "Path is too long.";
				break;

       		case EACCES :
				msg = "Search  permission is denied on a component "
					"of the path prefix.";
				break;

			case ELOOP :
				msg = "Too many symbolic links were encountered in "
					"resolving path.";
				break;

			default :
				msg = "An I/O error occurred.";
				break;
		}

		throwSimpleException(env, (char*)IO_EXCEPTION_CLASS, (char*)msg);
	}

	(*env)->ReleaseStringUTFChars(env, filepath, sFilepath);
}

JNIEXPORT jboolean JNICALL Java_edu_virginia_vcgr_genii_client_io_FileSystemUtils_isSoftLink
  (JNIEnv *env, jclass cls, jstring filepath)
{
	struct stat statBuf;
	const char *msg;
	const char *sFilepath = (*env)->GetStringUTFChars(env, filepath, NULL);
	if (sFilepath == NULL)
		return;

	if (lstat(sFilepath, &statBuf) < 0)
	{
		switch (errno)
		{
			case ENOENT :
				msg = "The file does not exist.";
				break;

			case EPERM :
				msg = "Permission denied for lstat command.";
				break;

			case ENAMETOOLONG :
              	msg = "Path is too long.";
				break;

       		case EACCES :
				msg = "Search  permission is denied on a component "
					"of the path prefix.";
				break;

			case ELOOP :
				msg = "Too many symbolic links were encountered in "
					"resolving path.";
				break;

			default :
				msg = "An I/O error occurred.";
				break;
		}

		throwSimpleException(env, (char*)IO_EXCEPTION_CLASS, (char*)msg);
	}

	(*env)->ReleaseStringUTFChars(env, filepath, sFilepath);

	return S_ISLNK(statBuf.st_mode);
}
