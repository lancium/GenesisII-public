#include "ogrsh/Logging.hpp"
#include "ogrsh/FileDescriptor.hpp"

#include "ogrsh/shims/File.hpp"

namespace ogrsh
{
	FileDescriptor::FileDescriptor(const FileDescriptor&)
	{
		OGRSH_FATAL("Not allowed to copy FileDescriptors.");
		ogrsh::shims::real_exit(1);
	}

	FileDescriptor& FileDescriptor::operator= (const FileDescriptor&)
	{
		OGRSH_FATAL("Not allowed to copy FileDescriptors.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	FileDescriptor::FileDescriptor(int fd, bool mustClose)
	{
		_mustClose = mustClose;

		if (fd < 0)
		{
			fd = ogrsh::shims::real_open("/dev/null", O_WRONLY, 0);
		}

		_fd = fd;
	}

	FileDescriptor::~FileDescriptor()
	{
		if (_fd >= 0 && _mustClose)
			ogrsh::shims::real_close(_fd);
	}

	std::string FileDescriptor::getFullVirtualPath() const
	{
		return _fullVirtualPath;
	}

	void FileDescriptor::setFullVirtualPath(const std::string &fullVirtualPath)
	{
		_fullVirtualPath = fullVirtualPath;
	}

	int FileDescriptor::getFD() const
	{
		return _fd;
	}

	int FileDescriptor::acquireDupDescriptor(int oldfd, int newfd)
	{
		int ret;

		if (newfd >= 0)
			ret = ogrsh::shims::real_dup2(oldfd, newfd);
		else
			ret = ogrsh::shims::real_dup(oldfd);

		return ret;
	}
}
