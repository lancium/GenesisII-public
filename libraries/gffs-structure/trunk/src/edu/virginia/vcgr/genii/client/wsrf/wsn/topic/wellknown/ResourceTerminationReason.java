package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import org.w3c.dom.Element;

public class ResourceTerminationReason implements Serializable
{
	static final long serialVersionUID = 0L;

	private Collection<Element> _terminationReason = new LinkedList<Element>();

	final public Collection<Element> terminationReason()
	{
		return _terminationReason;
	}
}