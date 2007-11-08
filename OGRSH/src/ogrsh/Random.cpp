#include <stdlib.h>

#include "ogrsh/Random.hpp"
#include "ogrsh/Logging.hpp"

namespace ogrsh
{
	static int _initialized = 0;

	void initializeGenerators()
	{
		if (!_initialized)
		{
			srand48(time(NULL));
		}
	}

	int nextInt()
	{
		return lrand48();
	}

	long int nextLongInt()
	{
		return lrand48();
	}

	long long int nextLongLongInt()
	{
		long long int ret;
		long int *array = (long int*)&ret;

		if (sizeof(ret) == sizeof(long int))
		{
			ret = nextLongInt();
		} else if (sizeof(ret) == (2 * sizeof(long int)))
		{
			array[0] = nextLongInt();
			array[1] = nextLongInt();
		} else
		{
			OGRSH_FATAL("A pre-condition for nextLongLongInt cannot be met.");

			ogrsh::shims::real_exit(1);
		}

		return ret;
	}
}
