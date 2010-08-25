package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;

abstract class AbstractTopicQueryExpression 
	implements TopicQueryExpression, Serializable
{
	static final long serialVersionUID = 0L;
	
	private TopicQueryDialects _dialect;
	
	protected abstract String toString(NamespaceFactory prefixFactory);
	
	protected AbstractTopicQueryExpression(TopicQueryDialects dialect)
	{
		_dialect = dialect;
	}
	
	@Override
	final public TopicQueryDialects dialect()
	{
		return _dialect;
	}
	
	@Override
	final public MessageElement toTopicExpressionElement(QName elementName,
		String nsPrefixPattern) throws SOAPException
	{
		MessageElement ret = new MessageElement(elementName);
		ret.setAttribute("Dialect", _dialect.dialect().toString());
		NamespaceFactoryImpl factory = new NamespaceFactoryImpl(
			nsPrefixPattern);
		ret.addTextNode(toString(factory));
		
		for (Map.Entry<String, String> entry : 
			factory._uri2PrefixMap.entrySet())
		{
			ret.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				String.format("%s:%s", 
					XMLConstants.XMLNS_ATTRIBUTE, entry.getValue()),
				entry.getKey());
		}
		
		return ret;
	}
	
	static private class NamespaceFactoryImpl implements NamespaceFactory
	{
		private String _nsPrefixPattern;
		private int _count = 1;
		private Map<String, String> _uri2PrefixMap = 
			new HashMap<String, String>();
		
		private NamespaceFactoryImpl(String prefixPattern)
		{
			_nsPrefixPattern = prefixPattern;
		}
		
		@Override
		final public String getPrefix(String namespaceURI)
		{
			String prefix = _uri2PrefixMap.get(namespaceURI);
			if (prefix == null)
				_uri2PrefixMap.put(namespaceURI, 
					prefix = String.format(_nsPrefixPattern, _count++));
			
			return prefix;
		}
	}
}