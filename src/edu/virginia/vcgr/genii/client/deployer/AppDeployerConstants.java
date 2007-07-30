package edu.virginia.vcgr.genii.client.deployer;

import javax.xml.namespace.QName;

public class AppDeployerConstants
{
	static final public String APPLICATION_DEPLOYER_NS =
		"http://vcgr.cs.virginia.edu/genii/application-deployer";
	
	static public QName DEPLOYER_SUPPORT_DOCUMENT_ATTR_QNAME =
		new QName(APPLICATION_DEPLOYER_NS, "support-document");
}
