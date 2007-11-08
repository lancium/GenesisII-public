#include <string>

#include "jcomm/IOException.hpp"
#include "jcomm/IOGRSHWriteBuffer.hpp"
#include "jcomm/IPackable.hpp"

namespace jcomm
{
	IPackable::IPackable(const std::string &typeName)
	{
		_typeName = typeName;
	}

	IPackable::IPackable(const IPackable &other)
	{
		_typeName = other._typeName;
	}

	IPackable::~IPackable()
	{
	}

	IPackable& IPackable::operator= (const IPackable &other)
	{
		if (this != &other)
		{
			_typeName = other._typeName;
		}

		return *this;
	}

	const std::string& IPackable::getTypeName() const
	{
		return _typeName;
	}
}
