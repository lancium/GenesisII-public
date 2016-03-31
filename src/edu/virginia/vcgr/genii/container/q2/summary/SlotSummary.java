package edu.virginia.vcgr.genii.container.q2.summary;

public class SlotSummary
{
	private long _slotsAvailable;
	private long _slotsUsed;

	private long _coresAvailable;
	private long _coresUsed;

	public SlotSummary(long slotsAvailable, long slotsUsed, long coresAvailable, long coresUsed)
	{
		_slotsAvailable = slotsAvailable;
		_slotsUsed = slotsUsed;
	}

	public SlotSummary()
	{
		this(0L, 0L, 0L, 0l);
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
		_coresAvailable += summary._coresAvailable;
		_coresUsed += summary._coresUsed;
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

	/* Update the core information */

	final public void addAvailableCores(int numAvailable)
	{
		_coresAvailable += numAvailable;
	}

	final public void addUsedCores(int numUsed)
	{
		_coresUsed += numUsed;
	}

	final public void addCores(int numAvailable, int numUsed)
	{
		addAvailableCores(numAvailable);
		addUsedCores(numUsed);
	}

	final public long coresAvailable()
	{
		return _coresAvailable;
	}

	final public long coresUsed()
	{
		return _coresUsed;
	}

	final public long totalCores()
	{
		return coresAvailable() + coresUsed();
	}
}