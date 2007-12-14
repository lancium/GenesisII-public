package edu.virginia.vcgr.genii.container.q2;

/**
 * A data structure which binds a bes container ID with the number of slots
 * that that container has available.  This number starts out as the total
 * allocated to the container, and later gets refined by calls to reserveSlot.
 * 
 * @author mmm2a
 */
public class ResourceSlots
{
	private long _besID;
	private int _slotsAvailable;
	
	public ResourceSlots(BESData besData)
	{
		_besID = besData.getID();
		_slotsAvailable = besData.getTotalSlots();
	}
	
	public long getBESID()
	{
		return _besID;
	}
	
	public int slotsAvailable()
	{
		return _slotsAvailable;
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
}