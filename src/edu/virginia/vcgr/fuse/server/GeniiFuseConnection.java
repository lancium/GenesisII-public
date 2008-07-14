package edu.virginia.vcgr.fuse.server;

import fuse.FuseException;

public interface GeniiFuseConnection
{
	public void unmount() throws FuseException;
}