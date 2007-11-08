#include <exception>
#include <string>

#include "jcomm/IOException.hpp"

namespace jcomm
{
	IOException::IOException(const std::string &message)
	{
		_what = message;
	}

	IOException::~IOException() throw ()
	{
	}

	const char* IOException::what() const throw()
	{
		return _what.c_str();
	}
}
