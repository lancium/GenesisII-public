package edu.virginia.vcgr.genii.client.logging;

import java.io.Serializable;

/**
 * this class is just a placeholder now for the removed dlog feature. the class must still exist in case old installs have it in stored
 * calling contexts, so that the context can be deserialized properly.
 */
public class LoggingContext implements Serializable, Cloneable
{
	private static final long serialVersionUID = 1L;

	// The log id for this context
	@SuppressWarnings("unused")
	private String _rpcid = "n";
}
