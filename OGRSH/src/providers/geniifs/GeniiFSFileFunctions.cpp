#include <string>

#include "ogrsh/Logging.hpp"

#include "ogrsh/FileDescriptor.hpp"
#include "ogrsh/FileFunctions.hpp"

#include "providers/geniifs/GeniiFSFileFunctions.hpp"
#include "providers/geniifs/GeniiFSFileDescriptor.hpp"
#include "providers/geniifs/GeniiFSDirectoryDescriptor.hpp"

#include "jcomm/FileClient.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		GeniiFSFileFunctions::GeniiFSFileFunctions(
			GeniiFSSession *session, GeniiFSMount *mount,
				const std::string &rnsSource)
			: _rnsSource(rnsSource)
		{
			_session = session;
			_mount = mount;
		}

		ogrsh::FileDescriptor* GeniiFSFileFunctions::open64(
			const ogrsh::Path &relativePath, int flags, mode_t mode)
		{
			OGRSH_DEBUG("GeniiFSFileFunctions::open64(\""
				<< (const std::string&)relativePath << "\", "
				<< flags << ", " << mode << ") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			jcomm::FileClient fc(*_session->getSocket());

			try
			{
				std::string fileDesc = fc.open(fullPath, flags, mode);
				if (fileDesc[0] == 'D')
					return new GeniiFSDirectoryDescriptor(
						_session, _mount, fullPath);


				return new GeniiFSFileDescriptor(_session, _mount, fileDesc);
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return NULL;
			}

			return NULL;
		}

		ogrsh::FileDescriptor* GeniiFSFileFunctions::creat(
			const ogrsh::Path &relativePath, mode_t mode)
		{
			OGRSH_DEBUG("GeniiFSFileFunctions::open(\""
				<< (const std::string&)relativePath << "\", "
				<< mode << ") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			/*
			try
			{
				std::string directoryKey = dc.opendir(fullPath);

				return new GeniiFSDirectoryStream(_session, directoryKey);
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return NULL;
			}
			*/

			OGRSH_FATAL("GeniiFSFileFunctions::creat NOT IMPLEMENTED.");
			ogrsh::shims::real_exit(1);

			return NULL;
		}

		int GeniiFSFileFunctions::unlink(const ogrsh::Path &relativePath)
		{
			OGRSH_DEBUG("GeniiFSFileFunctions::unlink(\""
				<< (const std::string&)relativePath << "\") called.");
	
			std::string fullPath =
				(relativePath.length() == 0) ? _rnsSource :
					_rnsSource + (const std::string&)relativePath;

			jcomm::FileClient fc(*_session->getSocket());

			try
			{
				return fc.unlink(fullPath);
			}
			catch (jcomm::OGRSHException oe)
			{
				oe.setErrno();
				return -1;
			}

			return -1;
		}
	}
}
