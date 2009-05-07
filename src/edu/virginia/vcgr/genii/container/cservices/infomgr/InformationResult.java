package edu.virginia.vcgr.genii.container.cservices.infomgr;

import java.util.Calendar;

/**
 * A bundle class that wraps together information about an endpoint,
 * Any exceptions that occurred the last time the endpoint was
 * communicated with, and the timestamp of the last update attempt.
 * 
 * @author mmm2a
 *
 * @param <InfoType>
 */
public class InformationResult<InfoType>
{
	private Throwable _exception;
	private Calendar _lastUpdated;
	private InfoType _information;
	
	/**
	 * Construct a new result with the given parameters.
	 * 
	 * @param information The information (if available) for the result.
	 * @param lastUpdated The time that the last update was attempted.
	 * @param exception Any exceptions that occurred (or null) trying to
	 * update the information.
	 */
	public InformationResult(InfoType information, Calendar lastUpdated,
		Throwable exception)
	{
		_exception = exception;
		_lastUpdated = lastUpdated;
		_information = information;
	}
	
	/**
	 * Construct a new result with the given parameters.  This is equivalent
	 * to constructing a result with a null exception parameter.
	 * 
	 * @param information The information (if available) for the result.
	 * @param lastUpdated The time that the last update was attempted.
	 */
	public InformationResult(InfoType information, Calendar lastUpdated)
	{
		this(information, lastUpdated, null);
	}
	
	/**
	 * Was their an exception on the last update attempt?
	 * 
	 * @return True if the endpoint successfully responded to the last update
	 * request, false otherwise.
	 */
	final public boolean wasResponsive()
	{
		return _exception == null;
	}
	
	/**
	 * Did the last attempt to update the target endpoint timeout?
	 * 
	 * @return True if the last attempt timed out, false otherwise.
	 */
	final public boolean timedOut()
	{
		return ( (_exception != null) && 
			(_exception instanceof TimeoutException) );
	}
	
	/**
	 * Retrieve the timestamp for the last update attempt.
	 * 
	 * @return The timestamp of the last update attempt.
	 */
	final public Calendar lastUpdated()
	{
		return _lastUpdated;
	}
	
	/**
	 * Retrieve any information stored with this result.  This information
	 * is not guaranteed to be up to date unless "wasResponsive" returns
	 * true.  If an attempt to update results fails, and old information
	 * exists, then the old information is re-used.
	 * 
	 * @return Any information (possibly null) available.
	 */
	final public InfoType information()
	{
		return _information;
	}
	
	/**
	 * The exception (if any) that occurred the last time this information
	 * was updated (or attempted).
	 * 
	 * @return The exception (or null) that occurred the last time we tried to
	 * update this information.
	 */
	final public Throwable exception()
	{
		return _exception;
	}
}