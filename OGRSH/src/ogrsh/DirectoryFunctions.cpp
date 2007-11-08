#include <string>

#include "ogrsh/DirectoryFunctions.hpp"
#include "ogrsh/DirectoryStream.hpp"
#include "ogrsh/Logging.hpp"

namespace ogrsh
{
	DirectoryFunctions::DirectoryFunctions(const DirectoryFunctions&)
	{
		OGRSH_FATAL("Not allowed to copy directory functions.");
		ogrsh::shims::real_exit(1);
	}

	DirectoryFunctions& DirectoryFunctions::operator= (
		const DirectoryFunctions&)
	{
		OGRSH_FATAL("Not allowed to copy directory functions.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	DirectoryFunctions::DirectoryFunctions()
	{
	}

	DirectoryFunctions::~DirectoryFunctions()
	{
	}
}
