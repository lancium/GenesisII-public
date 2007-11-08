#ifndef __GENIIFS_PROVIDER_HPP__
#define __GENIIFS_PROVIDER_HPP__

#include <string>
#include <map>

#include "ogrsh/DynamicallyLoadedSymbol.hpp"
#include "ogrsh/Provider.hpp"
#include "ogrsh/Session.hpp"

#include "xercesc/dom/DOMElement.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		class GeniiFSProvider : public ogrsh::Provider
		{
			protected:
				virtual ogrsh::Session* createSession(
					const std::string &sessionName,
					const xercesc_2_8::DOMElement&);

			public:
				GeniiFSProvider(const std::string &providerName);
				virtual ~GeniiFSProvider();
		};
	}
}

#endif
