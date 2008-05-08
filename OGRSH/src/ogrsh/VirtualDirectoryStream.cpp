#include <list>
#include <string>

#include "ogrsh/DirectoryStream.hpp"
#include "ogrsh/VirtualDirectoryStream.hpp"
#include "ogrsh/VirtualFileDescriptor.hpp"

namespace ogrsh
{
/*
			std::list<std::string> _entries;
			std::list<std::string>::iterator _iterator;
			dirent _currentDirent;
			dirent64 _currentDirent64;
*/

	VirtualDirectoryStream::VirtualDirectoryStream(
		const std::list<std::string> &entries)
		: DirectoryStream(new VirtualFileDescriptor())
	{
		_entries = entries;
		_iterator = _entries.begin();
	}

	dirent* VirtualDirectoryStream::readdir()
	{
		if (_iterator == _entries.end())
			return NULL;

		std::string entry = *_iterator;
		_currentDirent.d_ino = 0;
		_currentDirent.d_off = 0;
		_currentDirent.d_reclen = (unsigned short)entry.length();
		_currentDirent.d_type = DT_DIR;
		strcpy(_currentDirent.d_name, entry.c_str());

		++_iterator;

		return &_currentDirent;
	}

	dirent64* VirtualDirectoryStream::readdir64()
	{
		if (_iterator == _entries.end())
			return NULL;

		std::string entry = *_iterator;
		_currentDirent64.d_ino = 0;
		_currentDirent64.d_off = 0;
		_currentDirent64.d_reclen = (unsigned short)entry.length();
		_currentDirent.d_type = DT_DIR;
		strcpy(_currentDirent64.d_name, entry.c_str());

		++_iterator;

		return &_currentDirent64;
	}

	void VirtualDirectoryStream::rewinddir()
	{
		_iterator = _entries.begin();
	}

	int VirtualDirectoryStream::dirfd()
	{
		return getFileDescriptor()->getFD();
	}
}
