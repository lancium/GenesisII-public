#include <string>

#include "ogrsh/DirectoryStream.hpp"
#include "ogrsh/Logging.hpp"

#include "providers/geniifs/GeniiFSDirectoryStream.hpp"
#include "providers/geniifs/GeniiFSDirectoryDescriptor.hpp"

#include "jcomm/DirectoryClient.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		GeniiFSDirectoryStream::GeniiFSDirectoryStream(GeniiFSSession *session,
			GeniiFSMount *mount, const std::string &directoryKey,
			const std::string &fullpath)
			: DirectoryStream(NULL), _directoryKey(directoryKey)
		{
			_session = session;
			_mount = mount;

			setFileDescriptor(
				new GeniiFSDirectoryDescriptor(session, mount, fullpath));
		}

		GeniiFSDirectoryStream::GeniiFSDirectoryStream(
			GeniiFSSession *session, GeniiFSMount *mount,
			const std::string &directoryKey, GeniiFSDirectoryDescriptor *desc)
			: DirectoryStream(desc), _directoryKey(directoryKey)
		{
			_session = session;
			_mount = mount;
		}

		GeniiFSDirectoryStream::~GeniiFSDirectoryStream()
		{
			OGRSH_DEBUG("Calling closedir on open GeniFS Directory \""
				<< _directoryKey << "\".");

			jcomm::DirectoryClient client(*(_session->getSocket()));
			client.closedir(_directoryKey);
		}

		dirent* GeniiFSDirectoryStream::readdir()
		{
			OGRSH_DEBUG("Calling readdir on open GeniFS Directory \""
				<< _directoryKey << "\".");

			jcomm::DirectoryClient client(*(_session->getSocket()));
			jcomm::DirectoryEntry *entry = client.readdir(_directoryKey);
			if (entry == NULL)
				return NULL;

			_dent.d_ino = entry->_inode;
			_dent.d_off = 0;
			_dent.d_reclen = entry->_entryName.length();
			_dent.d_type = entry->_entryType;
			strcpy(_dent.d_name, entry->_entryName.c_str());

			delete entry;

			return &_dent;
		}

		dirent64* GeniiFSDirectoryStream::readdir64()
		{
			OGRSH_DEBUG("Calling readdir64 on open GeniFS Directory \""
				<< _directoryKey << "\".");

			jcomm::DirectoryClient client(*(_session->getSocket()));
			jcomm::DirectoryEntry *entry = client.readdir(_directoryKey);
			if (entry == NULL)
				return NULL;

			_dent64.d_ino = entry->_inode;
			_dent64.d_off = 0;
			_dent64.d_reclen = entry->_entryName.length();
			_dent64.d_type = entry->_entryType;
			strcpy(_dent64.d_name, entry->_entryName.c_str());

			delete entry;

			return &_dent64;
		}

		void GeniiFSDirectoryStream::rewinddir()
		{
			OGRSH_DEBUG("Calling rewinddir on open GeniFS Directory \""
				<< _directoryKey << "\".");

			jcomm::DirectoryClient client(*(_session->getSocket()));
			client.rewinddir(_directoryKey);
		}

		int GeniiFSDirectoryStream::dirfd()
		{
			return getFileDescriptor()->getFD();
		}
	}
}
