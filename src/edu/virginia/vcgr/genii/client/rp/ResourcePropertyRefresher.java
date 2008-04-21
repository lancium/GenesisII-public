package edu.virginia.vcgr.genii.client.rp;

/**
 * This is the interface that is automatically included in all
 * RP managers.  It gives the client the ability to refresh (or
 * reload) the managers attributes or RP cache.
 * 
 * @author mmm2a
 */
public interface ResourcePropertyRefresher
{
	/**
	 * Force any cached resource properties to be reloaded the
	 * next time a call is made to get a resource property.
	 */
	public void refreshResourceProperties();
}
