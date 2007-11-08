#ifndef __XML_PARSER_ERROR_HANDLER_HPP__
#define __XML_PARSER_ERROR_HANDLER_HPP__

#include "xercesc/sax/ErrorHandler.hpp"
#include "xercesc/sax/SAXParseException.hpp"

namespace ogrsh
{
	class XMLParserErrorHandler : public xercesc_2_8::ErrorHandler
	{
		private:
			bool _sawErrors;

		public:
			XMLParserErrorHandler();

			virtual void warning(const xercesc_2_8::SAXParseException &e);
			virtual void error(const xercesc_2_8::SAXParseException &e);
			virtual void fatalError(const xercesc_2_8::SAXParseException &e);
			virtual void resetErrors();

			bool sawErrors();
	};
}

#endif
