package edu.virginia.vcgr.genii.container.byteio;

import java.util.Calendar;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.mortbay.log.LogFactory;

import edu.virginia.vcgr.genii.container.common.DefaultGenesisIIAttributesPreFetcher;
import edu.virginia.vcgr.genii.container.resource.IResource;

public abstract class ByteIOAttributePreFetcher<Type extends IResource>
	extends DefaultGenesisIIAttributesPreFetcher<Type>
{
	static private Log _logger = LogFactory.getLog(
		ByteIOAttributePreFetcher.class);
	
	protected abstract QName getTransferMechanismAttributeName();
	protected abstract QName getSizeAttributeName();
	protected abstract QName getAccessTimeAttributeName();
	protected abstract QName getCreateTimeAttributeName();
	protected abstract QName getModificationTimeAttributeName();
	
	protected abstract Long getSize() throws Throwable;
	protected abstract URI[] getTransferMechanisms() throws Throwable;
	protected abstract Calendar getAccessTime() throws Throwable;
	protected abstract Calendar getModificationTime() throws Throwable;
	protected abstract Calendar getCreateTime() throws Throwable;
	
	protected ByteIOAttributePreFetcher(Type resource)
	{
		super(resource);
	}
	
	protected void fillInSizeAttribute(Collection<MessageElement> attributes)
	{
		try
		{
			Long size = getSize();
			if (size != null)
				attributes.add(new MessageElement(getSizeAttributeName(), 
					size));
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to pre-fetch size attribute.", cause);
		}
	}
	
	protected void fillInTransferMechanismsAttribute(
		Collection<MessageElement> attributes)
	{
		try
		{
			URI[] xferMechs = getTransferMechanisms();
			if (xferMechs != null)
			{
				QName attrName = getTransferMechanismAttributeName();
				
				for (URI uri : xferMechs)
				{
					attributes.add(new MessageElement(attrName, uri));
				}
			}
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to pre-fetch transfer mechanisms attribute.", 
				cause);
		}
	}
	
	protected void fillInCalendarAttribute(
		Collection<MessageElement> attributes, QName name, Calendar value)
	{
		if (value != null)
			attributes.add(new MessageElement(name, value));
	}
	
	protected void fillInAccessTimeAttribute(
		Collection<MessageElement> attributes)
	{
		try
		{
			fillInCalendarAttribute(attributes, 
				getAccessTimeAttributeName(), getAccessTime());
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to pre-fetch access time attribute.", cause);
		}
	}
	
	protected void fillInModificationTimeAttribute(
		Collection<MessageElement> attributes)
	{
		try
		{
			fillInCalendarAttribute(attributes, 
				getModificationTimeAttributeName(), getModificationTime());
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to pre-fetch modification time attribute.", 
				cause);
		}
	}
	
	protected void fillInCreateTimeAttribute(
		Collection<MessageElement> attributes)
	{
		try
		{
			fillInCalendarAttribute(attributes, 
				getCreateTimeAttributeName(), getCreateTime());
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to pre-fetch create time attribute.", cause);
		}
	}
	
	@Override
	protected void fillInAttributes(Collection<MessageElement> attributes)
	{
		super.fillInAttributes(attributes);
		
		fillInSizeAttribute(attributes);
		fillInTransferMechanismsAttribute(attributes);
		fillInAccessTimeAttribute(attributes);
		fillInCreateTimeAttribute(attributes);
		fillInModificationTimeAttribute(attributes);
	}
}