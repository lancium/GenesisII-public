#include "ogrsh/DynamicallyLoadedSymbol.hpp"

namespace ogrsh
{
	DynamicallyLoadedSymbol::DynamicallyLoadedSymbol(void *dlHandle)
	{
		_dlHandle = dlHandle;
	}

	void DynamicallyLoadedSymbol::setDLHandle(void *dlHandle)
	{
		_dlHandle = dlHandle;
	}

	void* DynamicallyLoadedSymbol::getDLHandle() const
	{
		return _dlHandle;
	}
}
