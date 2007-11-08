#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <string>

#include "ogrsh/FileDescriptor.hpp"
#include "ogrsh/FileFunctions.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/Path.hpp"

namespace ogrsh
{
	FileFunctions::FileFunctions(const FileFunctions&)
	{
		OGRSH_FATAL("Not allowed to copy file functions.");
		ogrsh::shims::real_exit(1);
	}

	FileFunctions& FileFunctions::operator= (const FileFunctions&)
	{
		OGRSH_FATAL("Not allowed to copy file functions.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	FileFunctions::FileFunctions()
	{
	}

	FileFunctions::~FileFunctions()
	{
	}
}
