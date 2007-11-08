#include <string>

#include "ogrsh/Mount.hpp"
#include "ogrsh/DirectoryFunctions.hpp"
#include "ogrsh/ACLFunctions.hpp"
#include "ogrsh/ExecuteFunctions.hpp"

#include "providers/localfs/LocalFSMount.hpp"
#include "providers/localfs/LocalFSDirectoryFunctions.hpp"
#include "providers/localfs/LocalFSFileFunctions.hpp"
#include "providers/localfs/LocalFSExecuteFunctions.hpp"
#include "providers/localfs/LocalFSACLFunctions.hpp"

namespace ogrsh
{
	namespace localfs
	{
		LocalFSMount::LocalFSMount(const std::string &location,
			const std::string &sourceLocation)
			: ogrsh::Mount(location)
		{
			_dirFunctions = new LocalFSDirectoryFunctions(sourceLocation);
			_aclFunctions = new LocalFSACLFunctions(sourceLocation);
			_fileFunctions = new LocalFSFileFunctions(sourceLocation);
			_executeFunctions = new LocalFSExecuteFunctions(sourceLocation);
		}

		LocalFSMount::~LocalFSMount()
		{
			delete _dirFunctions;
			delete _aclFunctions;
			delete _fileFunctions;
			delete _executeFunctions;
		}

		ogrsh::DirectoryFunctions* LocalFSMount::getDirectoryFunctions()
		{
			return _dirFunctions;
		}

		ogrsh::ACLFunctions* LocalFSMount::getACLFunctions()
		{
			return _aclFunctions;
		}

		ogrsh::FileFunctions* LocalFSMount::getFileFunctions()
		{
			return _fileFunctions;
		}

		ogrsh::ExecuteFunctions* LocalFSMount::getExecuteFunctions()
		{
			return _executeFunctions;
		}
	}
}
