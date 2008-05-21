package edu.virginia.vcgr.genii.client.stats;

public enum TimeInterval
{
	THIRTY_SECONDS(1000L * 30, "Thirty Seconds", "30 secs."),
	ONE_MINUTE(1000L * 60, "One Minute", "1 min."),
	FIVE_MINUTES(1000L * 60 * 5, "Five Minutes", "5 mins.");
	
	final private long _durationMS;
	final private String _longDescription;
	final private String _shortDescription;
	
	TimeInterval(long durationMS, String longDescription, String shortDescription)
	{
		_durationMS = durationMS;
		_longDescription = longDescription;
		_shortDescription = shortDescription;
	}
	
	public long durationMS()
	{
		return _durationMS;
	}

	public String longDescription()
	{
		return _longDescription;
	}
	
	public String shortDescription()
	{
		return _shortDescription;
	}
}