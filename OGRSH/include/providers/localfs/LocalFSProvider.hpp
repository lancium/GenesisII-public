#ifndef __LOCALFS_PROVIDER_HPP__
#define __LOCALFS_PROVIDER_HPP__

#include <string>
#include <map>

#include "ogrsh/DynamicallyLoadedSymbol.hpp"
#include "ogrsh/Provider.hpp"
#include "ogrsh/Session.hpp"

#include "xercesc/dom/DOMElement.hpp"

namespace ogrsh
{
	namespace localfs
	{
		class LocalFSProvider : public ogrsh::Provider
		{
			protected:
				virtual ogrsh::Session* createSession(
					const std::string &sessionName,
					const xercesc_2_8::DOMElement&);

			public:
				LocalFSProvider(const std::string &providerName);
				virtual ~LocalFSProvider();
		};
	}
}

#endif
