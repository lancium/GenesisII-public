#include <string>

#include "ogrsh/Logging.hpp"
#include "ogrsh/Session.hpp"

namespace ogrsh
{
	Session::Session(const Session&)
	{
		OGRSH_FATAL("Not allowed to copy Sessions.");
		ogrsh::shims::real_exit(1);
	}

	Session& Session::operator= (const Session&)
	{
		OGRSH_FATAL("Not allowed to copy Sessions.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	Session::Session(const std::string &sessionName)
		: _sessionName(sessionName)
	{
		_sessionStarted = false;
	}

	Session::~Session()
	{
	}

	const std::string& Session::getSessionName() const
	{
		return _sessionName;
	}

	void Session::beginSession()
	{
		if (_sessionStarted)
		{
			OGRSH_ERROR("Session \"" << _sessionName
				<< "\" has already been started.");
			ogrsh::shims::real_exit(1);
		}

		this->startSession();
		_sessionStarted = true;
	}

	void Session::endSession()
	{
		if (_sessionStarted)
		{
			this->stopSession();
			_sessionStarted = false;
		}
	}

	bool Session::isStarted() const
	{
		return _sessionStarted;
	}

	Mount* Session::mountLocation(
		const std::string &mountLocation,
		const xercesc_2_8::DOMElement &configNode)
	{
		if (!isStarted())
			beginSession();

		return createMount(mountLocation, configNode);
	}
}
