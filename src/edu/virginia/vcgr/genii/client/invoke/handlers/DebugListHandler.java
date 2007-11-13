package edu.virginia.vcgr.genii.client.invoke.handlers;

import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.RNSPortType;

import edu.virginia.vcgr.genii.client.invoke.InvocationContext;
import edu.virginia.vcgr.genii.client.invoke.PipelineProcessor;

public class DebugListHandler 
{
	@PipelineProcessor(portType = RNSPortType.class)
	public ListResponse list(InvocationContext ctxt, List listRequest) throws Throwable
	{
		System.err.println("DEBUG:  list called.");
		return (ListResponse)ctxt.proceed();
	}
}