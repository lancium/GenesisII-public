#include <string>

#include "ogrsh/Logging.hpp"
#include "ogrsh/Mount.hpp"
#include "ogrsh/Random.hpp"

namespace ogrsh
{
	Mount::Mount(const Mount&)
	{
		OGRSH_FATAL("Not allowed to copy mounts.");
		ogrsh::shims::real_exit(1);
	}

	Mount& Mount::operator= (const Mount&)
	{
		OGRSH_FATAL("Not allowed to copy mounts.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	Mount::Mount(const std::string &location)
		: _location(location)
	{
		_deviceNumber = nextLongLongInt();
	}

	Mount::~Mount()
	{
	}

	const std::string& Mount::getMountLocation() const
	{
		return _location;
	}

	dev_t Mount::getDeviceID() const
	{
		return _deviceNumber;
	}
}
