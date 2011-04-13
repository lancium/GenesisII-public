package edu.virginia.vcgr.ogrsh.server.handlers;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.RNSEntryResponseType;
import org.morgan.util.GUID;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rp_2.UpdateResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.UpdateType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactory;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSIterable;
import edu.virginia.vcgr.genii.client.rns.RNSLegacyProxy;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.ogrsh.server.comm.OGRSHOperation;
import edu.virginia.vcgr.ogrsh.server.dir.StatBuffer;
import edu.virginia.vcgr.ogrsh.server.dir.TimeValStructure;
import edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException;
import edu.virginia.vcgr.ogrsh.server.packing.IOGRSHReadBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IOGRSHWriteBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IPackable;
import edu.virginia.vcgr.ogrsh.server.util.StatUtils;

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
		private long _inode;
		private DirectoryEntryType _entryType;
		private String _entryName;
		
		public DirectoryEntry(long inode, String entryName, DirectoryEntryType entryType)
		{
			_inode = inode;
			_entryName = entryName;
			_entryType = entryType;
		}
		
		public void pack(IOGRSHWriteBuffer buffer) throws IOException
		{
			buffer.writeObject(_inode);
			buffer.writeObject(_entryName);
			buffer.writeObject(_entryType.getValue());
		}

		public void unpack(IOGRSHReadBuffer buffer) throws IOException
		{
			_inode = (Long)buffer.readObject();
			_entryName = String.class.cast(buffer.readObject());
			_entryType = DirectoryEntryType.fromInt((Integer)buffer.readObject());
		}
	}
	
	static private class DirectorySession
	{
		private Collection<DirectoryEntry> _entries;
		private Iterator<DirectoryEntry> _currentIterator;
		
		public DirectorySession(Collection<DirectoryEntry> entries)
		{
				_entries = entries;
				_currentIterator = _entries.iterator();
		}
		
		public Iterator<DirectoryEntry> getIterator()
		{
			return _currentIterator;
		}
		
		public void rewind()
		{
			_currentIterator = _entries.iterator();
		}
	}
	
	private HashMap<String, DirectorySession> _openSessions =
		new HashMap<String, DirectorySession>();
	
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
			EnhancedRNSPortType dirPT = ClientUtils.createProxy(
				EnhancedRNSPortType.class, full.getEndpoint());
			RNSIterable iterable = new RNSIterable(
				dirPT.lookup(null), null, 100);
			LinkedList<DirectoryEntry> entries = new LinkedList<DirectoryEntry>();
			for (RNSEntryResponseType et : iterable)
			{
				String entryName = et.getEntryName();
				TypeInformation ti = new TypeInformation(et.getEndpoint());
				long st_ino = StatUtils.generateInodeNumber(ti.getEndpoint());
				if (ti.isRNS())
				{
					entries.add(new DirectoryEntry(st_ino, entryName, 
						DirectoryEntryType.DIRECTORY));
				}
				else if (ti.isByteIO())
				{
					entries.add(new DirectoryEntry(st_ino, entryName,
						DirectoryEntryType.REGULAR_FILE));
				}
				else
				{
					entries.add(new DirectoryEntry(st_ino, entryName,
						DirectoryEntryType.UNKNOWN));
				}
			}
			String key = new GUID().toString();
			_openSessions.put(key, new DirectorySession(entries));
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
		DirectorySession session = _openSessions.get(dirSession);
		if (session == null)
			throw new OGRSHException(OGRSHException.EBADF, 
				"Unknown directory session.");
		Iterator<DirectoryEntry> iter = session.getIterator();
		if (iter.hasNext())
			return iter.next();
		return null;
	}
	
	@OGRSHOperation
	public int rewinddir(String dirSession)
		throws OGRSHException
	{
		DirectorySession session = _openSessions.get(dirSession);
		if (session == null)
			throw new OGRSHException(OGRSHException.EBADF, 
				"Unknown directory session.");
		session.rewind();
		return 0;
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
	public int utimes(String fullpath, TimeValStructure accessTime, TimeValStructure modTime)
		throws OGRSHException
	{
		EndpointReferenceType epr = null;
		GeniiCommon proxy = null;
		
		_logger.debug("utime'ing path \"" + fullpath + "\".");
	
		try
		{
			RNSPath currentPath = RNSPath.getCurrent();
			RNSPath full = currentPath.lookup(fullpath, RNSPathQueryFlags.MUST_EXIST);
			TypeInformation ti = new TypeInformation(full.getEndpoint());
			if (!ti.isByteIO())
			{
				// Can't set times on non-byte-ios
				throw new OGRSHException(OGRSHException.EROFS, "Cannot change timestamps on non-files.");
			}
			
			if (ti.isSByteIOFactory())
			{
				StreamableByteIOFactory proxy2 = ClientUtils.createProxy(
					StreamableByteIOFactory.class, ti.getEndpoint());
				epr = proxy2.openStream(null).getEndpoint();
				ti = new TypeInformation(epr);
				proxy = ClientUtils.createProxy(GeniiCommon.class, epr);
			} else
			{
				proxy = ClientUtils.createProxy(GeniiCommon.class, full.getEndpoint());
			}
			
			String ns = (ti.isRByteIO() ? 
				ByteIOConstants.RANDOM_BYTEIO_NS : ByteIOConstants.STREAMABLE_BYTEIO_NS);
			
			Calendar aTime = Calendar.getInstance();
			Calendar mTime = Calendar.getInstance();
			
			aTime.setTimeInMillis(accessTime.getSeconcds() * 1000L + accessTime.getMicroSeconds() / 1000L);
			mTime.setTimeInMillis(modTime.getSeconcds() * 1000L + modTime.getMicroSeconds() / 1000L);
			
			proxy.updateResourceProperties(new UpdateResourceProperties(
					new UpdateType(new MessageElement[] {
				new MessageElement(new QName(ns, ByteIOConstants.ACCESSTIME_ATTR_NAME), aTime),
				new MessageElement(new QName(ns, ByteIOConstants.MODTIME_ATTR_NAME), mTime)
			})));
			
			return 0;
		}
		catch (OGRSHException oe)
		{
			throw oe;
		}
		catch (Throwable cause)
		{
			throw new OGRSHException(cause);
		}
		finally
		{
			if (epr != null)
			{
				try
				{
					GeniiCommon common = ClientUtils.createProxy(
						GeniiCommon.class, epr);
					common.destroy(new Destroy());
				}
				catch (Throwable t)
				{
				}
			}
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
	
			if (!(new TypeInformation(path.getEndpoint()).isRNS()))
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
			EnhancedRNSPortType dirPT = ClientUtils.createProxy(
				EnhancedRNSPortType.class, full.getEndpoint());
			RNSLegacyProxy proxy = new RNSLegacyProxy(dirPT);
			if (proxy.lookup().length != 0)
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
	
	@OGRSHOperation
	public int rename(String oldFullPath, String newFullPath) 
		throws OGRSHException
	{
		try
		{
			RNSPath currentPath = RNSPath.getCurrent();
			RNSPath o = currentPath.lookup(oldFullPath, 
				RNSPathQueryFlags.MUST_EXIST);
			RNSPath n = currentPath.lookup(newFullPath, 
				RNSPathQueryFlags.MUST_NOT_EXIST);
			n.link(o.getEndpoint());
			o.unlink();
		}
		catch (Throwable cause)
		{
			throw new OGRSHException(cause);
		}
		
		return 0;
	}
}
