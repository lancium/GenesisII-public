#include "ogrsh/shims/Threads.hpp"
#include "ogrsh/Logging.hpp"

#include <pthread.h>
#include <dlfcn.h>

namespace ogrsh
{
	void startAllShims();
	void stopAllShims();

	namespace shims
	{
		SHIM_DEF(int, pthread_create, (pthread_t *thread,
			const pthread_attr_t *attr,
			void (*start_routine)(void*), void *arg),
			(thread, attr, start_routine, arg))
		{
			OGRSH_TRACE("pthread_create(...) called.");

			int ret;

			stopAllShims();
			ret = real_pthread_create(thread, attr, start_routine, arg);
			startAllShims();

			return ret;
		}

		void startThreadShims()
		{
			START_SHIM(pthread_create);
		}

		void stopThreadShims()
		{
			STOP_SHIM(pthread_create);
		}
	}
}
