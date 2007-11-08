#include <string>

#include "ogrsh/DirectoryStream.hpp"
#include "ogrsh/DirectoryFunctions.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/shims/Directory.hpp"

#include "providers/localfs/LocalFSDirectoryFunctions.hpp"
#include "providers/localfs/LocalFSDirectoryStream.hpp"

namespace ogrsh
{
	namespace localfs
	{
		LocalFSDirectoryFunctions::LocalFSDirectoryFunctions(
			const std::string &localSource)
			: _localSource(localSource)
		{
		}

		int LocalFSDirectoryFunctions::chdir(const ogrsh::Path &relativePath)
		{
			OGRSH_DEBUG("LocalFSDirectoryFunctions::chdir(\""
				<< (const std::string&)relativePath << "\") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			return ogrsh::shims::real_chdir(fullPath.c_str());
		}

		int LocalFSDirectoryFunctions::mkdir(
			const ogrsh::Path &relativePath, mode_t mode)
		{
			OGRSH_DEBUG("LocalFSDirectoryFunctions::mkdir(\""
				<< (const std::string&)relativePath << "\", "
				<< mode << ") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			return ogrsh::shims::real_mkdir(fullPath.c_str(), mode);
		}

		int LocalFSDirectoryFunctions::chmod(
			const ogrsh::Path &relativePath, mode_t mode)
		{
			OGRSH_DEBUG("LocalFSDirectoryFunctions::chmod(\""
				<< (const std::string&)relativePath << "\", "
				<< mode << ") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			return ogrsh::shims::real_chmod(fullPath.c_str(), mode);
		}

		int LocalFSDirectoryFunctions::rmdir(const ogrsh::Path &relativePath)
		{
			OGRSH_DEBUG("LocalFSDirectoryFunctions::rmdir(\""
				<< (const std::string&)relativePath << "\") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			return ogrsh::shims::real_rmdir(fullPath.c_str());
		}

		int LocalFSDirectoryFunctions::link(
			const ogrsh::Path &oldPath, const ogrsh::Path &newPath)
		{
			OGRSH_DEBUG("LocalFSDirectoryFunctions::link(\""
				<< (const std::string&)oldPath << "\", \""
				<< (const std::string&)newPath << "\") called.");

			std::string fullOldPath =
				(oldPath.length() == 0) ? _localSource :
					_localSource + (const std::string&)oldPath;
			std::string fullNewPath =
				(newPath.length() == 0) ? _localSource :
					_localSource + (const std::string&)newPath;

			return ogrsh::shims::real_link(
				fullOldPath.c_str(), fullNewPath.c_str());
		}

		int LocalFSDirectoryFunctions::rename(
			const ogrsh::Path &oldPath, const ogrsh::Path &newPath)
		{
			OGRSH_DEBUG("LocalFSDirectoryFunctions::rename(\""
				<< (const std::string&)oldPath << "\", \""
				<< (const std::string&)newPath << "\") called.");

			std::string fullOldPath =
				(oldPath.length() == 0) ? _localSource :
					_localSource + (const std::string&)oldPath;
			std::string fullNewPath =
				(newPath.length() == 0) ? _localSource :
					_localSource + (const std::string&)newPath;

			return ogrsh::shims::real_rename(
				fullOldPath.c_str(), fullNewPath.c_str());
		}

		ogrsh::DirectoryStream* LocalFSDirectoryFunctions::opendir(
			const ogrsh::Path &relativePath)
		{
			OGRSH_DEBUG("LocalFSDirectoryFunctions::opendir(\""
				<< (const std::string&)relativePath << "\") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			LocalFSDirectoryStream *ret = new LocalFSDirectoryStream(fullPath);
			if (!(ret->isValid()))
			{
				delete ret;
				ret = NULL;
			}

			return ret;
		}

		int LocalFSDirectoryFunctions::__xstat(int version,
			const ogrsh::Path &relativePath, struct stat *statbuf)
		{
			OGRSH_DEBUG("LocalFSDirectoryFunctions::__xstat("
				<< version << ", \""
				<< (const std::string&)relativePath << "\") called.");

			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			OGRSH_TRACE("LocalFSDirectoryFunctions::__xstat -> "
				<< "redirecting to __xstat(" << version << ", "
				<< fullPath.c_str() << ", ...)");

			return ogrsh::shims::real___xstat(
				version, fullPath.c_str(), statbuf);
		}

		int LocalFSDirectoryFunctions::__xstat64(int version,
			const ogrsh::Path &relativePath, struct stat64 *statbuf)
		{
			OGRSH_DEBUG("LocalFSDirectoryFunctions::__xstat64("
				<< version << ", \""
				<< (const std::string&)relativePath << "\") called.");

			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			OGRSH_TRACE("LocalFSDirectoryFunctions::__xstat -> "
				<< "redirecting to __xstat64(" << version << ", "
				<< fullPath.c_str() << ", ...)");

			return ogrsh::shims::real___xstat64(
				version, fullPath.c_str(), statbuf);
		}

		int LocalFSDirectoryFunctions::__lxstat(int version,
			const ogrsh::Path &relativePath, struct stat *statbuf)
		{
			OGRSH_DEBUG("LocalFSDirectoryFunctions::__lxstat("
				<< version << ", \""
				<< (const std::string&)relativePath << "\") called.");

			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			OGRSH_TRACE("LocalFSDirectoryFunctions::__lxstat -> "
				<< "redirecting to __lxstat(" << version << ", "
				<< fullPath.c_str() << ", ...)");

			return ogrsh::shims::real___lxstat(
				version, fullPath.c_str(), statbuf);
		}

		int LocalFSDirectoryFunctions::__lxstat64(int version,
			const ogrsh::Path &relativePath, struct stat64 *statbuf)
		{
			OGRSH_DEBUG("LocalFSDirectoryFunctions::__lxstat64("
				<< version << ", \""
				<< (const std::string&)relativePath << "\") called.");

			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			OGRSH_TRACE("LocalFSDirectoryFunctions::__lxstat -> "
				<< "redirecting to __lxstat64(" << version << ", "
				<< fullPath.c_str() << ", ...)");

			return ogrsh::shims::real___lxstat64(
				version, fullPath.c_str(), statbuf);
		}

		int LocalFSDirectoryFunctions::readlink(
			const ogrsh::Path &relativePath, char *buf, size_t bufsize)
		{
			OGRSH_DEBUG("LocalFSDirectoryFunctions::readlink("
				<< (const std::string&)relativePath << ", ...) called.");

			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			return ogrsh::shims::real_readlink(
				fullPath.c_str(), buf, bufsize);
		}
	}
}
