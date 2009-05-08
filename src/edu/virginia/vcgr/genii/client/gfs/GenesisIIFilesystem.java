package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ggf.rns.EntryType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fsii.DirectoryHandle;
import edu.virginia.vcgr.fsii.FSFilesystem;
import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.fsii.FilesystemEntryType;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSEntryAlreadyExistsException;
import edu.virginia.vcgr.fsii.exceptions.FSEntryNotFoundException;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.exceptions.FSIllegalAccessException;
import edu.virginia.vcgr.fsii.exceptions.FSInvalidFileHandleException;
import edu.virginia.vcgr.fsii.exceptions.FSNotADirectoryException;
import edu.virginia.vcgr.fsii.exceptions.FSNotAFileException;
import edu.virginia.vcgr.fsii.file.OpenFlags;
import edu.virginia.vcgr.fsii.file.OpenModes;
import edu.virginia.vcgr.fsii.path.UnixFilesystemPathRepresentation;
import edu.virginia.vcgr.fsii.security.Permissions;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.byteio.RandomByteIORP;
import edu.virginia.vcgr.genii.client.byteio.StreamableByteIORP;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.cache.AttributeCache;
import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.iterator.WSIterable;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.client.security.credentials.identity.Identity;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.enhancedrns.IterateListRequestType;
import edu.virginia.vcgr.genii.enhancedrns.IterateListResponseType;

public class GenesisIIFilesystem implements FSFilesystem
{
	static private Log _logger = LogFactory.getLog(GenesisIIFilesystem.class);
	
	private RNSPath _root;
	private RNSPath _lastPath;
	private Collection<Identity> _callerIdentities;
	
	private TimedOutLRUCache<String, RNSPath> _lookupCache =
		new TimedOutLRUCache<String, RNSPath>(128, 1000L * 48);
	
	private FileHandleTable<GeniiOpenFile> _fileTable =
		new FileHandleTable<GeniiOpenFile>(256);
	
	final static private long toNonNull(Long l)
	{
		if (l == null)
			return 0;
		return l.longValue();
	}
	
	final static private long toMillis(Calendar c)
	{
		if (c == null)
			return System.currentTimeMillis();
		return c.getTimeInMillis();
	}
	
