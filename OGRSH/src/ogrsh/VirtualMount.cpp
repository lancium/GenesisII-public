#include <string>

#include "ogrsh/Mount.hpp"
#include "ogrsh/MountTree.hpp"
#include "ogrsh/DirectoryFunctions.hpp"
#include "ogrsh/ACLFunctions.hpp"
#include "ogrsh/FileFunctions.hpp"
#include "ogrsh/ExecuteFunctions.hpp"

#include "ogrsh/VirtualMount.hpp"
#include "ogrsh/VirtualDirectoryFunctions.hpp"
#include "ogrsh/VirtualACLFunctions.hpp"
#include "ogrsh/VirtualFileFunctions.hpp"
#include "ogrsh/VirtualExecuteFunctions.hpp"

namespace ogrsh
{
	VirtualMount::VirtualMount(MountTree *mountTree)
		: Mount("/")
	{
		_dirFunctions = new VirtualDirectoryFunctions(mountTree);
		_aclFunctions = new VirtualACLFunctions(mountTree);
		_fileFunctions = new VirtualFileFunctions(mountTree);
		_executeFunctions = new VirtualExecuteFunctions(mountTree);
	}

	VirtualMount::~VirtualMount()
	{
		delete _dirFunctions;
		delete _aclFunctions;
		delete _fileFunctions;
		delete _executeFunctions;
	}

	DirectoryFunctions* VirtualMount::getDirectoryFunctions()
	{
		return _dirFunctions;
	}

	ACLFunctions* VirtualMount::getACLFunctions()
	{
		return _aclFunctions;
	}

	FileFunctions* VirtualMount::getFileFunctions()
	{
		return _fileFunctions;
	}

	ExecuteFunctions* VirtualMount::getExecuteFunctions()
	{
		return _executeFunctions;
	}
}
