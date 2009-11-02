package edu.virginia.vcgr.genii.client.jsdl.sweep.eval;

import org.w3c.dom.Node;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepException;

public interface SweepTargetIdentifier
{
	public SweepTarget identify(Node context) throws SweepException;
}