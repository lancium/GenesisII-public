#include <dlfcn.h>

#include "xercesc/dom/DOMDocument.hpp"
#include "xercesc/dom/DOMElement.hpp"
#include "xercesc/dom/DOMNode.hpp"
#include "xercesc/dom/DOMNodeList.hpp"
#include "xercesc/dom/DOMException.hpp"
#include "xercesc/dom/DOMImplementation.hpp"

#include "xercesc/util/OutOfMemoryException.hpp"
#include "xercesc/util/PlatformUtils.hpp"

#include "xercesc/parsers/XercesDOMParser.hpp"

#include "ogrsh/ConfigurationFileParser.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/XMLParserErrorHandler.hpp"
#include "ogrsh/XMLUtilities.hpp"

using namespace xercesc_2_8;

namespace ogrsh
{
	/* Namespaces */
	static const char *_OGRSH_NS_URI = "http://vcgr.cs.virginia.edu/ogrsh";

	/* Element Local Names */
	static const char *_FS_PROVIDER_NAME = "fs-provider";
	static const char *_SESSION_NAME = "session";
	static const char *_MOUNT_NAME = "mount";
	static const char *_GLOBAL_CONFIG_NAME = "global-config";
	static const char *_HOME_DIR_NAME = "home-dir";

	/* Attribute Names */
	static const char *_FS_PROVIDER_NAME_ATTR_NAME = "name";
	static const char *_PROVIDER_CREATOR_ATTR_NAME = "provider-creator";
	static const char *_PROVIDER_LIBRARY_ATTR_NAME = "provider-library";
	static const char *_SESSION_NAME_ATTR_NAME = "name";
	static const char *_MOUNT_LOCATION_ATTR_NAME = "location";
	static const char *_MOUNT_PROVIDER_ATTR_NAME = "provider";
	static const char *_MOUNT_SESSION_ATTR_NAME = "session";

	static XercesDOMParser* createParser();

	/**
	  * Returns true if there is no error, false otherwise.
	  */
	static bool parse(XercesDOMParser *, const std::string &configFile);
	static Provider* handleProvider(DOMElement *node,
		ConfigurationFileParseHandler *handler);
	static void handleMount(DOMElement *el,
		ConfigurationFileParseHandler *handler);
	static void handleSession(Provider *provider, DOMElement *el);
	static Provider* createProvider(const std::string &providerName,
		const std::string &libraryName, const std::string &creatorSymbol);
	static void handleGlobalConfig(DOMElement *el,
		ConfigurationFileParseHandler *handler);

	void parseConfigurationFile(const std::string &configFile,
		ConfigurationFileParseHandler *handler)
	{
		// Initialize the XML4C2 system
		try
		{
			XMLPlatformUtils::Initialize();
		}
		catch (const XMLException &e)
		{
			OGRSH_FATAL("Error during Xerces-c initialization.\n"
				<< "Exception message:  " << convert(e.getMessage()));
			ogrsh::shims::real_exit(1);
		}

		// Create our parser, then attach an error handler to the parser.
		// The parser will call back to methods of the ErrorHandler if it
		// descovers errors during the course of parsing the XML document.
		XercesDOMParser *parser = createParser();

		if (parse(parser, configFile))
		{
			DOMDocument *doc = parser->getDocument();
			DOMElement *rootElement = doc->getDocumentElement();
			DOMNodeList *children = rootElement->getChildNodes();

			XMLSize_t length = children->getLength();
			for (XMLSize_t lcv = 0; lcv < length; lcv++)
			{
				DOMNode *node = children->item(lcv);
				if (node->getNodeType() == DOMNode::ELEMENT_NODE)
				{
					DOMElement *el = (DOMElement*)node;
					std::string namespaceURI = convert(el->getNamespaceURI());
					std::string localName = convert(el->getLocalName());

					if (namespaceURI == _OGRSH_NS_URI)
					{
						if (localName == _FS_PROVIDER_NAME)
						{
							// Its a provider element
							Provider *provider = handleProvider(el, handler);
							handler->addProvider(provider);
						} else if (localName == _MOUNT_NAME)
						{
							handleMount(el, handler);
						} else if (localName == _GLOBAL_CONFIG_NAME)
						{
							handleGlobalConfig(el, handler);
						} else
						{
							OGRSH_FATAL("Unknown configuration element {"
								<< namespaceURI << "}" << localName);
							ogrsh::shims::real_exit(1);
						}
					} else
					{
						OGRSH_FATAL("Unknown configuration element {"
							<< namespaceURI << "}" << localName);
						ogrsh::shims::real_exit(1);
					}
				}
			}
		} else
		{
			ogrsh::shims::real_exit(1);
		}

		delete parser;
		XMLPlatformUtils::Terminate();
	}

