package edu.virginia.vcgr.genii.ui.xml;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.axis.message.MessageElement;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.ser.AnyHelper;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;


public class DefaultXMLTreeSource implements XMLTreeSource
{
	static final private QName TEMP_URI_NAME = new QName(
			"http://tempuri.org", "temp");
		
	private XMLEventReader _reader = null;
	private QName _name = null;
	private Object _object = null;
	private MessageElement _me = null;
	
	private XMLEventReader createReader(String string) 
		throws XMLStreamException
	{
		StringReader sReader = null;
		
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE,
			Boolean.TRUE);
		
		sReader = new StringReader(string);
		return factory.createXMLEventReader(sReader);
	}
	
	public DefaultXMLTreeSource(XMLEventReader reader)
	{
		_reader = reader;
	}
	
	public DefaultXMLTreeSource(Object obj)
	{
		_name = TEMP_URI_NAME;
		_object = obj;
	}
	
	public DefaultXMLTreeSource(MessageElement me)
	{
		_me = me;
	}
	
	@Override
	public XMLEventReader getReader() throws Throwable
	{
		if (_reader == null)
		{
			StringWriter sWriter = null;
			
			try
			{
				sWriter = new StringWriter();
				if (_me != null)
					AnyHelper.write(sWriter, _me);
				else
					ObjectSerializer.serialize(sWriter, _object, _name);

				sWriter.flush();
				_reader  = createReader(sWriter.toString());
			}
			finally
			{
				StreamUtils.close(sWriter);
			}
		}
		
		return _reader;
	}
}