#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <string>

#include "ogrsh/ExecuteFunctions.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/Path.hpp"

namespace ogrsh
{
	ExecuteFunctions::ExecuteFunctions(const ExecuteFunctions&)
	{
		OGRSH_FATAL("Not allowed to copy execute functions.");
		ogrsh::shims::real_exit(1);
	}

	ExecuteFunctions& ExecuteFunctions::operator= (const ExecuteFunctions&)
	{
		OGRSH_FATAL("Not allowed to copy execute functions.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	ExecuteFunctions::ExecuteFunctions()
	{
	}

	ExecuteFunctions::~ExecuteFunctions()
	{
	}
}
