#ifndef __CONFIGURATION_FILE_PARSE_HANDLER_HPP__
#define __CONFIGURATION_FILE_PARSE_HANDLER_HPP__

#include <string>

#include "ogrsh/Provider.hpp"

#include "xercesc/dom/DOMElement.hpp"

namespace ogrsh
{
	class ConfigurationFileParseHandler
	{
		public:
			virtual ~ConfigurationFileParseHandler();

			virtual void setHomeDirectory(
				const std::string &homeDirectory) = 0;
			virtual void addProvider(Provider*) = 0;
			virtual void addMount(const std::string &providerName,
				const std::string &sessionName,
				const std::string &mountLocation,
				const xercesc_2_8::DOMElement &configNode) = 0;
	};

	inline ConfigurationFileParseHandler::~ConfigurationFileParseHandler()
	{
	}
}

#endif
