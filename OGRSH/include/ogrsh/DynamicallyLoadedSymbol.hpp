#ifndef __DYNAMICALLY_LOADED_SYMBOL_HPP__
#define __DYNAMICALLY_LOADED_SYMBOL_HPP__

#include <stdlib.h>

namespace ogrsh
{
	class DynamicallyLoadedSymbol
	{
		private:
			void *_dlHandle;

		protected:
			DynamicallyLoadedSymbol(void *dlHandle = NULL);

		public:
			void setDLHandle(void *dlHandle);
			void* getDLHandle() const;
	};
}

#endif
