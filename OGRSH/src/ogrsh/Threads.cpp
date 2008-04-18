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

		SHIM_DEF(void, pthread_exit, (void *value_arg),
				(value_arg))
		{
			OGRSH_TRACE("pthread_exit(...) called.");

			stopAllShims();
			real_pthread_exit(value_arg);
			startAllShims();
		}

		SHIM_DEF(int, pthread_join, (pthread_t thread, void **value_ptr),
			(thread, value_ptr))
		{
			OGRSH_TRACE("pthread_join(...) called.");

			int ret;

			stopAllShims();
			ret = real_pthread_join(thread, value_ptr);
			startAllShims();

			return ret;
		}

		void startThreadShims()
		{
			START_SHIM(pthread_create);
			START_SHIM(pthread_exit);
			START_SHIM(pthread_join);
		}

		void stopThreadShims()
		{
			STOP_SHIM(pthread_join);
			STOP_SHIM(pthread_exit);
			STOP_SHIM(pthread_create);
		}
	}
}
