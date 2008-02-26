package edu.virginia.vcgr.genii.client.invoke.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.common.rattrs.GetAttributesDocumentResponse;
import edu.virginia.vcgr.genii.common.rattrs.GetAttributesResponse;

public class CachedAttributeData
{
	private HashMap<QName, Collection<MessageElement>> _attrs =
		new HashMap<QName, Collection<MessageElement>>();
	private MessageElement []_all;
	private boolean _isFull;
	
	private CachedAttributeData(MessageElement []elements, boolean isFull)
	{
		_isFull = isFull;
		_all = elements;
		for (MessageElement element : elements)
		{
			QName attrName = element.getQName();
			Collection<MessageElement> col = _attrs.get(attrName);
			if (col == null)
				_attrs.put(attrName, col = new ArrayList<MessageElement>());
			col.add(element);
		}
	}
	
	public CachedAttributeData(GetAttributesResponse resp)
	{
		this(resp.get_any(), false);
	}
	
	public CachedAttributeData(GetAttributesDocumentResponse resp)
	{
		this(resp.get_any(), true);
	}
		
	@SuppressWarnings("unchecked")
	public CachedAttributeData(MessageElement uberDoc)
	{
		this((MessageElement[])uberDoc.getChildren().toArray(
			new MessageElement[0]), true);
	}
	
	public CachedAttributeData(Collection<MessageElement> attrs)
	{
		this(attrs.toArray(new MessageElement[0]), false);
	}
	
	public CachedAttributeData(MessageElement []any)
	{
		this(any, false);
	}
	
	public boolean isFull()
	{
		return _isFull;
	}
	
	public MessageElement[] getAll()
	{
		return _all;
	}
	
	public Collection<MessageElement> getAttributes(QName attrName)
	{
		return _attrs.get(attrName);
	}
}