	XercesDOMParser* createParser()
	{
		XercesDOMParser *parser = new XercesDOMParser;

		parser->setValidationScheme(XercesDOMParser::Val_Auto);
		parser->setDoNamespaces(true);
		parser->setDoSchema(false);
		parser->setValidationSchemaFullChecking(false);
		parser->setCreateEntityReferenceNodes(false);

		return parser;
	}

	/**
	  * Returns true if there is no error, false otherwise.
	  */
	bool parse(XercesDOMParser *parser, const std::string &configFile)
	{
		XMLParserErrorHandler *errReporter = new XMLParserErrorHandler;
		parser->setErrorHandler(errReporter);

		try
		{
			parser->parse(configFile.c_str());
			bool ret = !(errReporter->sawErrors());
			delete errReporter;
			return ret;
		}
		catch (const OutOfMemoryException&)
		{
			OGRSH_FATAL("XML parser ran out of memory while parsing "
				<< configFile);
		}
		catch (const XMLException &e)
		{
			OGRSH_FATAL("An error occured while parsing the config file \""
				<< configFile << "\".\nDetails:  " << convert(e.getMessage()));
		}
		catch (const DOMException &de)
		{
			const unsigned int maxChars = 2047;
			XMLCh *errText = new XMLCh[maxChars];

			if (DOMImplementation::loadDOMExceptionMsg(
				de.code, errText, maxChars))
			{
				OGRSH_FATAL("XML Error while parsing \"" << configFile
					<< "\"\nDetails:\n\tDOM Exception Code:  " << de.code
					<< "\n\tMessage:  " << convert(errText));
			} else
			{
				OGRSH_FATAL("XML Error while parsing \"" << configFile
					<< "\"\nDetails:\n\tDOM Exception Code:  " << de.code);
			}

			delete []errText;
		}
		catch (...)
		{
			OGRSH_FATAL("An unknown error occured during XML parsing "
				"for config file \"" << configFile << "\".");
		}

		delete errReporter;
		return false;
	}

	Provider* handleProvider(DOMElement *node,
		ConfigurationFileParseHandler *handler)
	{
		std::string name = getAttribute(node, _FS_PROVIDER_NAME_ATTR_NAME);
		std::string creator = getAttribute(node, _PROVIDER_CREATOR_ATTR_NAME);

		if (name.length() == 0)
		{
			OGRSH_FATAL("Provider doesn't have a name in configuration file.");
			ogrsh::shims::real_exit(1);
		}

		if (creator.length() == 0)
		{
			OGRSH_FATAL("Provider \"" << name
				<< "\" doesn't have an associated creator.");
			ogrsh::shims::real_exit(1);
		}

		std::string providerLibrary = getAttribute(
			node, _PROVIDER_LIBRARY_ATTR_NAME);
		Provider *provider = createProvider(name, providerLibrary, creator);

		DOMNodeList *children = node->getChildNodes();

		XMLSize_t length = children->getLength();
		for (XMLSize_t lcv = 0; lcv < length; lcv++)
		{
			DOMNode *node = children->item(lcv);
			if (node->getNodeType() == DOMNode::ELEMENT_NODE)
			{
				DOMElement *el = (DOMElement*)node;
				std::string namespaceURI = convert(el->getNamespaceURI());
				std::string localName = convert(el->getLocalName());

				if (namespaceURI == _OGRSH_NS_URI)
				{
					if (localName == _SESSION_NAME)
					{
						handleSession(provider, el);
					} else
					{
						OGRSH_FATAL("Unknown configuration element {"
							<< namespaceURI << "}" << localName);
						ogrsh::shims::real_exit(1);
					}
				} else
				{
					OGRSH_FATAL("Unknown configuration element {"
						<< namespaceURI << "}" << localName);
					ogrsh::shims::real_exit(1);
				}
			}
		}

		return provider;
	}

