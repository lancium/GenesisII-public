package edu.virginia.vcgr.smb.server;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.filters.RNSFilter;

public class SMBSearchFilter implements RNSFilter {
	private RNSFilter left;
	private RNSFilter right;
	
	public SMBSearchFilter(RNSFilter left, RNSFilter right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public boolean matches(RNSPath testEntry) {
		return left.matches(testEntry) && right.matches(testEntry);
	}
}
