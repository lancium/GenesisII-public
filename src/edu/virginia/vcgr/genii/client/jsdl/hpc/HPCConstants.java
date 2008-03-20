package edu.virginia.vcgr.genii.client.jsdl.hpc;

import javax.xml.namespace.QName;

public interface HPCConstants
{
	static public final String HPC_NS = 
		"http://schemas.ggf.org/jsdl/2006/07/jsdl-hpcpa";
	static public final String HPC_APPLICATION_NAME =
		"HPCProfileApplication";
	
	static public final QName HPC_APPLICATION_QNAME =
		new QName(HPC_NS, HPC_APPLICATION_NAME);
}