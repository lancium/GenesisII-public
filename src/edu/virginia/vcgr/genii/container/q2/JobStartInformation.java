package edu.virginia.vcgr.genii.container.q2;

import org.ggf.jsdl.JobDefinition_Type;

import edu.virginia.vcgr.genii.client.context.ICallingContext;

/**
 * A simple data structure for keeping together JSDL and calling context
 * that allows for a simple return from another method.  This is mostly
 * sugar coating to make calling the database class easier.
 * 
 * @author mmm2a
 */
public class JobStartInformation
{
	private ICallingContext _callingContext;
	private JobDefinition_Type _jsdl;
	
	public JobStartInformation(
		ICallingContext callingContext, JobDefinition_Type jsdl)
	{
		_callingContext = callingContext;
		_jsdl = jsdl;
	}
	
	public ICallingContext getCallingContext()
	{
		return _callingContext;
	}
	
	public JobDefinition_Type getJSDL()
	{
		return _jsdl;
	}
}