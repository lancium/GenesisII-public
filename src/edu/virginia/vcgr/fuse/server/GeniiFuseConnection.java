package edu.virginia.vcgr.fuse.server;

import fuse.FuseException;

public interface GeniiFuseConnection
{
	public void unmount(boolean lazy) throws FuseException;
	public void unmount() throws FuseException;
}