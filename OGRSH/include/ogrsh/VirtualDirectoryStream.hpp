#ifndef __VIRTUAL_DIRECTORY_STREAM_HPP__
#define __VIRTUAL_DIRECTORY_STREAM_HPP__

#include <list>
#include <string>

#include "ogrsh/DirectoryStream.hpp"

namespace ogrsh
{
	class VirtualDirectoryStream : public DirectoryStream
	{
		private:
			std::list<std::string> _entries;
			std::list<std::string>::iterator _iterator;
			dirent _currentDirent;
			dirent64 _currentDirent64;

		public:
			VirtualDirectoryStream(const std::list<std::string> &entries);

			virtual dirent* readdir();
			virtual dirent64* readdir64();

			virtual int dirfd();
	};
}

#endif
