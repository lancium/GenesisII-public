package edu.virginia.vcgr.fuse.fs.genii;

import java.util.Calendar;
import java.util.Collection;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;

public interface GeniiFuseFileSystemContext
{
	public Calendar getMountTime();
	public ICallingContext getCallingContext();
	public Collection<Identity> getCallerIdentities();
	public RNSPath getRoot();
}