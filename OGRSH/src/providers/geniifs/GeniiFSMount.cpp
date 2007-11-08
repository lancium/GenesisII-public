#include <string>

#include "ogrsh/Mount.hpp"
#include "ogrsh/DirectoryFunctions.hpp"
#include "ogrsh/FileFunctions.hpp"
#include "ogrsh/ExecuteFunctions.hpp"
#include "ogrsh/ACLFunctions.hpp"

#include "providers/geniifs/GeniiFSSession.hpp"
#include "providers/geniifs/GeniiFSMount.hpp"
#include "providers/geniifs/GeniiFSDirectoryFunctions.hpp"
#include "providers/geniifs/GeniiFSFileFunctions.hpp"
#include "providers/geniifs/GeniiFSExecuteFunctions.hpp"
#include "providers/geniifs/GeniiFSACLFunctions.hpp"

namespace ogrsh
{
	namespace geniifs
	{
/*
				GeniiFSSession *_session;

				ogrsh::DirectoryFunctions *_dirFunctions;
				ogrsh::ACLFunctions *_aclFunctions;
*/

		GeniiFSMount::GeniiFSMount(GeniiFSSession *session,
			const std::string &location,
			const std::string &sourceLocation)
			: ogrsh::Mount(location)
		{
			_dirFunctions = new GeniiFSDirectoryFunctions(
				session, this, sourceLocation);
			_aclFunctions = new GeniiFSACLFunctions(
				session, sourceLocation);
			_fileFunctions = new GeniiFSFileFunctions(
				session, this, sourceLocation);
			_executeFunctions = new GeniiFSExecuteFunctions(
				session, this, sourceLocation);
		}

		GeniiFSMount::~GeniiFSMount()
		{
			delete _dirFunctions;
			delete _aclFunctions;
			delete _fileFunctions;
			delete _executeFunctions;
		}

		ogrsh::DirectoryFunctions* GeniiFSMount::getDirectoryFunctions()
		{
			return _dirFunctions;
		}

		ogrsh::ACLFunctions* GeniiFSMount::getACLFunctions()
		{
			return _aclFunctions;
		}

		ogrsh::FileFunctions* GeniiFSMount::getFileFunctions()
		{
			return _fileFunctions;
		}

		ogrsh::ExecuteFunctions* GeniiFSMount::getExecuteFunctions()
		{
			return _executeFunctions;
		}
	}
}
