package edu.virginia.vcgr.genii.container.q2;

import java.util.Calendar;

/**
 * The BESData structure is the main data structure for storing "in-memory" information about BES resources in the queue. The information in
 * this data structure is considered "small" enough to keep in memory while the container is alive.
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
	 * The total number of slots this container has allocated to it (not the number available for use right now as some may be allocated to
	 * jobs).
	 */
	private int _totalSlots;

	/**
	 * The total number of cores this container has allocated to it (not the number available for use right now as some may be allocated to
	 * jobs).
	 */

	private int _totalCores;

    // MTP: Historical Approximate Queue Delay calculation with decay factor per day
    private double _historicalApproxQueueDelay;

    private final double DECAY_FACTOR = 0.99;

    private long _delayTimeStamp;

    public double get_historicalApproxQueueDelay() {
            return _historicalApproxQueueDelay;
    }

    public void set_historicalApproxQueueDelay(double _historicalApproxQueueDelay) {
            this._historicalApproxQueueDelay = _historicalApproxQueueDelay;
    }

    public void update_historicalApproxQueueDelay(long _queueDelay){
    	long currentTime = Calendar.getInstance().getTimeInMillis();
    	long diff = (currentTime - _delayTimeStamp)/(1000 * 3600 * 24); // difference between last update and current time to calculate the approximate decay
    	double decay = Math.pow(DECAY_FACTOR, diff);

    	_historicalApproxQueueDelay = (_historicalApproxQueueDelay * decay + _queueDelay ) / (1 + decay);
    }

    public BESData(long besID, String besName, int totalSlots)
    {
    	_besID = besID;
    	_besName = besName;
    	_totalSlots = totalSlots;
    	_historicalApproxQueueDelay = 0;
    	_delayTimeStamp = Calendar.getInstance().getTimeInMillis();
    }

    public BESData(long besID, String besName, int totalSlots, int totalCores)
    {
    	_besID = besID;
    	_besName = besName;
    	_totalSlots = totalSlots;
    	_totalCores = totalCores;
    	_historicalApproxQueueDelay = 0;
    	_delayTimeStamp = Calendar.getInstance().getTimeInMillis();
    }

    public BESData(long besID, String besName, int totalSlots, int totalCores, long histApproxDelay)
    {
    	_besID = besID;
    	_besName = besName;
    	_totalSlots = totalSlots;
    	_totalCores = totalCores;
    	_historicalApproxQueueDelay = histApproxDelay;
    	_delayTimeStamp = Calendar.getInstance().getTimeInMillis();
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

	public int getTotalCores()
	{
		return _totalCores;
	}

	public void setTotalCores(int totalCores)
	{
		_totalCores = totalCores;
	}
}