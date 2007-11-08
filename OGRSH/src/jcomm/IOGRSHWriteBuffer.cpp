#include <string>

#include "jcomm/IPackable.hpp"
#include "jcomm/IOGRSHWriteBuffer.hpp"

namespace jcomm
{
	IOGRSHWriteBuffer::~IOGRSHWriteBuffer()
	{
	}

	IOGRSHWriteBuffer& IOGRSHWriteBuffer::operator= (
		const IOGRSHWriteBuffer &other)
	{
		return *this;
	}
}
