package edu.virginia.vcgr.genii.client.jsdl.personality;

import javax.xml.namespace.QName;

public interface GeniiRestrictionFacet extends PersonalityFacet
{
	static final public String GENII_RESTRICTION_NS =
		"http://vcgr.cs.virginia.edu/jsdl/genii";
	
	static final public QName OR_ELEMENT = new QName(
		GENII_RESTRICTION_NS, "or");
	static final public QName PROPERTY_ELEMENT = new QName(
		GENII_RESTRICTION_NS, "property");
	static final public String PROPERTY_NAME_ATTRIBUTE = "name";
	static final public String PROPERTY_VALUE_ATTRIBUTE = "value";
}