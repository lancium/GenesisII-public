#ifndef __MOUNT_HPP__
#define __MOUNT_HPP__

#include <string>

#include "ogrsh/ACLFunctions.hpp"
#include "ogrsh/DirectoryFunctions.hpp"
#include "ogrsh/FileFunctions.hpp"
#include "ogrsh/ExecuteFunctions.hpp"

namespace ogrsh
{
	class Mount
	{
		private:
			Mount(const Mount&);
			Mount& operator= (const Mount&);

		protected:
			std::string _location;
			dev_t _deviceNumber;

			Mount(const std::string &location);

		public:
			virtual ~Mount();

			const std::string& getMountLocation() const;
			dev_t getDeviceID() const;

			virtual DirectoryFunctions* getDirectoryFunctions() = 0;
			virtual ACLFunctions* getACLFunctions() = 0;
			virtual FileFunctions* getFileFunctions() = 0;
			virtual ExecuteFunctions* getExecuteFunctions() = 0;
	};
}

#endif
