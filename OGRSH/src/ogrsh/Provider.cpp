#include <string>
#include <map>

#include "ogrsh/DynamicallyLoadedSymbol.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/Provider.hpp"
#include "ogrsh/Session.hpp"

namespace ogrsh
{
	Provider::Provider(const Provider&)
	{
		OGRSH_FATAL("Not allowed to copy providers.");
		ogrsh::shims::real_exit(1);
	}

	Provider& Provider::operator= (const Provider&)
	{
		OGRSH_FATAL("Not allowed to copy providers.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	Provider::Provider(const std::string &providerName)
		: _providerName(providerName)
	{
	}

	Provider::~Provider()
	{
		// Clean up sessions
		std::map<std::string, Session*>::iterator iter;
		for (iter = _sessions.begin(); iter != _sessions.end(); iter++)
		{
			Session *session = iter->second;
			session->endSession();
			delete session;
		}
	}

	const std::string& Provider::getProviderName() const
	{
		return _providerName;
	}

	void Provider::addSession(const std::string &sessionName,
		const xercesc_2_8::DOMElement &domElement)
	{
		Session *session = getSession(sessionName);
		if (session != NULL)
		{
			OGRSH_FATAL("Session \"" << sessionName
				<< "\" already exists in provider \"" << _providerName
				<< "\".");
			ogrsh::shims::real_exit(1);
		}

		_sessions[sessionName] = createSession(sessionName, domElement);
	}

	Session* Provider::getSession(const std::string &sessionName)
	{
		std::map<std::string, Session*>::iterator iter = _sessions.find(
			sessionName);
		if (iter != _sessions.end())
			return iter->second;

		return NULL;
	}
}
