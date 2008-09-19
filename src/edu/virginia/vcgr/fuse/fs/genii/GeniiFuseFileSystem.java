package edu.virginia.vcgr.fuse.fs.genii;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Collection;

import edu.virginia.vcgr.fuse.fs.FuseFileSystem;
import edu.virginia.vcgr.fuse.fs.FuseFileSystemEntry;
import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import fuse.FuseException;

public class GeniiFuseFileSystem implements FuseFileSystem
{
	static private final int DEFAULT_BLOCK_SIZE = 512;
	static private final int DEFAULT_FILENAME_SIZE = 1024;

	private TimedOutLRUCache<String, FuseFileSystemEntry> _lookupCache =
		new TimedOutLRUCache<String, FuseFileSystemEntry>(4, 1000L * 4);
	
	private Calendar _mountTime;
	
	private ICallingContext _callingContext;
	private RNSPath _root;
	private RNSPath _lastPath;
	private Collection<Identity> _callerIdentities;
	
	public GeniiFuseFileSystem(ICallingContext callingContext,
		RNSPath root, String sandbox)
			throws IOException, RNSPathDoesNotExistException,
				GeneralSecurityException
	{
		_mountTime = Calendar.getInstance();
		
		if (callingContext == null)
			callingContext = ContextManager.getCurrentContext();
		_callingContext = callingContext;
		
		if (_root == null)
			_root = callingContext.getCurrentPath().getRoot();
		
		if (sandbox != null)
			_root = _root.lookup(sandbox);
		
		_root = _root.createSandbox();
		_lastPath = _root;
		
		_callerIdentities = SecurityUtils.getCallerIdentities(callingContext);
	}
	
	@Override
	public int blockSize()
	{
		return DEFAULT_BLOCK_SIZE;
	}

	@Override
	public int blocksFree()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int filesFree()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public FuseFileSystemEntry lookup(String path) throws FuseException
	{
		FuseFileSystemEntry entry = _lookupCache.get(path);
		if (entry == null)
		{
			RNSPath target = _lastPath.lookup(path);
			_lastPath = target;
			entry = new GeniiFuseEntryFacade(target, 
				new GeniiFileSystemContextImpl());
			_lookupCache.put(path, entry);
		}
		
		return entry;
	}

	@Override
	public int maxEntryNameLength()
	{
		return DEFAULT_FILENAME_SIZE;
	}

	@Override
	public int totalBlocks()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int totalFiles()
	{
		return Integer.MAX_VALUE;
	}
	
	private class GeniiFileSystemContextImpl implements GeniiFuseFileSystemContext
	{
		@Override
		public Calendar getMountTime()
		{
			return _mountTime;
		}
		
		@Override
		public Collection<Identity> getCallerIdentities()
		{
			return _callerIdentities;
		}

		@Override
		public ICallingContext getCallingContext()
		{
			return _callingContext;
		}

		@Override
		public RNSPath getRoot()
		{
			return _root;
		}
	}
}
