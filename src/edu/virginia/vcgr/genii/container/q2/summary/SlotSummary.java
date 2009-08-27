package edu.virginia.vcgr.genii.container.q2.summary;

public class SlotSummary
{
	private long _slotsAvailable;
	private long _slotsUsed;
	
	public SlotSummary(long slotsAvailable, long slotsUsed)
	{
		_slotsAvailable = slotsAvailable;
		_slotsUsed = slotsUsed;
	}
	
	public SlotSummary()
	{
		this(0L, 0L);
	}
	
	final public void addAvailableSlots(int numAvailable)
	{
		_slotsAvailable += numAvailable;
	}
	
	final public void addUsedSlots(int numUsed)
	{
		_slotsUsed += numUsed;
	}
	
	final public void add(int numAvailable, int numUsed)
	{
		addAvailableSlots(numAvailable);
		addUsedSlots(numUsed);
	}
	
	final public void add(SlotSummary summary)
	{
		_slotsAvailable += summary._slotsAvailable;
		_slotsUsed += summary._slotsUsed;
	}
	
	final public long slotsAvailable()
	{
		return _slotsAvailable;
	}
	
	final public long slotsUsed()
	{
		return _slotsUsed;
	}
	
	final public long totalSlots()
	{
		return slotsAvailable() + slotsUsed();
	}
}