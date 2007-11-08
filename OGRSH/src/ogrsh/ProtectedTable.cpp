#include <errno.h>
#include <sys/param.h>

#include <pthread.h>

#include "ogrsh/Logging.hpp"
#include "ogrsh/ProtectedTable.hpp"

namespace ogrsh
{
	ProtectedTable* ProtectedTable::_instance = NULL;

	static pthread_mutex_t descMutex = PTHREAD_MUTEX_INITIALIZER;

	ProtectedTable::ProtectedTable()
	{
		_table = new bool[NOFILE];
		for (int lcv = 0; lcv < NOFILE; lcv++)
			_table[lcv] = false;
	}

	ProtectedTable::ProtectedTable(const ProtectedTable&)
	{
		OGRSH_FATAL("Not allowed to copy ProtectedTable.");
		ogrsh::shims::real_exit(1);
	}

	ProtectedTable& ProtectedTable::operator= (
		const ProtectedTable&)
	{
		OGRSH_FATAL("Not allowed to copy ProtectedTable.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	ProtectedTable::~ProtectedTable()
	{
		pthread_mutex_lock(&descMutex);

		delete []_table;

		pthread_mutex_unlock(&descMutex);
	}

	void ProtectedTable::protect(int fd)
	{
		pthread_mutex_lock(&descMutex);

		_table[fd] = true;

		pthread_mutex_unlock(&descMutex);
	}

	bool ProtectedTable::isProtected(int descriptor)
	{
		bool ret;
		pthread_mutex_lock(&descMutex);

		ret = _table[descriptor];

		pthread_mutex_unlock(&descMutex);

		return ret;
	}

	void ProtectedTable::unprotect(int descriptor)
	{
		pthread_mutex_lock(&descMutex);

		_table[descriptor] = false;

		pthread_mutex_unlock(&descMutex);
	}

	ProtectedTable& ProtectedTable::getInstance()
	{
		pthread_mutex_lock(&descMutex);

		if (_instance == NULL)
			_instance = new ProtectedTable();

		pthread_mutex_unlock(&descMutex);

		return *_instance;
	}
}
