package edu.virginia.vcgr.genii.container.q2;

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
	
	public void reserveSlot()
	{
		if (_slotsAvailable <= 0)
			throw new RuntimeException("Slot underflow exception.");
		
		_slotsAvailable--;
	}
}