	void handleMount(DOMElement *el, ConfigurationFileParseHandler *handler)
	{
		std::string location = getAttribute(el, _MOUNT_LOCATION_ATTR_NAME);
		std::string providerName = getAttribute(el,
			_MOUNT_PROVIDER_ATTR_NAME);
		std::string sessionName = getAttribute(el, _MOUNT_SESSION_ATTR_NAME);

		if (location.length() == 0)
		{
			OGRSH_FATAL("Mount doesn't have a location in configuration file.");
			ogrsh::shims::real_exit(1);
		}

		if (providerName.length() == 0)
		{
			OGRSH_FATAL("Mount \"" << location
				<< "\" doesn't have an associated provider.");
			ogrsh::shims::real_exit(1);
		}

		if (sessionName.length() == 0)
		{
			OGRSH_FATAL("Mount \"" << location
				<< "\" with provider \"" << providerName
				<< "\" doesn't have an associated session.");
			ogrsh::shims::real_exit(1);
		}

		handler->addMount(providerName, sessionName, location, *el);
	}

	Provider* createProvider(const std::string &providerName,
		const std::string &libraryName, const std::string &creatorSymbol)
	{
		void *handle;

		if (libraryName.length() > 0)
		{
			handle = dlopen(libraryName.c_str(), RTLD_NOW);
			if (handle == NULL)
			{
				OGRSH_FATAL("Error opening provider library \""
					<< libraryName << "\" for provider \""
					<< providerName << "\".\nCause:  " << dlerror());
				ogrsh::shims::real_exit(1);
			}
		} else
		{
			handle = dlopen(NULL, RTLD_NOW);
		}

		createProviderFunction creator =
			(createProviderFunction)dlsym(handle, creatorSymbol.c_str());
		if (creator == NULL)
		{
			OGRSH_FATAL("Unable to find creator function \""
				<< creatorSymbol << "\".");
			ogrsh::shims::real_exit(1);
		}

		Provider *provider = creator(providerName);
		provider->setDLHandle(handle);

		return provider;
	}
	
	void handleSession(Provider *provider, DOMElement *el)
	{
		std::string name = getAttribute(el, _SESSION_NAME_ATTR_NAME);

		if (name.length() == 0)
		{
			OGRSH_FATAL("Session inside of provider \"" <<
				provider->getProviderName()
				<< "\" doesn't have a name in configuration file.");
			ogrsh::shims::real_exit(1);
		}

		provider->addSession(name, *el);
	}

	void handleGlobalConfig(DOMElement *el,
		ConfigurationFileParseHandler *handler)
	{
		DOMNodeList *children = el->getChildNodes();

		XMLSize_t length = children->getLength();
		for (XMLSize_t lcv = 0; lcv < length; lcv++)
		{
			DOMNode *node = children->item(lcv);
			if (node->getNodeType() == DOMNode::ELEMENT_NODE)
			{
				DOMElement *el2 = (DOMElement*)node;
				std::string namespaceURI = convert(el2->getNamespaceURI());
				std::string localName = convert(el2->getLocalName());

				if (namespaceURI == _OGRSH_NS_URI)
				{
					if (localName == _HOME_DIR_NAME)
					{
						std::string homeDir = convert(el2->getTextContent());
						handler->setHomeDirectory(homeDir);
					} else
					{
						OGRSH_FATAL("Unrecognized element {"
							<< namespaceURI << "}" << localName
							<< " in global configuration.");
						ogrsh::shims::real_exit(1);
					}
				} else
				{
					OGRSH_FATAL("Unrecognized element {"
						<< namespaceURI << "}" << localName
						<< " in global configuration.");
					ogrsh::shims::real_exit(1);
				}
			}
		}
	}
}
