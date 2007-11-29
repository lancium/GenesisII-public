#include <string>

#include <errno.h>

#include "ogrsh/Logging.hpp"

#include "ogrsh/DirectoryStream.hpp"
#include "ogrsh/DirectoryFunctions.hpp"

#include "ogrsh/shims/File.hpp"

#include "providers/geniifs/GeniiFSDirectoryFunctions.hpp"
#include "providers/geniifs/GeniiFSDirectoryStream.hpp"
#include "providers/geniifs/GeniiFSMount.hpp"

#include "jcomm/DirectoryClient.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		GeniiFSDirectoryFunctions::GeniiFSDirectoryFunctions(
			GeniiFSSession *session, GeniiFSMount *mount,
				const std::string &rnsSource)
			: _rnsSource(rnsSource)
		{
			_session = session;
			_mount = mount;
		}

		int GeniiFSDirectoryFunctions::utime(const ogrsh::Path &relativePath,
			const struct utimbuf *buf)
		{
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::utime(\""
				<< (const std::string&)relativePath << "\", ...) called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			ogrsh::shims::uber_real_fprintf(
				stderr, "Haven't implemented GENII utime yet\n");

			return 0;
		}

		int GeniiFSDirectoryFunctions::utimes(const ogrsh::Path &relativePath,
			const struct timeval *times)
		{
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::utimes(\""
				<< (const std::string&)relativePath << "\", ...) called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			ogrsh::shims::uber_real_fprintf(
				stderr, "Haven't implemented GENII utimes yet\n");

			return 0;
		}

		int GeniiFSDirectoryFunctions::chdir(const ogrsh::Path &relativePath)
		{
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::chdir(\""
				<< (const std::string&)relativePath << "\") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			jcomm::DirectoryClient dc(*_session->getSocket());

			try
			{
				return dc.chdir(fullPath);
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return -1;
			}

			return -1;
		}

		int GeniiFSDirectoryFunctions::mkdir(
			const ogrsh::Path &relativePath, mode_t mode)
		{
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::mkdir(\""
				<< (const std::string&)relativePath << "\", "
				<< mode << ") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			jcomm::DirectoryClient dc(*_session->getSocket());

			try
			{
				return dc.mkdir(fullPath, (int)mode);
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return -1;
			}

			return -1;
		}

		int GeniiFSDirectoryFunctions::chmod(
			const ogrsh::Path &relativePath, mode_t mode)
		{
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::chmod(\""
				<< (const std::string&)relativePath << "\", "
				<< mode << ") called.");

			/* MOOCH we don't really support chmod at this point. */
			return 0;
		}

		int GeniiFSDirectoryFunctions::rmdir(const ogrsh::Path &relativePath)
		{
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::rmdir(\""
				<< (const std::string&)relativePath << "\") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			jcomm::DirectoryClient dc(*_session->getSocket());

			try
			{
				return dc.rmdir(fullPath);
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return -1;
			}

			return -1;
		}

		int GeniiFSDirectoryFunctions::link(
			const ogrsh::Path &oldPath, const ogrsh::Path &newPath)
		{
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::link(\""
				<< (const std::string&)oldPath << "\", \""
				<< (const std::string&)newPath << "\") called.");

			std::string fullOldPath =
				(oldPath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)oldPath;
			std::string fullNewPath =
				(newPath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)newPath;

			jcomm::DirectoryClient dc(*_session->getSocket());

			try
			{
				return dc.link(fullOldPath, fullNewPath);
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return -1;
			}

			return -1;
		}

		int GeniiFSDirectoryFunctions::rename(
			const ogrsh::Path &oldPath, const ogrsh::Path &newPath)
		{
/* MOOCH
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::rename(\""
				<< (const std::string&)oldPath << "\", \""
				<< (const std::string&)newPath << "\") called.");

			std::string fullOldPath =
				(oldPath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)oldPath;
			std::string fullNewPath =
				(newPath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)newPath;

			jcomm::DirectoryClient dc(*_session->getSocket());

			try
			{
				return dc.rename(fullOldPath, fullNewPath);
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return -1;
			}

			return -1;
*/
			OGRSH_FATAL("GeniiFSDirectoryFunctions::rename is unimplemented.");
			ogrsh::shims::real_exit(1);
			return -1;
		}

		ogrsh::DirectoryStream* GeniiFSDirectoryFunctions::opendir(
			const ogrsh::Path &relativePath)
		{
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::opendir(\""
				<< (const std::string&)relativePath << "\") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			jcomm::DirectoryClient dc(*_session->getSocket());

			try
			{
				std::string directoryKey = dc.opendir(fullPath);

				return new GeniiFSDirectoryStream(_session, _mount, 
					directoryKey, fullPath);
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return NULL;
			}

			return NULL;
		}

		int GeniiFSDirectoryFunctions::__xstat(int version,
			const ogrsh::Path &relativePath, struct stat *statbuf)
		{
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::__xstat(\""
				<< (const std::string&)relativePath << "\") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			jcomm::DirectoryClient dc(*_session->getSocket());

			try
			{
				jcomm::StatBuffer statBuf = dc.xstat(fullPath);

				statbuf->st_dev = _mount->getDeviceID();
				statbuf->st_ino = statBuf.st_ino;
				statbuf->st_mode = statBuf.st_mode;
				statbuf->st_nlink = 0;
				statbuf->st_uid = 0;
				statbuf->st_gid = 0;
				statbuf->st_rdev = 0;
				statbuf->st_size = statBuf.st_size;
				statbuf->st_blksize = statBuf.st_blocksize;
				statbuf->st_blocks = (statBuf.st_size +511) / 512;
				statbuf->st_atime = statBuf._st_atime;
				statbuf->st_mtime = statBuf._st_mtime;
				statbuf->st_ctime = statBuf._st_ctime;
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return -1;
			}

			return 0;
		}

		int GeniiFSDirectoryFunctions::__xstat64(int version,
			const ogrsh::Path &relativePath, struct stat64 *statbuf)
		{
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::__xstat64(\""
				<< (const std::string&)relativePath << "\") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			jcomm::DirectoryClient dc(*_session->getSocket());

			try
			{
				jcomm::StatBuffer statBuf = dc.xstat(fullPath);

				statbuf->st_dev = _mount->getDeviceID();
				statbuf->st_ino = statBuf.st_ino;
				statbuf->st_mode = statBuf.st_mode;
				statbuf->st_nlink = 0;
				statbuf->st_uid = 0;
				statbuf->st_gid = 0;
				statbuf->st_rdev = 0;
				statbuf->st_size = statBuf.st_size;
				statbuf->st_blksize = statBuf.st_blocksize;
				statbuf->st_blocks = (statBuf.st_size +511) / 512;
				statbuf->st_atime = statBuf._st_atime;
				statbuf->st_mtime = statBuf._st_mtime;
				statbuf->st_ctime = statBuf._st_ctime;
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return -1;
			}

			return 0;
		}

		int GeniiFSDirectoryFunctions::__lxstat(int version,
			const ogrsh::Path &relativePath, struct stat *statbuf)
		{
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::__lxstat(\""
				<< (const std::string&)relativePath << "\") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			jcomm::DirectoryClient dc(*_session->getSocket());

			try
			{
				jcomm::StatBuffer statBuf = dc.xstat(fullPath);

				statbuf->st_dev = 0;
				statbuf->st_ino = statBuf.st_ino;
				statbuf->st_mode = statBuf.st_mode;
				statbuf->st_nlink = 0;
				statbuf->st_uid = 0;
				statbuf->st_gid = 0;
				statbuf->st_rdev = 0;
				statbuf->st_size = statBuf.st_size;
				statbuf->st_blksize = statBuf.st_blocksize;
				statbuf->st_blocks = (statBuf.st_size +511) / 512;
				statbuf->st_atime = statBuf._st_atime;
				statbuf->st_mtime = statBuf._st_mtime;
				statbuf->st_ctime = statBuf._st_ctime;
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return -1;
			}

			return 0;
		}

		int GeniiFSDirectoryFunctions::__lxstat64(int version,
			const ogrsh::Path &relativePath, struct stat64 *statbuf)
		{
			OGRSH_DEBUG("GeniiFSDirectoryFunctions::__lxstat64(\""
				<< (const std::string&)relativePath << "\") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			jcomm::DirectoryClient dc(*_session->getSocket());

			try
			{
				jcomm::StatBuffer statBuf = dc.xstat(fullPath);

				statbuf->st_dev = 0;
				statbuf->st_ino = statBuf.st_ino;
				statbuf->st_mode = statBuf.st_mode;
				statbuf->st_nlink = 0;
				statbuf->st_uid = 0;
				statbuf->st_gid = 0;
				statbuf->st_rdev = 0;
				statbuf->st_size = statBuf.st_size;
				statbuf->st_blksize = statBuf.st_blocksize;
				statbuf->st_blocks = (statBuf.st_size +511) / 512;
				statbuf->st_atime = statBuf._st_atime;
				statbuf->st_mtime = statBuf._st_mtime;
				statbuf->st_ctime = statBuf._st_ctime;
			}
			catch (jcomm::OGRSHException oe)
			{
				OGRSH_INFO("Error trying to __lxstat64.  "
					<< oe.what());
				oe.setErrno();
				return -1;
			}

			return 0;
		}

		int GeniiFSDirectoryFunctions::readlink(const Path &relativePath,
			char *buf, size_t bufsize)
		{
			// There are no links, so this should never happen.
			return -1;
		}
	}
}
