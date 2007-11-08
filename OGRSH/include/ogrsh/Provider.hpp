#ifndef __PROVIDER_HPP__
#define __PROVIDER_HPP__

#include <string>
#include <map>

#include "ogrsh/DynamicallyLoadedSymbol.hpp"
#include "ogrsh/Session.hpp"

#include "xercesc/dom/DOMElement.hpp"

namespace ogrsh
{
	class Provider : public DynamicallyLoadedSymbol
	{
		private:
			std::string _providerName;
			std::map<std::string, Session*> _sessions;

			Provider(const Provider&);
			Provider& operator= (const Provider&);

		protected:
			Provider(const std::string &providerName);

			virtual Session* createSession(const std::string &sessionName,
				const xercesc_2_8::DOMElement&) = 0;
				
		public:
			virtual ~Provider();

			const std::string& getProviderName() const;

			void addSession(const std::string &sessionName,
				const xercesc_2_8::DOMElement&);
			Session* getSession(const std::string &sessionName);
	};

	typedef Provider* (*createProviderFunction)(const std::string&);
}

#endif
