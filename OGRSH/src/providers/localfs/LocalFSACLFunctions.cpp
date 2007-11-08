#include <string>

#include "ogrsh/ACLFunctions.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/shims/ACL.hpp"

#include "providers/localfs/LocalFSACLFunctions.hpp"

namespace ogrsh
{
	namespace localfs
	{
		LocalFSACLFunctions::LocalFSACLFunctions(
			const std::string &localSource)
			: _localSource(localSource)
		{
		}

		acl_t LocalFSACLFunctions::acl_get_file(
			const ogrsh::Path &relativePath, acl_type_t type)
		{
			OGRSH_DEBUG("LocalFSACLFunctions::acl_get_file("
				<< (const std::string&)relativePath << "\") called.");

			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			OGRSH_TRACE("LocalFSACLFunctions::acl_get_file -> "
				<< "redirecting to acl_get_file("
				<< fullPath.c_str() << ", ...)");

			return ogrsh::shims::real_acl_get_file(
				fullPath.c_str(), type);
		}

		int LocalFSACLFunctions::acl_extended_file(
			const ogrsh::Path &relativePath)
		{
			OGRSH_DEBUG("LocalFSACLFunctions::acl_extended_file("
				<< (const std::string&)relativePath << "\") called.");

			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			OGRSH_TRACE("LocalFSACLFunctions::acl_extended_file -> "
				<< "redirecting to acl_extended_file("
				<< fullPath.c_str() << ", ...)");

			return ogrsh::shims::real_acl_extended_file(
				fullPath.c_str());
		}

		int LocalFSACLFunctions::access(
			const ogrsh::Path &relativePath, int mode)
		{
			OGRSH_DEBUG("LocalFSACLFunctions::access("
				<< (const std::string&)relativePath << "\", ...) called.");

			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			OGRSH_TRACE("LocalFSACLFunctions::access -> "
				<< "redirecting to access("
				<< fullPath.c_str() << ", ...)");

			return ogrsh::shims::real_access(
				fullPath.c_str(), mode);
		}

		int LocalFSACLFunctions::eaccess(
			const ogrsh::Path &relativePath, int mode)
		{
			OGRSH_DEBUG("LocalFSACLFunctions::eaccess("
				<< (const std::string&)relativePath << "\", ...) called.");

			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			OGRSH_TRACE("LocalFSACLFunctions::eaccess -> "
				<< "redirecting to eaccess("
				<< fullPath.c_str() << ", ...)");

			return ogrsh::shims::real_eaccess(
				fullPath.c_str(), mode);
		}

		int LocalFSACLFunctions::euidaccess(
			const ogrsh::Path &relativePath, int mode)
		{
			OGRSH_DEBUG("LocalFSACLFunctions::euidaccess("
				<< (const std::string&)relativePath << "\", ...) called.");

			std::string fullPath =
				(relativePath.length() == 0) ? _localSource :
					_localSource + (const std::string&)relativePath;

			OGRSH_TRACE("LocalFSACLFunctions::euidaccess -> "
				<< "redirecting to euidaccess("
				<< fullPath.c_str() << ", ...)");

			return ogrsh::shims::real_euidaccess(
				fullPath.c_str(), mode);
		}
	}
}
