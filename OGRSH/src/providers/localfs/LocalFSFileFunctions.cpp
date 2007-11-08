#include <string>

#include "ogrsh/FileFunctions.hpp"
#include "ogrsh/FileDescriptor.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/shims/File.hpp"

#include "providers/localfs/LocalFSFileFunctions.hpp"
#include "providers/localfs/LocalFSFileDescriptor.hpp"

namespace ogrsh
{
	namespace localfs
	{
		LocalFSFileFunctions::LocalFSFileFunctions(
			const std::string &localSource)
			: _localSource(localSource)
		{
		}

		ogrsh::FileDescriptor* LocalFSFileFunctions::open64(
			const ogrsh::Path &relativePath, int flags, mode_t mode)
		{
			OGRSH_DEBUG("LocalFSFileFunctions::open64(\""
				<< (const std::string&)relativePath << "\", "
				<< flags << ", " << mode << ") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			int fd = ogrsh::shims::real_open64(fullPath.c_str(),
				flags, mode);
			if (fd < 0)
				return NULL;

			OGRSH_DEBUG("Returning from open64 with fd " << fd);
			return new LocalFSFileDescriptor(fd);
		}

		ogrsh::FileDescriptor* LocalFSFileFunctions::creat(
			const ogrsh::Path &relativePath, mode_t mode)
		{
			OGRSH_DEBUG("LocalFSFileFunctions::creat(\""
				<< (const std::string&)relativePath << "\", "
				<< mode << ") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			OGRSH_FATAL("LocalFSFileFunctions::creat NOT IMPLEMENTED.");
			ogrsh::shims::real_exit(1);
			return NULL;

			// return new LocalFSFileDescriptor(fullPath);
		}

		int LocalFSFileFunctions::unlink(const ogrsh::Path &relativePath)
		{
			OGRSH_DEBUG("LocalFSFileFunctions::unlink(\""
				<< (const std::string&)relativePath << "\") called.");

			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			return ogrsh::shims::real_unlink(fullPath.c_str());
		}
	}
}
