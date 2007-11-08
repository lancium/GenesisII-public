#include <string>

#include "jcomm/IPackable.hpp"
#include "jcomm/IOGRSHReadBuffer.hpp"

namespace jcomm
{
	IOGRSHReadBuffer::~IOGRSHReadBuffer()
	{
	}

	IOGRSHReadBuffer& IOGRSHReadBuffer::operator= (
		const IOGRSHReadBuffer &other)
	{
		return *this;
	}
}
