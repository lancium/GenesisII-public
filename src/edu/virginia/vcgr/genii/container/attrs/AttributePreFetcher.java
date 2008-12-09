package edu.virginia.vcgr.genii.container.attrs;

import java.util.Collection;

import org.apache.axis.message.MessageElement;

public interface AttributePreFetcher
{
	public Collection<MessageElement> preFetch();
}