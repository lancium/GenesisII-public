package edu.virginia.vcgr.genii.client.appdesc;

import javax.xml.namespace.QName;

public class ApplicationDescriptionConstants
{
	static final public String APPLICATION_DESCRIPTION_NS = "http://vcgr.cs.virginia.edu/genii/application-description";

	static public QName APPLICATION_DESCRIPTION_ATTR_QNAME = new QName(APPLICATION_DESCRIPTION_NS, "application-description");
	static public QName APPLICATION_VERSION_ATTR_QNAME = new QName(APPLICATION_DESCRIPTION_NS, "application-version");
	static public QName SUPPORT_DOCUMENT_ATTR_QNAME = new QName(APPLICATION_DESCRIPTION_NS, "support-document");
}