#include <errno.h>
#include <sys/param.h>

#include <pthread.h>

#include "ogrsh/FileDescriptor.hpp"
#include "ogrsh/FileDescriptorTable.hpp"
#include "ogrsh/Logging.hpp"

namespace ogrsh
{
	FileDescriptorTable* FileDescriptorTable::_instance = NULL;

	static pthread_mutex_t descMutex = PTHREAD_MUTEX_INITIALIZER;

	FileDescriptorTable::FileDescriptorTable()
	{
		_table = new FileDescriptor*[NOFILE];
		for (int lcv = 0; lcv < NOFILE; lcv++)
			_table[lcv] = NULL;
	}

	FileDescriptorTable::FileDescriptorTable(const FileDescriptorTable&)
	{
		OGRSH_FATAL("Not allowed to copy FileDescriptorTables.");
		ogrsh::shims::real_exit(1);
	}

	FileDescriptorTable& FileDescriptorTable::operator= (
		const FileDescriptorTable&)
	{
		OGRSH_FATAL("Not allowed to copy FileDescriptorTables.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	FileDescriptorTable::~FileDescriptorTable()
	{
		pthread_mutex_lock(&descMutex);

		for (int lcv = 0; lcv < NOFILE; lcv++)
		{
			if (_table[lcv])
			{
				delete _table[lcv];
			}
		}

		delete []_table;

		pthread_mutex_unlock(&descMutex);
	}

	int FileDescriptorTable::insert(FileDescriptor *desc)
	{
		pthread_mutex_lock(&descMutex);

		int descriptor = desc->getFD();

		if (descriptor < 0 || descriptor >= NOFILE)
		{
			OGRSH_FATAL("Invalid file descriptor used for table.");
			ogrsh::shims::real_exit(1);
		}

		_table[descriptor] = desc;

		pthread_mutex_unlock(&descMutex);

		return descriptor;
	}

	FileDescriptor* FileDescriptorTable::lookup(int descriptor)
	{
		pthread_mutex_lock(&descMutex);

		FileDescriptor* ret = _table[descriptor];

		pthread_mutex_unlock(&descMutex);

		if (ret == NULL)
		{
			errno = EBADF;
			return NULL;
		}

		return ret;
	}

	int FileDescriptorTable::close(int descriptor)
	{
		pthread_mutex_lock(&descMutex);

		FileDescriptor* ret = _table[descriptor];

		pthread_mutex_unlock(&descMutex);

		if (ret == NULL)
		{
			errno = EBADF;
			return -1;
		}

		delete ret;
		_table[descriptor] = NULL;

		return 0;
	}

	FileDescriptorTable& FileDescriptorTable::getInstance()
	{
		pthread_mutex_lock(&descMutex);

		if (_instance == NULL)
			_instance = new FileDescriptorTable();

		pthread_mutex_unlock(&descMutex);

		return *_instance;
	}
}
