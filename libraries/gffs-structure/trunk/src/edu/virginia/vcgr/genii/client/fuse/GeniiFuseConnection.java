package edu.virginia.vcgr.genii.client.fuse;

import fuse.FuseException;

public interface GeniiFuseConnection
{
	public void unmount(boolean lazy) throws FuseException;

	public void unmount() throws FuseException;
}