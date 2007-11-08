package edu.virginia.vcgr.ogrsh.server.handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.RNSPortType;
import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.ogrsh.server.comm.OGRSHOperation;
import edu.virginia.vcgr.ogrsh.server.dir.StatBuffer;
import edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException;
import edu.virginia.vcgr.ogrsh.server.packing.IOGRSHReadBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IOGRSHWriteBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IPackable;

public class DirectoryHandler
{
	static private Log _logger = LogFactory.getLog(DirectoryHandler.class);
	
	static private enum DirectoryEntryType
	{
		UNKNOWN(0),
		DIRECTORY(4),
		REGULAR_FILE(8),
		BLOCK_DEVICE(6),
		LINK(10);
		
		private int _value;
		
		private DirectoryEntryType(int value)
		{
			_value = value;
		}
		
		public int getValue()
		{
			return _value;
		}
		
		static DirectoryEntryType fromInt(int value)
		{
			if (value == UNKNOWN._value)
				return UNKNOWN;
			else if (value == DIRECTORY._value)
				return DIRECTORY;
			else if (value == REGULAR_FILE._value)
				return REGULAR_FILE;
			else if (value == BLOCK_DEVICE._value)
				return BLOCK_DEVICE;
			else if (value == LINK._value)
				return LINK;
			else 
				return UNKNOWN;
		}
	}
	
	static private class DirectoryEntry implements IPackable
	{
		private DirectoryEntryType _entryType;
		private String _entryName;
		
		public DirectoryEntry(String entryName, DirectoryEntryType entryType)
		{
			_entryName = entryName;
			_entryType = entryType;
		}
		
		public DirectoryEntry(IOGRSHReadBuffer buffer) throws IOException
		{
			unpack(buffer);
		}

		public void pack(IOGRSHWriteBuffer buffer) throws IOException
		{
			buffer.writeObject(_entryName);
			buffer.writeObject(_entryType.getValue());
		}

		public void unpack(IOGRSHReadBuffer buffer) throws IOException
		{
			_entryName = String.class.cast(buffer.readObject());
			_entryType = DirectoryEntryType.fromInt((Integer)buffer.readObject());
		}
	}
	
	private HashMap<String, Iterator<DirectoryEntry>> _openSessions =
		new HashMap<String, Iterator<DirectoryEntry>>();
	
	public DirectoryHandler()
	{
	}
	
	@OGRSHOperation
	public String opendir(String fullpath)
		throws OGRSHException
	{
		_logger.debug("Opening directory \"" + fullpath + "\".");
		
		try
		{
			RNSPath currentPath = RNSPath.getCurrent();
			RNSPath full = currentPath.lookup(fullpath, RNSPathQueryFlags.MUST_EXIST);
			RNSPortType dirPT = ClientUtils.createProxy(
				RNSPortType.class, full.getEndpoint());
			ListResponse resp = dirPT.list(new List(".*"));
			LinkedList<DirectoryEntry> entries = new LinkedList<DirectoryEntry>();
			for (EntryType et : resp.getEntryList())
			{
				String entryName = et.getEntry_name();
				TypeInformation ti = new TypeInformation(et.getEntry_reference());
				if (ti.isRNS())
				{
					entries.add(new DirectoryEntry(entryName, 
						DirectoryEntryType.DIRECTORY));
				}
				else if (ti.isByteIO())
				{
					entries.add(new DirectoryEntry(entryName,
						DirectoryEntryType.REGULAR_FILE));
				}
				else
				{
					entries.add(new DirectoryEntry(entryName,
						DirectoryEntryType.UNKNOWN));
				}
			}
			String key = new GUID().toString();
			_openSessions.put(key, entries.iterator());
			return key;
		}
		catch (Throwable cause)
		{
			throw new OGRSHException(cause);
		}
	}
	
	@OGRSHOperation
	public int closedir(String dirSession)
		throws OGRSHException
	{
		_openSessions.remove(dirSession);
		return 0;
	}
	
	@OGRSHOperation
	public DirectoryEntry readdir(String dirSession)
		throws OGRSHException
	{
		Iterator<DirectoryEntry> iter = _openSessions.get(dirSession);
		if (iter.hasNext())
			return iter.next();
		return null;
	}

	@OGRSHOperation
	public int link(String oldPath, String newPath)
		throws OGRSHException
	{
		_logger.debug("link'ing from \"" + oldPath + "\" to \"" + newPath + "\".");
		
		try
		{
			RNSPath currentPath = RNSPath.getCurrent();
			RNSPath o = currentPath.lookup(oldPath, RNSPathQueryFlags.MUST_EXIST);
			RNSPath n = currentPath.lookup(newPath, RNSPathQueryFlags.MUST_NOT_EXIST);
			n.link(o.getEndpoint());
			return 0;
		}
		catch (Throwable cause)
		{
			throw new OGRSHException(cause);
		}
	}
	
	@OGRSHOperation
	public StatBuffer xstat(String fullpath)
		throws OGRSHException
	{
		_logger.debug("xstat'ing path \"" + fullpath + "\".");
		
		try
		{
			RNSPath currentPath = RNSPath.getCurrent();
			RNSPath full = currentPath.lookup(fullpath, RNSPathQueryFlags.MUST_EXIST);
			TypeInformation ti = new TypeInformation(full.getEndpoint());
			return StatBuffer.fromTypeInformation(ti);
		}
		catch (Throwable cause)
		{
			throw new OGRSHException(cause);
		}
	}
	
	@OGRSHOperation
	public int chdir(String fullpath)
		throws OGRSHException
	{
		try
		{
			ICallingContext ctxt = ContextManager.getCurrentContext();
			RNSPath path = ctxt.getCurrentPath().lookup(fullpath,
				RNSPathQueryFlags.MUST_EXIST);
	
			if (!path.isDirectory())
				throw new OGRSHException("Path \"" + path.pwd() + 
					"\" is not an RNS directory.", OGRSHException.NOT_A_DIRECTORY);
			
			ctxt.setCurrentPath(path);
			ContextManager.storeCurrentContext(ctxt);
		}
		catch (Throwable cause)
		{
			throw new OGRSHException(cause);
		}
		
		return 0;
	}
	
	@OGRSHOperation
	public int mkdir(String fullpath, int mode)
		throws OGRSHException
	{
		try
		{
			ICallingContext ctxt = ContextManager.getCurrentContext();
			RNSPath path = ctxt.getCurrentPath().lookup(fullpath,
				RNSPathQueryFlags.MUST_NOT_EXIST);
			path.mkdir();
		}
		catch (Throwable cause)
		{
			throw new OGRSHException(cause);
		}
		
		return 0;
	}
	
	@OGRSHOperation
	public int rmdir(String fullpath) throws OGRSHException
	{
		try
		{
			RNSPath currentPath = RNSPath.getCurrent();
			RNSPath full = currentPath.lookup(fullpath,
				RNSPathQueryFlags.MUST_EXIST);
			RNSPortType dirPT = ClientUtils.createProxy(
				RNSPortType.class, full.getEndpoint());
			ListResponse resp = dirPT.list(new List(".*"));
			if (resp.getEntryList().length != 0)
				throw new OGRSHException(OGRSHException.DIRECTORY_NOT_EMPTY,
					"Directory \"" + fullpath + "\" is not empty.");
			full.delete();
		}
		catch (Throwable cause)
		{
			throw new OGRSHException(cause);
		}
		
		return 0;
	}
}
