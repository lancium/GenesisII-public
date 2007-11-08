#include <string>
#include <map>

#include "ogrsh/DynamicallyLoadedSymbol.hpp"
#include "ogrsh/Provider.hpp"
#include "ogrsh/Session.hpp"

#include "providers/localfs/LocalFSProvider.hpp"
#include "providers/localfs/LocalFSSession.hpp"

#include "xercesc/dom/DOMElement.hpp"

namespace ogrsh
{
	namespace localfs
	{
		ogrsh::Session* LocalFSProvider::createSession(
			const std::string &sessionName,
			const xercesc_2_8::DOMElement&)
		{
			return new LocalFSSession(sessionName);
		}

		LocalFSProvider::LocalFSProvider(const std::string &providerName)
			: ogrsh::Provider(providerName)
		{
		}

		LocalFSProvider::~LocalFSProvider()
		{
		}

		extern "C" {
			ogrsh::Provider* createLocalOGRSHProvider(
				const std::string &providerName)
			{
				return new LocalFSProvider(providerName);
			}
		}
	}
}
