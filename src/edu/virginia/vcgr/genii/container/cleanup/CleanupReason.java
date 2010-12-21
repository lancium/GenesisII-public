package edu.virginia.vcgr.genii.container.cleanup;

public class CleanupReason
{
	private String _reason;
	
	public CleanupReason(String reason)
	{
		if (reason == null)
			throw new IllegalArgumentException(
				"Cleanup reason cannot be null.");
		
		_reason = reason;
	}
	
	@Override
	final public String toString()
	{
		return _reason;
	}
}