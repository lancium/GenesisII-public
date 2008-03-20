package edu.virginia.vcgr.genii.client.jsdl.posix;

import javax.xml.namespace.QName;

public interface JSDLPosixConstants
{
	static public final String JSDL_POSIX_NS =
		"http://schemas.ggf.org/jsdl/2005/11/jsdl-posix";
	static public final String JSDL_POSIX_APPLICATION_NAME = 
		"POSIXApplication";
	
	static public final QName JSDL_POSIX_APPLICATION_QNAME =
		new QName(JSDL_POSIX_NS, JSDL_POSIX_APPLICATION_NAME);
}