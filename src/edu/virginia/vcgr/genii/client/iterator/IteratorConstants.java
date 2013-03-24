package edu.virginia.vcgr.genii.client.iterator;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.PortType;

public interface IteratorConstants
{
	static public final String ITERATOR_FACTORY_NS = "http://vcgr.cs.virginia.edu/genii/iterator-factory";

	static public final String ITERATOR_NS = "http://schemas.ogf.org/ws-iterator/2008/06/iterator";
	static public final String ITERATOR_PORT_TYPE_NAME = "WSIteratorPortType";

	static public final PortType ITERATOR_PORT_TYPE = PortType.get(new QName(ITERATOR_NS, ITERATOR_PORT_TYPE_NAME));
}