package edu.virginia.vcgr.genii.container.q2;

/**
 * The BESData structure is the main data structure for storing "in-memory"
 * information about BES resources in the queue.  The information in this
 * data structure is considered "small" enough to keep in memory while the
 * container is alive.
 * 
 * @author mmm2a
 */
public class BESData
{
	/**
	 * The container ID used in the database as a key.
	 */
	private long _besID;
	
	/**
	 * The name given to this container when it was linked into the queue.
	 */
	private String _besName;
	
	/**
	 * The total number of slots this container has allocated to it (not
	 * the number available for use right now as some may be allocated to
	 * jobs).
	 */
	private int _totalSlots;
	
	public BESData(long besID, String besName, int totalSlots)
	{
		_besID = besID;
		_besName = besName;
		_totalSlots = totalSlots;
	}
	
	public long getID()
	{
		return _besID;
	}
	
	public String getName()
	{
		return _besName;
	}
	
	public int getTotalSlots()
	{
		return _totalSlots;
	}
	
	public void setTotalSlots(int totalSlots)
	{
		_totalSlots = totalSlots;
	}
}