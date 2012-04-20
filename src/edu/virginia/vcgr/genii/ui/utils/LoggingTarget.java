package edu.virginia.vcgr.genii.ui.utils;

// Author: Chris Koeritz

/**
 * A per-program object that provides a pathway to the real target of
 * diagnostic and debugging information.  This must be initialized by
 * the owner of the diagnostic target; until then, anything logged will
 * be thrown on the floor.
 */
public class LoggingTarget
{
	private static ILoggingRecipient _realTarget;
	
	// sets the actual target to be used for logging.  any previous target
	// is dropped.
	public static synchronized void setTarget(ILoggingRecipient newTarget) {		
		_realTarget = newTarget;
	}
	
	// performs the actual logging to wherever the target object is.  this
	// can only succeed if the target has previously been set.  true is
	// returned if the logging seems to have arrived at a valid destination.
	public static synchronized boolean logInfo(String toConsume, Throwable cause) {
		if (_realTarget == null) return false;  // nothing to log to currently.
		return _realTarget.consumeLogging(toConsume, cause);
	}
}

