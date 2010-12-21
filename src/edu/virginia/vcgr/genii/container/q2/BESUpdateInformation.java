package edu.virginia.vcgr.genii.container.q2;

import java.util.Date;

/**
 * The data structure used to keep information about updates of BES resources.
 * BES resources are update at a frequency determined by the following
 * equation:
 *     updateInterval = updateFrequency * 2 ^ (MIN(missCap, misses))
 * <B>updateFrequency</B> is a base update frequency
 * <B>missCap</B> is the maximum number of misses to count against the resource
 * in the exponential backoff algorithm
 * <B>misses</B> is the number of updates that the bes resource has failed to
 * respond to.
 * 
 * @author mmm2a
 */
public class BESUpdateInformation
{
	private int _missCap;
	private int _misses;
	private long _updateCycle;
	private long _besID;
	private Date _lastUpdated;
	private Date _lastResponsive;
	private boolean _available;
	
	public BESUpdateInformation(long besID, long updateCycle, int missCap)
	{
		_available = false;
		_missCap = missCap;
		_misses = 0;
		_besID = besID;
		_lastResponsive = null;
		_lastUpdated = null;
		_updateCycle = updateCycle;
	}
	
	public long getBESID()
	{
		return _besID;
	}
	
	/**
	 * Update the information for this BES.  Calling this
	 * method implies that the container was responsive, but not
	 * necessary available.  The isAvailable parameter tells us that.
	 * 
	 * @param isAvailable Was the bes available, or not.
	 */
	synchronized public void update(boolean isAvailable)
	{
		/* Update it's last updates timestamp as well as it's last 
		 * responsive timestamp. */
		_lastUpdated = _lastResponsive = new Date();
				
		/* Update the misses count */
		_misses = 0;
		
		_available = isAvailable;
	}
	
	synchronized public void miss()
	{
		/* Update timestamps */
		_lastUpdated = new Date();
		
		/* Update the misses count */
		_misses = (_misses >= _missCap) ? _misses : (_misses + 1);
		
		_available = false;
	}
	
	/**
	 * A getter method that indicates whether this update record reflects a
	 * responsive and available resource or not.
	 * 
	 * @return True if the represented BES is available, false otherwise.
	 */
	synchronized public boolean isAvailable()
	{
		/* If it has never been updated, it isn't responsive */
		if (_lastResponsive == null || _lastUpdated == null)
			return false;
		
		/* Otherwise, it's responsive if the last responsive timestamp occurrs
		 * on or after the lastupdated timestamp.
		 */
		boolean responsive = !_lastResponsive.before(_lastUpdated);
		
		return responsive && _available;
	}
	
	public Date nextUpdate()
	{
		if (_available || (_lastUpdated == null))
			return null;
		
		Date lastUpdated = _lastUpdated;
		long timeToWait = (_updateCycle << _misses);
		
		return new Date(lastUpdated.getTime() + timeToWait);
	}
	
	/**
	 * Check to see if it's time for another update check.
	 * 
	 * @param now The current time to use in the update frequency calculation.
	 * 
	 * @return True if it's time to update this resource again.
	 */
	public boolean timeForUpdate(Date now)
	{
		/* If we've never update, then it's time for an update by
		 * definition.
		 */
		if (_lastUpdated == null)
			return true;
		
		if (_available)
			return false;
		
		/* Otherwise, see how long we are supposed to wait for the next
		 * update based off of the update frequency and the number of misses
		 * recorded so far.
		 */
		long timeToWait = (_updateCycle << _misses);
		
		/* We need to update if the timeToWait interval has already passed. */
		return (now.getTime() - _lastUpdated.getTime() >= timeToWait);
	}
	
	final public Date lastUpdated()
	{
		return _lastUpdated;
	}
	
	@Override
	public String toString()
	{
		Date nextUpdate = nextUpdate();
		
		String responsiveString = "available";
		if (!_available)
			responsiveString = "not available";
		else if (!isAvailable())
			responsiveString = "not responsive";
		
		if (nextUpdate == null)	
			return String.format("%s (misses = %d, last-updated = %3$tT %3$tD, \n" +
				"\tlast-responsive = %4$tT %4$tD, next-update = none scheduled)",
				responsiveString, _misses, _lastUpdated, _lastResponsive);
		else
			return String.format("%s (misses = %d, last-updated = %3$tT %3$tD, \n" +
				"\tlast-responsive = %4$tT %4$tD, next-update = %5$tT %5$tD)",
				responsiveString, _misses, _lastUpdated, _lastResponsive,
				nextUpdate);
	}
}