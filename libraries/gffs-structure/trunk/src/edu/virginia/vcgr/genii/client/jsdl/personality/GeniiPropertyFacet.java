package edu.virginia.vcgr.genii.client.jsdl.personality;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface GeniiPropertyFacet extends GeniiRestrictionFacet
{
	static final public QName PROPERTY_ELEMENT = new QName(GENII_RESTRICTION_NS, "property");
	static final public String PROPERTY_NAME_ATTRIBUTE = "name";
	static final public String PROPERTY_VALUE_ATTRIBUTE = "value";

	public void consumeProperty(Object currentUnderstanding, String propertyName, String propertyValue) throws JSDLException;
}