	final static private Calendar toCalendar(long time)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		return c;
	}
	
	final static private long generateInodeNumber(EndpointReferenceType target)
	{
		WSName name = new WSName(target);
		if (name.isValidWSName())
		{
			return name.getEndpointIdentifier().toString().hashCode();
		} else
		{
			_logger.warn("Trying to generate an INode number of a target which"
				+ "does not implement the WS-Naming specification.");
			
			try
			{
				byte []array = EPRUtils.toBytes(target);
				long result = 0;
				for (byte d : array)
				{
					result ^= d;
				}
				
				return result;
			}
			catch (ResourceException re)
			{
				_logger.fatal("Unexpected error while trying to serialize EPR.", re);
				throw new RuntimeException(re);
			}
		}
	}
	
	FilesystemStatStructure stat(String name, EndpointReferenceType target)
		throws FSException
	{
		TypeInformation typeInfo = new TypeInformation(target);
		FilesystemEntryType type;
		
		if (typeInfo.isRNS())
			type = FilesystemEntryType.DIRECTORY;
		else
			type = FilesystemEntryType.FILE;
		
		long size = 0;
		long created, modified, accessed;
		
		try
		{
			if (typeInfo.isRByteIO())
			{
				try
				{
					RandomByteIORP rp = 
						(RandomByteIORP)ResourcePropertyManager.createRPInterface(
							target, RandomByteIORP.class);
					size = toNonNull(rp.getSize());
					created = toMillis(rp.getCreateTime());
					modified = toMillis(rp.getModificationTime());
					accessed = toMillis(rp.getAccessTime());
				}
				catch (Throwable cause)
				{
					size = 0L;
					created = 0;
					modified = accessed = System.currentTimeMillis();
				}
			} else if (typeInfo.isSByteIO())
			{
				try
				{
					StreamableByteIORP rp = 
						(StreamableByteIORP )ResourcePropertyManager.createRPInterface(
							target, StreamableByteIORP .class);
					size = toNonNull(rp.getSize());
					created = toMillis(rp.getCreateTime());
					modified = toMillis(rp.getModificationTime());
					accessed = toMillis(rp.getAccessTime());
				}
				catch (Throwable cause)
				{
					size = 0L;
					created = 0;
					modified = accessed = System.currentTimeMillis();
				}
			} else if (typeInfo.isSByteIOFactory())
			{
				try
				{
					StreamableByteIORP rp = 
						(StreamableByteIORP )ResourcePropertyManager.createRPInterface(
							target, StreamableByteIORP .class);
					size = toNonNull(rp.getSize());
					created = toMillis(rp.getCreateTime());
					modified = toMillis(rp.getModificationTime());
					accessed = toMillis(rp.getAccessTime());
				}
				catch (Throwable cause)
				{
					size = 0L;
					created = 0;
					modified = accessed = System.currentTimeMillis();
				}
			} else
			{
				created = 0;
				modified = accessed = System.currentTimeMillis();
			}
			
			Permissions permissions;
			
			try
			{
				permissions = (new GenesisIIACLManager(
					target, _callerIdentities)).getPermissions();
			}
			catch (Throwable cause)
			{
				permissions = new Permissions();
			}
			
			int inode = (int)generateInodeNumber(target);
			return new FilesystemStatStructure(inode, name,
				type, size, created, modified, accessed, permissions);
		}
		catch (Throwable cause)
		{
			throw FSExceptions.translate(String.format(
				"Unable to stat target %s.", name), cause);
		}
	}
	
	FilesystemStatStructure stat(RNSPath target) 
		throws RNSPathDoesNotExistException, FSException
	{
		return stat(target.getName(), target.getEndpoint());
	}
	
	public GenesisIIFilesystem(RNSPath root, String sandbox)
		throws IOException, RNSPathDoesNotExistException,
			GeneralSecurityException
	{
		ICallingContext callingContext = ContextManager.getCurrentContext();
		
		if (_root == null)
			_root = callingContext.getCurrentPath().getRoot();
		
		if (sandbox != null)
			_root = _root.lookup(sandbox);
		
		_root = _root.createSandbox();
		_lastPath = _root;
		
		_callerIdentities = SecurityUtils.getCallerIdentities(callingContext);
	}
	
	protected GeniiOpenFile lookup(long fileHandle) throws FSException
	{
		GeniiOpenFile gof = _fileTable.get((int)fileHandle);
		if (gof == null)
			throw new FSInvalidFileHandleException(String.format(
				"Invalid file handle (%d).", fileHandle));
		
		return gof;
	}
	
	public RNSPath lookup(String []pathComponents) throws FSException
	{
		String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(
			pathComponents);	
		
		RNSPath entry;
		
		synchronized(_lookupCache)
		{
			entry = _lookupCache.get(fullPath);
		}
		
		if (entry == null)
		{
			synchronized(_lastPath)
			{
				entry = _lastPath.lookup(fullPath);
				_lastPath = entry;
			}
			
			synchronized(_lookupCache)
			{
				addToCache(entry);
			}
		}
		
		return entry;
	}
	
	protected void addToCache(RNSPath entry)
	{
		synchronized(_lookupCache)
		{
			if (entry != null)
				_lookupCache.put(entry.pwd(), entry);
		}
	}
	
	@Override
	public void chmod(String[] path, Permissions permissions)
			throws FSException
	{
		RNSPath target = lookup(path);
		if (!target.exists())
			throw new FSEntryNotFoundException(String.format(
				"Couldn't find target path %s.", target.pwd()));
		
		try
		{
			GenesisIIACLManager mgr = new GenesisIIACLManager(target.getEndpoint(),
				_callerIdentities);
			mgr.setPermissions(permissions);
		}
		catch (Throwable cause)
		{
			throw FSExceptions.translate("Couldn't change permissions.", 
				cause);
		}
	}

	@Override
	public void close(long fileHandle) throws FSException
	{
		_fileTable.release((int)fileHandle);
	}

	@Override
	public void flush(long fileHandle) throws FSException
	{
		GeniiOpenFile gof = lookup(fileHandle);
		gof.flush();
		flushAttributeCache(gof.getPath());
	}

	@Override
	public void link(String[] sourcePath, String[] targetPath)
			throws FSException
	{
		RNSPath source = lookup(sourcePath);
		RNSPath target = lookup(targetPath);
		
		if (!source.exists())
			throw new FSEntryNotFoundException(String.format(
				"Couldn't find entry %s.", source.pwd()));
		if (target.exists())
			throw new FSEntryAlreadyExistsException(String.format(
				"Entry %s already exists.", target.pwd()));
		
		try
		{
			target.link(source.getEndpoint());
		}
		catch (Throwable cause)
		{
			throw FSExceptions.translate("Unable to create link.", cause);
		}
	}

	@Override
	public DirectoryHandle listDirectory(String[] path) throws FSException
	{
		RNSPath target = lookup(path);
		if (!target.exists())
			throw new FSEntryNotFoundException(String.format(
				"The directory %s does not exist.", target.pwd()));
		
		try
		{
			TypeInformation info = new TypeInformation(target.getEndpoint());
			if (info.isEnhancedRNS())
			{
				EnhancedRNSPortType pt = ClientUtils.createProxy(
					EnhancedRNSPortType.class, target.getEndpoint());
				IterateListResponseType response = pt.iterateList(
					new IterateListRequestType());
				WSIterable<EntryType> entries = new WSIterable<EntryType>(
					EntryType.class, response.getResult(),
						100, true);
				return new EnhancedRNSHandle(this, entries);
			} else if (info.isRNS())
			{
				return new DefaultRNSHandle(this, target.listContents());
			} else
			{
				throw new FSNotADirectoryException(String.format(
					"Path %s is not a directory.", target.pwd()));
			}
		}
		catch (Throwable cause)
		{
			throw FSExceptions.translate("Unable to list directory contents.", 
				cause);
		}
	}

	@Override
	public void mkdir(String[] path, Permissions initialPermissions)
			throws FSException
	{
		RNSPath target = lookup(path);
		if (target.exists())
			throw new FSEntryAlreadyExistsException(String.format(
				"Directory %s already exists.", target.pwd()));
		
		try
		{
			target.mkdir();
			if (initialPermissions != null)
			{
				GenesisIIACLManager mgr = new GenesisIIACLManager(
					target.getEndpoint(), _callerIdentities);
				mgr.setPermissions(initialPermissions);
			}
		}
		catch (Throwable cause)
		{
			throw FSExceptions.translate("Unable to create directory.", cause);
		}
	}

	private long open(String[] path, boolean wasCreated, RNSPath target, 
		EndpointReferenceType epr, OpenFlags flags, OpenModes mode) 
			throws FSException, ResourceException, GenesisIISecurityException, 
				RemoteException, IOException
	{
		GeniiOpenFile gof;
				
		TypeInformation tInfo = new TypeInformation(epr);
		if (tInfo.isRByteIO())
			gof = new RandomByteIOOpenFile(path, epr, true, 
				mode == OpenModes.READ_WRITE, flags.isAppend());
		else if (tInfo.isSByteIO())
			gof = new StreamableByteIOOpenFile(path, wasCreated, epr, true,
				mode == OpenModes.READ_WRITE, flags.isAppend());
		else if (tInfo.isSByteIOFactory())
			gof = new StreamableByteIOFactoryOpenFile(path, epr, true,
				mode == OpenModes.READ_WRITE, flags.isAppend());
		else
		{
			String eprString = ObjectSerializer.toString(
				epr, new QName(GenesisIIConstants.GENESISII_NS, "endpoint"),
				false);
			gof = new GenericGeniiOpenFile(path, ByteBuffer.wrap(eprString.getBytes()),
				true, mode == OpenModes.READ_WRITE, flags.isAppend());
		}
		
		return _fileTable.allocate(gof);
	}
	
	@Override
	public long open(String[] path, OpenFlags flags, OpenModes mode,
		Permissions initialPermissions) throws FSException
	{
		RNSPath target = lookup(path);
		EndpointReferenceType epr;
		
		try
		{
			if (target.exists())
			{
				if (flags.isTruncate())
					truncate(path, 0L);
				
				epr = target.getEndpoint();
				
				if (flags.isExclusive())
					throw new FSEntryAlreadyExistsException(String.format(
						"Path %s already exists.", target.pwd()));
				
				return open(path, false, target, epr, flags, mode);
			} else
			{
				if (!flags.isCreate())
					throw new FSEntryNotFoundException(String.format(
						"Couldn't find path %s.", target.pwd()));
				
				epr = target.createNewFile();
				if (initialPermissions != null)
					(new GenesisIIACLManager(
						epr, _callerIdentities)).setPermissions(
							initialPermissions);
				
				return open(path, true, target, epr, flags, mode);
			}
		}
		catch (Throwable cause)
		{
			throw FSExceptions.translate("Unable to open file.", cause);
		}
	}

	@Override
	public void read(long fileHandle, long offset, ByteBuffer target) 
		throws FSException
	{
		GeniiOpenFile gof = lookup(fileHandle);
		gof.read(offset, target);
	}
	
	@Override
	public void write(long fileHandle, long offset, ByteBuffer source) 
		throws FSException
	{
		GeniiOpenFile gof = lookup(fileHandle);
		gof.write(offset, source);
	}

	@Override
	public FilesystemStatStructure stat(String[] path) throws FSException
	{
		RNSPath target = lookup(path);
		if (!target.exists())
			throw new FSEntryNotFoundException(String.format(
				"Unable to locate path %s.", target.pwd()));
		
		try
		{
			return stat(target);
		}
		catch (Throwable cause)
		{
			throw FSExceptions.translate("Unable to stat path.", cause);
		}
	}

	@Override
	public void truncate(String[] path, long newSize) throws FSException
	{
		RNSPath target = lookup(path);
		if (!target.exists())
			throw new FSEntryNotFoundException(String.format(
				"Couldn't find path %s.", target.pwd()));
		
		try
		{
			TypeInformation info = new TypeInformation(target.getEndpoint());
			if (info.isRByteIO())
			{
				RandomByteIOTransferer transferer =
					RandomByteIOTransfererFactory.createRandomByteIOTransferer(
						ClientUtils.createProxy(
							RandomByteIOPortType.class, target.getEndpoint()));
				transferer.truncAppend(newSize, new byte[0]);
				
				//New size is 0, flush attribute cache
				flushAttributeCache(path);				
			} else if (info.isSByteIO())
			{
				// Can't do this.
			} else if (info.isSByteIOFactory())
			{
				// Can't do this.
			} else if (info.isRNS())
			{
				throw new FSNotAFileException(String.format(
					"Path %s is not a file.", target.pwd()));
			} else
				throw new FSIllegalAccessException(String.format(
					"Path %s is read only.", target.pwd()));
		}
		catch (Throwable cause)
		{
			throw FSExceptions.translate("Unable to truncate file.", cause);
		}
	}

	@Override
	public void unlink(String[] path) throws FSException
	{
		RNSPath target = lookup(path);
		if (!target.exists())
			throw new FSEntryNotFoundException(String.format(
				"Couldn't locate path %s.", target.pwd()));
		
		try
		{
			target.delete();
			
			//Take it out of the attribute cache
			flushAttributeCache(path);
			
			String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(
				path);	
			
			synchronized(_lookupCache)
			{
				_lookupCache.remove(fullPath);
			}
		}
		catch (Throwable cause)
		{
			throw FSExceptions.translate("Unable to delete entry.", cause);
		}
	}

	@Override
	public void updateTimes(String[] path, long accessTime,
			long modificationTime) throws FSException
	{
		RNSPath target = lookup(path);
		if (!target.exists())
			throw new FSEntryNotFoundException(String.format(
				"Unable to find path %s.", target.pwd()));
		
		try
		{
			TypeInformation info = new TypeInformation(target.getEndpoint());
			if (info.isRByteIO())
			{
				RandomByteIORP rp =
					(RandomByteIORP)ResourcePropertyManager.createRPInterface(
						target.getEndpoint(), RandomByteIORP.class);
				rp.setAccessTime(toCalendar(accessTime));
				rp.setModificationTime(toCalendar(modificationTime));
			} else if (info.isSByteIO())
			{
				StreamableByteIORP rp =
					(StreamableByteIORP)ResourcePropertyManager.createRPInterface(
						target.getEndpoint(), StreamableByteIORP.class);
				rp.setAccessTime(toCalendar(accessTime));
				rp.setModificationTime(toCalendar(modificationTime));
			}
		}
		catch (Throwable cause)
		{
			throw FSExceptions.translate(String.format(
				"Unable to update timestamps for %s.", target.pwd()), cause);
		}
	}

	@Override
	public void rename(String[] fromPath, String[] toPath) throws FSException
	{
		RNSPath from = lookup(fromPath);
		RNSPath to = lookup(toPath);
		
		if (!from.exists())
			throw new FSEntryNotFoundException(String.format(
				"Unable to locate path %s.", from.pwd()));
		if (to.exists())
			throw new FSEntryAlreadyExistsException(String.format(
				"Path %s already exists.", to.pwd()));
		
		try
		{
			to.link(from.getEndpoint());
			from.unlink();
			
			//Take the from out of the attribute cache
			flushAttributeCache(fromPath);
			
			String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(
				fromPath);	
			synchronized(_lookupCache)
			{
				_lookupCache.remove(fullPath);
			}
		}
		catch (Throwable cause)
		{
			throw FSExceptions.translate(String.format(
				"Unable to rename %s to %s.", from.pwd(), to.pwd()), cause);
		}
	}
	/*
	 * flushAttributeCache takes in a path and if it exists in the
	 * attribute cache will remove the entry corresponding to the that
	 * path
	 */
	private void flushAttributeCache(String [] path) throws FSException {
		RNSPath rnsPath = lookup(path);
		try {
			WSName wsName = new WSName(rnsPath.getEndpoint());
			if(wsName.isValidWSName()) {				
				AttributeCache.flush(wsName);
			}
		} catch(RNSPathDoesNotExistException rpe){
			_logger.debug(String.format("Path does not exist unexpected for "
					+"%s", UnixFilesystemPathRepresentation.INSTANCE.toString(
							path)));
		}
	}
}