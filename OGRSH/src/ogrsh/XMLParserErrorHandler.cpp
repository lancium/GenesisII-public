#include "ogrsh/Logging.hpp"
#include "ogrsh/XMLUtilities.hpp"
#include "ogrsh/XMLParserErrorHandler.hpp"

using namespace xercesc_2_8;

namespace ogrsh
{
	XMLParserErrorHandler::XMLParserErrorHandler()
	{
		_sawErrors = false;
	}

	void XMLParserErrorHandler::warning(const SAXParseException &e)
	{
		OGRSH_WARN("XML Parse Warning on line "
			<< (unsigned int)e.getLineNumber() << " -- "
			<< convert(e.getMessage()));
	}

	void XMLParserErrorHandler::error(const SAXParseException &e)
	{
		_sawErrors = true;
		OGRSH_ERROR("XML Parse Error on line "
			<< (unsigned int)e.getLineNumber() << " -- "
			<< convert(e.getMessage()));
	}

	void XMLParserErrorHandler::fatalError(const SAXParseException &e)
	{
		_sawErrors = true;
		OGRSH_FATAL("XML Fatal Parse Error on line "
			<< (unsigned int)e.getLineNumber() << " -- "
			<< convert(e.getMessage()));
	}

	void XMLParserErrorHandler::resetErrors()
	{
		_sawErrors = false;
	}

	bool XMLParserErrorHandler::sawErrors()
	{
		return _sawErrors;
	}
}
