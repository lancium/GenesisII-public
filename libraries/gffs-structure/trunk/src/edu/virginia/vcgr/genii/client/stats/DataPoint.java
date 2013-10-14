package edu.virginia.vcgr.genii.client.stats;

public interface DataPoint
{
	public boolean withinWindow(long currentTime, long windowSize);
}