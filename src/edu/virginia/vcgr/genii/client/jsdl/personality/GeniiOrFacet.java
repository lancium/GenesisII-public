package edu.virginia.vcgr.genii.client.jsdl.personality;

import javax.xml.namespace.QName;

public interface GeniiOrFacet extends GeniiRestrictionFacet
{
	static final public QName OR_ELEMENT = new QName(
		GENII_RESTRICTION_NS, "or");
}