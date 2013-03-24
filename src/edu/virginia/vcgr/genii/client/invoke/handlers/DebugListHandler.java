package edu.virginia.vcgr.genii.client.invoke.handlers;

import org.ggf.rns.RNSPortType;

import edu.virginia.vcgr.genii.client.invoke.InvocationContext;
import edu.virginia.vcgr.genii.client.invoke.PipelineProcessor;

public class DebugListHandler
{
	@PipelineProcessor(portType = RNSPortType.class)
	public int list(InvocationContext ctxt, int listRequest) throws Throwable
	{
		System.err.println("DEBUG:  list called.");
		return 7;
	}
}