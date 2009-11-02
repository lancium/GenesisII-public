package edu.virginia.vcgr.genii.client.jsdl.sweep.eval;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepException;

public interface SweepTarget
{
	public void replace(Object value) throws SweepException;
}