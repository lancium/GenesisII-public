package edu.virginia.vcgr.genii.ui.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.axis.message.MessageElement;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.ser.AnyHelper;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;

public class XMLPrettyPrinter
{
	private XMLFormatHandler _handler;
	
	static private String formDocument(MessageElement me)
		throws IOException
	{
		StringWriter writer = null;
		
		try
		{
			writer = new StringWriter();
			AnyHelper.write(writer, me);
			return writer.toString();
		}
		catch (IOException ioe)
		{
			throw ioe;
		}
		catch (Exception e)
		{
			throw new IOException("Unable to string-ify XML document.", e);
		}
		finally
		{
			StreamUtils.close(writer);
		}
	}
	
	static private String formDocument(QName name, Object obj)
		throws IOException
	{
		StringWriter writer = null;
		
		try
		{
			writer = new StringWriter();
			ObjectSerializer.serialize(writer, obj, name);
			return writer.toString();
		}
		finally
		{
			StreamUtils.close(writer);
		}
	}
	
	private void appendAttributes(NamespaceContext nsContext,
		Iterator<?> attrs, String tabs) throws IOException
	{
		while (attrs.hasNext())
		{
			Attribute attribute = (Attribute)attrs.next();
			QName name = attribute.getName();
			String formattedName;
			String nsURI = name.getNamespaceURI();
			if (nsURI == null || nsURI.length() == 0)
				formattedName = name.getLocalPart();
			else
				formattedName = String.format("%s:%s",
					nsContext.getPrefix(nsURI),
					name.getLocalPart());
			
			_handler.appendText(String.format("\n%s", tabs));
			_handler.startAttribute();
			_handler.appendText(String.format("%s=\"%s\"",
				formattedName, attribute.getValue()));
			_handler.endAttribute();
		}
	}
	
	private void appendNamespaces(NamespaceContext nsContext,
		Iterator<?> namespaces, String tabs) throws IOException
	{
		while (namespaces.hasNext())
		{
			Namespace ns = (Namespace)namespaces.next();
			_handler.appendText(String.format("\n%s", tabs));
			_handler.startAttribute();
			_handler.appendText(String.format("xmlns:%s=\"%s\"",
				ns.getPrefix(), ns.getNamespaceURI()));
			_handler.endAttribute();
		}
	}
	
	private void appendElement(XMLEventFilter reader, StartElement event, 
		String tabs) throws IOException, XMLStreamException
	{
		QName name = event.getName();
		String formattedName = String.format("%s:%s",
			event.getNamespaceContext().getPrefix(name.getNamespaceURI()),
			name.getLocalPart());
		
		// Write the start element
		_handler.appendText(tabs);
		_handler.startElement();
		_handler.appendText(String.format("<%s", formattedName));
		appendAttributes(event.getNamespaceContext(), event.getAttributes(),
			tabs + "    ");
		appendNamespaces(event.getNamespaceContext(), event.getNamespaces(),
			tabs + "    ");
		
		// Check if it is an empty element
		XMLEvent next = reader.next();
		if (next.isEndElement())
		{
			_handler.appendText("/>");
			_handler.endElement();
			_handler.appendText("\n");
			return;
		}
		
		// Not an empty element
		_handler.appendText(">");
		_handler.endElement();
		while (next.isCharacters())
		{
			_handler.appendText(next.asCharacters().getData().trim());
			next = reader.next();
		}
		
		if (next.isEndElement())
		{
			_handler.startElement();
			_handler.appendText(String.format("</%s>", formattedName));
			_handler.endElement();
			_handler.appendText("\n");
		} else
		{
			_handler.appendText("\n");
			while (next.isStartElement())
			{
				appendElement(reader, next.asStartElement(), tabs + "  ");
				next = reader.next();
			}
			if (!next.isEndElement())
				throw new IOException("Bad XML Document.");
			else
			{
				_handler.appendText(tabs);
				_handler.startElement();
				_handler.appendText(String.format("</%s>", formattedName));
				_handler.endElement();
				_handler.appendText("\n");
			}
		}
	}
	
	private void formatDocument(XMLEventFilter reader, String tabs)
		throws IOException
	{
		try
		{
			XMLEvent event = reader.next();
			if (event == null)
				return;
			if (event.isStartElement())
				appendElement(reader, event.asStartElement(), tabs);
			else
				throw new IOException("Invalid XML document found.");
		}
		catch (XMLStreamException xse)
		{
			throw new IOException("Unable to parse XML document.", xse);
		}
	}
	
	public XMLPrettyPrinter(XMLFormatHandler handler)
	{
		_handler = handler;
	}
	
	public void formatDocument(XMLEventReader reader)
		throws IOException
	{
		try
		{
			formatDocument(new XMLEventFilter(reader), "");
		} 
		catch (XMLStreamException e)
		{
			throw new IOException(
				"Unable to create filter for XML event stream.", e);
		}
	}
	
	public void formatDocument(QName objectName, Object object)
		throws IOException
	{
		XMLEventReader reader = null;
		StringReader sReader = null;
		
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
		
		try
		{
			sReader = new StringReader(formDocument(objectName, object));
			reader = factory.createXMLEventReader(sReader);
			formatDocument(reader);
		}
		catch (XMLStreamException e)
		{
			throw new IOException(
				"Unable to create XML event reader for document.", e);
		}
		finally
		{
			if (reader != null)
				try { reader.close(); } catch (Throwable cause) {}
			StreamUtils.close(sReader);
		}
	}
	
	public void formatDocument(MessageElement me) throws IOException
	{
		XMLEventReader reader = null;
		StringReader sReader = null;
		
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
		
		try
		{
			sReader = new StringReader(formDocument(me));
			reader = factory.createXMLEventReader(sReader);
			formatDocument(reader);
		}
		catch (XMLStreamException e)
		{
			throw new IOException(
				"Unable to create XML event reader for document.", e);
		}
		finally
		{
			if (reader != null)
				try { reader.close(); } catch (Throwable cause) {}
			StreamUtils.close(sReader);
		}
	}
}