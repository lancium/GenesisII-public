#ifndef __GENIIFS_FILE_FUNCTIONS_HPP__
#define __GENIIFS_FILE_FUNCTIONS_HPP__

#include <string>

#include "ogrsh/FileDescriptor.hpp"
#include "ogrsh/FileFunctions.hpp"

#include "providers/geniifs/GeniiFSSession.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		class GeniiFSMount;

		class GeniiFSFileFunctions : public ogrsh::FileFunctions
		{
			private:
				GeniiFSSession *_session;
				GeniiFSMount *_mount;
				std::string _rnsSource;

			public:
				GeniiFSFileFunctions(GeniiFSSession *session,
					GeniiFSMount *mount,
					const std::string &rnsSource);

				virtual ogrsh::FileDescriptor* open64(
					const ogrsh::Path &relativePath, int flags, mode_t mode);
				virtual ogrsh::FileDescriptor* creat(
					const ogrsh::Path &relativePath, mode_t mode);
				virtual int unlink(
					const ogrsh::Path &relativePath);
		};
	}
}

#endif
