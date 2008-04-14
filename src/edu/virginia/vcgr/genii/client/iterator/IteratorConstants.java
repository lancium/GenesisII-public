package edu.virginia.vcgr.genii.client.iterator;

import javax.xml.namespace.QName;

public interface IteratorConstants
{
	static public final String ITERATOR_NS = 
		"http://vcgr.cs.virginia.edu/genii/iterator";
	static public final String ITERATOR_PORT_TYPE_NAME = "IteratorPortType";
	
	static public final QName ITERATOR_PORT_TYPE_QNAME =
		new QName(ITERATOR_NS, ITERATOR_PORT_TYPE_NAME);
}