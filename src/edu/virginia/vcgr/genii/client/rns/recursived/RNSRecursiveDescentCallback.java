package edu.virginia.vcgr.genii.client.rns.recursived;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public interface RNSRecursiveDescentCallback
{
	public boolean handleRNSPath(RNSPath path)
		throws Throwable;
	public void finish() throws Throwable;
}