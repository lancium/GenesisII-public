package edu.virginia.vcgr.genii.client.jsdl.sweep;

import org.w3c.dom.Node;

public interface SweepListener
{
	public void emitSweepInstance(Node document) throws SweepException;
}