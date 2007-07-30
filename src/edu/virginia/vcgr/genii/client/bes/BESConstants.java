package edu.virginia.vcgr.genii.client.bes;

import javax.xml.namespace.QName;

public class BESConstants
{
	static public final String BES_NS = 
		"http://schemas.ggf.org/bes/2006/08/bes";
	
	static public QName DEPLOYER_EPR_ATTR =
		new QName(BES_NS, "deployer");
}