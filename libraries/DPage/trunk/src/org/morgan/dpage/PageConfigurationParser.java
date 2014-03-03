package org.morgan.dpage;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

class PageConfigurationParser extends DefaultHandler {
	static public Map<String, PageContextDescription> parse(InputStream in)
			throws ParserConfigurationException, SAXException, IOException {
		PageConfigurationParser handler = new PageConfigurationParser();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(false);
		SAXParser parser = factory.newSAXParser();
		parser.parse(in, handler);
		return handler._pageDescriptions;
	}

	private Locator _locator = null;
	private Map<String, PageContextDescription> _pageDescriptions = new HashMap<String, PageContextDescription>();

	private PageConfigurationParser() {
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		_locator = locator;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("page")) {
			String context = attributes.getValue("context");
			String resourceBase = attributes.getValue("resource-base");
			String injectionHandlerFactoryClassName = attributes
					.getValue("injection-handler-factory");
			if (injectionHandlerFactoryClassName == null)
				injectionHandlerFactoryClassName = NullValueInjectionHandlerFactory.class
						.getName();

			if (context == null)
				throw new SAXParseException(
						"Page element missing required \"context\" attribute.",
						_locator);
			if (resourceBase == null)
				throw new SAXParseException(
						"Page element missing required \"resourceBase\" attribute.",
						_locator);

			try {
				_pageDescriptions.put(context,
						new PageContextDescription(context, resourceBase,
								injectionHandlerFactoryClassName));
			} catch (ClassNotFoundException e) {
				throw new SAXParseException(String.format(
						"Unable to create injection handler factory \"%s\".",
						injectionHandlerFactoryClassName), _locator, e);
			} catch (InstantiationException e) {
				throw new SAXParseException(String.format(
						"Unable to create injection handler factory \"%s\".",
						injectionHandlerFactoryClassName), _locator, e);
			} catch (IllegalAccessException e) {
				throw new SAXParseException(String.format(
						"Unable to create injection handler factory \"%s\".",
						injectionHandlerFactoryClassName), _locator, e);
			}
		}
	}
}