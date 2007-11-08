#include "ogrsh/DirectoryStream.hpp"
#include "ogrsh/FileDescriptorTable.hpp"
#include "ogrsh/Logging.hpp"

namespace ogrsh
{
	DirectoryStream::DirectoryStream(const DirectoryStream&)
	{
		OGRSH_FATAL("Not allowed to copy directory streams.");
		ogrsh::shims::real_exit(1);
	}

	DirectoryStream& DirectoryStream::operator= (const DirectoryStream&)
	{
		OGRSH_FATAL("Not allowed to copy directory streams.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	DirectoryStream::DirectoryStream(FileDescriptor *fd)
	{
		_fd = NULL;
		setFileDescriptor(fd);
	}

	DirectoryStream::~DirectoryStream()
	{
		if (_fd != NULL)
			FileDescriptorTable::getInstance().close(_fd->getFD());
	}

	FileDescriptor* DirectoryStream::getFileDescriptor()
	{
		return _fd;
	}

	void DirectoryStream::setFileDescriptor(FileDescriptor *fd)
	{
		if (_fd != NULL)
		{
			FileDescriptorTable::getInstance().close(_fd->getFD());
		}

		_fd = fd;

		if (_fd != NULL)
		{
			FileDescriptorTable::getInstance().insert(fd);
		}
	}
}
