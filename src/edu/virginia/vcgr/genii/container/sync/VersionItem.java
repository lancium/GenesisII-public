package edu.virginia.vcgr.genii.container.sync;

import java.io.Serializable;

public class VersionItem implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public int uid;
	public int version;

	public VersionItem()
	{
	}
	
	public VersionItem(int uid, int version)
	{
		this.uid = uid;
		this.version = version;
	}
}
