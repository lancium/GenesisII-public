#ifndef __CONFIGURATION_FILE_PARSER_HPP__
#define __CONFIGURATION_FILE_PARSER_HPP__

#include <string>

#include "ogrsh/ConfigurationFileParseHandler.hpp"

namespace ogrsh
{
	void parseConfigurationFile(const std::string &configFile,
		ConfigurationFileParseHandler *handler);
}

#endif
