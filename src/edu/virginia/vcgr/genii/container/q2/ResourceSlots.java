package edu.virginia.vcgr.genii.container.q2;

/**
 * A data structure which binds a bes container ID with the number of slots that that container has available. This number starts out as the
 * total allocated to the container, and later gets refined by calls to reserveSlot.
 * 
 * @author mmm2a
 */
public class ResourceSlots
{
	private BESData _besData;
	private long _besID;
	private int _slotsAvailable;
	private int _coresAvailable;

	public ResourceSlots(BESData besData)
	{
		_besData = besData;
		_besID = besData.getID();
		_slotsAvailable = besData.getTotalSlots();
		_coresAvailable = besData.getTotalCores();
	}

	public BESData getBESData()
	{
		return _besData;

	}

	public long getBESID()
	{
		return _besID;
	}

	public int slotsAvailable()
	{
		return _slotsAvailable;
	}

	public int coresAvailable()
	{
		return _coresAvailable;
	}

	/**
	 * Reserve one of the remaining slots for a new job.
	 */
	public void reserveSlot()
	{
		if (_slotsAvailable <= 0)
			throw new RuntimeException("Slot underflow exception.");

		_slotsAvailable--;
	}

	/**
	 * Reserve the requested cores from the remaining cores for a new job.
	 */
	public void reserveCores(int cores)
	{
		if (_coresAvailable < cores)
			throw new RuntimeException("Core underflow exception.");

		_coresAvailable -= cores;
	}
}