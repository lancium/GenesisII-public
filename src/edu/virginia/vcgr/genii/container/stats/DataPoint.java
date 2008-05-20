package edu.virginia.vcgr.genii.container.stats;

public interface DataPoint
{
	public boolean withinWindow(long currentTime, long windowSize);
}