package edu.virginia.vcgr.genii.client.jsdl.sweep;

import javax.xml.namespace.QName;

public interface SweepConstants
{
	static final public String SWEEP_NS =
		"http://schemas.ogf.org/jsdl/2009/03/sweep";
	static final public String SWEEP_FUNC_NS =
		"http://schemas.ogf.org/jsdl/2009/03/sweep/functions";

	static final public String SWEEP_NAME =
		"Sweep";
	
	static final public QName SWEEP_QNAME = new QName(
		SWEEP_NS, SWEEP_NAME);
}