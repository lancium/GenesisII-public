package edu.virginia.vcgr.genii.security;

import java.util.EnumSet;

/**
 * Base interface for all decisions based on RWXCategory (Read, Write and
 * Execute) accessibility.
 * 
 * @author ckoeritz
 */
public interface RWXAccessible {
	/**
	 * Retrieves the mask for the object.
	 */
	public EnumSet<RWXCategory> getMask();

	/**
	 * Sets the current mask for the object.
	 */
	public void setMask(EnumSet<RWXCategory> newMask);

	/**
	 * Checks if the object can perform the specified RWX operation.
	 */
	public boolean checkRWXAccess(RWXCategory category);
}
