package edu.virginia.vcgr.genii.container.bes.execution;

import java.io.Serializable;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;

/**
 * An execution context is a context that is passed to execution phases that may or may not contain
 * information relevant to them such as who the execution is taking place as, what directory it is
 * taking place in, etc.
 * 
 * @author mmm2a
 */
public interface ExecutionContext
{
	/**
	 * Get the calling context that should be used to make out calls if that is relevant.
	 * 
	 * @return The calling context to use for outcalls.
	 */
	public ICallingContext getCallingContext() throws ExecutionException;

	public String getBESEPI();

	/**
	 * Get the current working directory to use for the execution.
	 * 
	 * @return The current working directory for this activity.
	 */
	public BESWorkingDirectory getCurrentWorkingDirectory() throws ExecutionException;

	public void setProperty(String name, Serializable value) throws ExecutionException;

	public Serializable getProperty(String name) throws ExecutionException;

	public void updateState(ActivityState newState);
}