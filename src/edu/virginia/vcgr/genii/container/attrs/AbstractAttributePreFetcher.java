package edu.virginia.vcgr.genii.container.attrs;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.axis.message.MessageElement;

public abstract class AbstractAttributePreFetcher 
	implements AttributePreFetcher
{
	protected abstract void fillInAttributes(
		Collection<MessageElement> attributes);
	
	@Override
	public Collection<MessageElement> preFetch()
	{
		Collection<MessageElement> attributes =
			new ArrayList<MessageElement>();
		
		fillInAttributes(attributes);
		return attributes;
	}
}