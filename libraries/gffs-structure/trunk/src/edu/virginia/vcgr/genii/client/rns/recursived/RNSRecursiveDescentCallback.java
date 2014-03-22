package edu.virginia.vcgr.genii.client.rns.recursived;

import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public interface RNSRecursiveDescentCallback
{
	public RNSRecursiveDescentCallbackResult handleRNSPath(RNSPath path) throws RNSException;

	public void finish() throws RNSException;
}