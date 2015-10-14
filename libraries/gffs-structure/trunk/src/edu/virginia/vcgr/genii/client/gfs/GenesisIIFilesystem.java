package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rbyteio.RandomByteIOPortType;
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
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.fuse.DirectoryManager;
import edu.virginia.vcgr.genii.client.fuse.MetadataManager;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSIterable;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.KeystoreManager;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.identity.Identity;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

public class GenesisIIFilesystem implements FSFilesystem
{
	static private Log _logger = LogFactory.getLog(GenesisIIFilesystem.class);
	static final int FILE_TABLE_SIZE = 256;

	private RNSPath _root;
	private RNSPath _lastPath;
	private Collection<Identity> _callerIdentities;

	public static String CREDENTIAL_ERROR_MESSAGE = "There are no credentials or they have expired.  Cannot operate on: ";

	private FileHandleTable<GeniiOpenFile> _fileTable = new FileHandleTable<GeniiOpenFile>(FILE_TABLE_SIZE);

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

	/**
	 * succeeds if there are "additional credentials" in the wallet. this means that the fuse system is at least logged into *something*. if
	 * there are no additional credentials at all, then this throws an exception. the parameters are in two different forms since methods here
	 * use either a flat file name or an array of strings. we will display whichever one is not null.
	 */
	public static void checkCredentialsAreGood(String name, String[] names) throws FSException
	{
		boolean toReturn = false;
		ICallingContext callingContext;
		try {
			callingContext = ContextManager.getCurrentContext();
			if (callingContext != null) {
				// no context at all means no creds either, so to even think we have credentials we must have a context.
				KeyAndCertMaterial clientKeyMaterial =
					ClientUtils.checkAndRenewCredentials(callingContext, BaseGridTool.credsValidUntil(), new SecurityUpdateResults());
				if (clientKeyMaterial == null) {
					throw new RuntimeException("failed to retrieve a valid TLS certificate for the client");
				}
				TransientCredentials tranCreds = TransientCredentials.getTransientCredentials(callingContext);
				toReturn = (tranCreds != null) && !tranCreds.isEmpty();
			}
		} catch (Throwable e) {
			_logger.warn("exception thrown while checking credentials", e);
		}
		if (_logger.isTraceEnabled()) {
			_logger.debug("cred check says that creds are: " + (toReturn ? "good" : "bad"));
		}
		/*
		 * a note about failures here: if the credentials should become good again at some future point, then we will automatically reload
		 * them above in the check and renew call. thus we don't need any special code to reload the credentials and patch the context; it
		 * just happens as a matter of course.
		 */
		if (toReturn != true) {
			// the credentials are not good; blow out an appropriate type of exception.
			String relevantFile = name;
			if (relevantFile == null) {
				relevantFile = flattenPath(names);
			}
			throw new FSIllegalAccessException(CREDENTIAL_ERROR_MESSAGE + relevantFile);
		}
	}

	/**
	 * returns a list of path components in descending hierarchical order into the corresponding rooted directory path.
	 */
	public static String flattenPath(String[] toFlatten)
	{
		if ((toFlatten == null) || (toFlatten.length == 0)) {
			return null;
		}
		StringBuilder toReturn = new StringBuilder();
		for (String f : toFlatten) {
			toReturn.append("/");
			toReturn.append(f);
		}
		return toReturn.toString();
	}

	FilesystemStatStructure stat(String name, EndpointReferenceType target) throws FSException
	{
		checkCredentialsAreGood(name, null);

		TypeInformation typeInfo = new TypeInformation(target);
		FilesystemEntryType type;

		if (_logger.isTraceEnabled())
			_logger.debug("hitting stat() on name='" + name + "'");

		if (typeInfo.isRNS())
			type = FilesystemEntryType.DIRECTORY;
		else
			type = FilesystemEntryType.FILE;

		long size = 0;
		long created, modified, accessed;

		try {
			if (typeInfo.isRByteIO()) {
				try {
					RandomByteIORP rp = (RandomByteIORP) ResourcePropertyManager.createRPInterface(target, RandomByteIORP.class);
					size = toNonNull(rp.getSize());
					created = toMillis(rp.getCreateTime());
					modified = toMillis(rp.getModificationTime());
					accessed = toMillis(rp.getAccessTime());
				} catch (Throwable cause) {
					size = 0L;
					created = 0;
					modified = accessed = System.currentTimeMillis();
				}
			} else if (typeInfo.isSByteIO()) {
				try {
					StreamableByteIORP rp = (StreamableByteIORP) ResourcePropertyManager.createRPInterface(target, StreamableByteIORP.class);
					size = toNonNull(rp.getSize());
					created = toMillis(rp.getCreateTime());
					modified = toMillis(rp.getModificationTime());
					accessed = toMillis(rp.getAccessTime());
				} catch (Throwable cause) {
					size = 0L;
					created = 0;
					modified = accessed = System.currentTimeMillis();
				}
			} else if (typeInfo.isSByteIOFactory()) {
				try {
					StreamableByteIORP rp = (StreamableByteIORP) ResourcePropertyManager.createRPInterface(target, StreamableByteIORP.class);
					size = toNonNull(rp.getSize());
					created = toMillis(rp.getCreateTime());
					modified = toMillis(rp.getModificationTime());
					accessed = toMillis(rp.getAccessTime());
				} catch (Throwable cause) {
					size = 0L;
					created = 0;
					modified = accessed = System.currentTimeMillis();
				}
			} else {
				created = 0;
				modified = accessed = System.currentTimeMillis();
			}

			Permissions permissions;

			try {
				permissions = (new GenesisIIACLManager(target, _callerIdentities)).getPermissions();
			} catch (Throwable cause) {
				permissions = new Permissions();
			}

			int inode = (int) MetadataManager.generateInodeNumber(target);
			return new FilesystemStatStructure(inode, name, type, size, created, modified, accessed, permissions);
		} catch (Throwable cause) {
			throw FSExceptions.translate(String.format("Unable to stat target %s.", name), cause);
		}
	}

	FilesystemStatStructure stat(RNSPath target) throws RNSPathDoesNotExistException, FSException
	{
		FilesystemStatStructure statStructure = MetadataManager.retrieveStat(target.pwd());
		if (statStructure != null)
			return statStructure;
		return stat(target.getName(), target.getEndpoint());
	}

	public GenesisIIFilesystem(RNSPath root, String sandbox) throws IOException, RNSPathDoesNotExistException, AuthZSecurityException
	{
		ICallingContext callingContext = ContextManager.getExistingContext();

		if (_root == null)
			_root = callingContext.getCurrentPath().getRoot();

		if (sandbox != null)
			_root = _root.lookup(sandbox);

		_root = _root.createSandbox();
		_lastPath = _root;

		_callerIdentities = KeystoreManager.getCallerIdentities(callingContext);
	}

	protected GeniiOpenFile lookup(long fileHandle) throws FSException
	{
		GeniiOpenFile gof = _fileTable.get((int) fileHandle);
		if (gof == null)
			throw new FSInvalidFileHandleException(String.format("Invalid file handle (%d).", fileHandle));

		return gof;
	}

	protected GeniiOpenFile lookupGOF(String[] pathComponents)
	{
		// Lets look and see if the path already exists as an open file in the open file table, if so return it
		int i;
		GeniiOpenFile gof;
		for (i = 0; i < FILE_TABLE_SIZE; i++) {
			gof = _fileTable.get(i);
			if (gof != null) {
				// Check if it is the same file
				// if so, return gof
				if (pathComponents.length == gof.getPath().length) {
					for (int j = 0; j < pathComponents.length; j++) {
						if (!pathComponents[j].equals(gof.getPath()[j]))
							continue;
					}
					return gof;
				}
			}
		}
		return null;
	}

	public RNSPath lookup(String[] pathComponents) throws FSException
	{
		checkCredentialsAreGood(null, pathComponents);

		if (_logger.isTraceEnabled())
			_logger.debug("hitting lookup() on name='" + flattenPath(pathComponents) + "'");

		String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(pathComponents);

		RNSPath entry;
		synchronized (_lastPath) {
			entry = _lastPath.lookup(fullPath);
			if (entry != null)
				_lastPath = entry;
		}
		return entry;
	}

	@Override
	public void chmod(String[] path, Permissions permissions) throws FSException
	{
		checkCredentialsAreGood(null, path);

		RNSPath target = lookup(path);
		if (!target.exists())
			throw new FSEntryNotFoundException(String.format("Couldn't find target path %s.", target.pwd()));

		try {
			GenesisIIACLManager mgr = new GenesisIIACLManager(target.getEndpoint(), _callerIdentities);
			mgr.setPermissions(permissions);
		} catch (Throwable cause) {
			throw FSExceptions.translate("Couldn't change permissions.", cause);
		}
	}

	@Override
	public void close(long fileHandle) throws FSException
	{
		_fileTable.release((int) fileHandle);
	}

	@Override
	public void flush(long fileHandle) throws FSException
	{
		GeniiOpenFile gof = lookup(fileHandle);
		gof.flush();
	}

	@Override
	public void link(String[] sourcePath, String[] targetPath) throws FSException
	{
		checkCredentialsAreGood(null, sourcePath);

		RNSPath source = lookup(sourcePath);
		RNSPath target = lookup(targetPath);

		if (!source.exists())
			throw new FSEntryNotFoundException(String.format("Couldn't find entry %s.", source.pwd()));
		if (target.exists())
			throw new FSEntryAlreadyExistsException(String.format("Entry %s already exists.", target.pwd()));

		try {
			target.link(source.getEndpoint());
			DirectoryManager.addNewDirEntry(target, this, false);
		} catch (Throwable cause) {
			throw FSExceptions.translate("Unable to create link.", cause);
		}
	}

	@Override
	public DirectoryHandle listDirectory(String[] path) throws FSException
	{
		checkCredentialsAreGood(null, path);

		String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(path);
		RNSPath target = lookup(path);
		if (!target.exists())
			throw new FSEntryNotFoundException(String.format("The directory %s does not exist.", target.pwd()));

		try {
			TypeInformation info = new TypeInformation(target.getEndpoint());
			DirectoryHandle directoryHandle;
			if (info.isEnhancedRNS()) {
				_logger.trace("Using Short form for Enhanced-RNS handle.");
				ICallingContext context = ContextManager.getCurrentContext().getParent();

				context.setSingleValueProperty(GenesisIIConstants.RNS_SHORT_FORM_TOKEN, true);

				EnhancedRNSPortType pt = ClientUtils.createProxy(EnhancedRNSPortType.class, target.getEndpoint());
				RNSIterable entries = new RNSIterable(fullPath, pt.lookup(null), context, RNSConstants.PREFERRED_BATCH_SIZE);
				directoryHandle = new EnhancedRNSHandle(this, entries, fullPath);

				context.removeProperty(GenesisIIConstants.RNS_SHORT_FORM_TOKEN);
			} else if (info.isRNS()) {
				directoryHandle = new DefaultRNSHandle(this, target.listContents(true));
			} else {
				throw new FSNotADirectoryException(String.format("Path %s is not a directory.", target.pwd()));
			}
			return directoryHandle;

		} catch (Throwable cause) {
			throw FSExceptions.translate("Unable to list directory contents.", cause);
		}
	}

	@Override
	public void mkdir(String[] path, Permissions initialPermissions) throws FSException
	{
		checkCredentialsAreGood(null, path);

		if (_logger.isTraceEnabled())
			_logger.debug("hitting mkdir() on name='" + flattenPath(path) + "'");

		RNSPath target = lookup(path);
		if (target.exists())
			throw new FSEntryAlreadyExistsException(String.format("Directory %s already exists.", target.pwd()));

		try {
			target.mkdir();
			if (initialPermissions != null) {
				GenesisIIACLManager mgr = new GenesisIIACLManager(target.getEndpoint(), _callerIdentities);
				mgr.setCreatePermissions(initialPermissions);
			}
			DirectoryManager.addNewDirEntry(target, this, true);
		} catch (Throwable cause) {
			throw FSExceptions.translate("Unable to create directory.", cause);
		}
	}

	private long open(String[] path, boolean wasCreated, RNSPath target, EndpointReferenceType epr, OpenFlags flags, OpenModes mode)
		throws FSException, ResourceException, GenesisIISecurityException, RemoteException, IOException
	{
		checkCredentialsAreGood(null, path);

		GeniiOpenFile gof;

		TypeInformation tInfo = new TypeInformation(epr);

		if (tInfo.isRByteIO()) {
			gof = new RandomByteIOOpenFile(path, epr, true, mode == OpenModes.READ_WRITE, flags.isAppend());
		} else if (tInfo.isSByteIO())
			gof = new StreamableByteIOOpenFile(path, wasCreated, epr, true, mode == OpenModes.READ_WRITE, flags.isAppend());
		else if (tInfo.isSByteIOFactory())
			gof = new StreamableByteIOFactoryOpenFile(path, epr, true, mode == OpenModes.READ_WRITE, flags.isAppend());
		else {
			String eprString = ObjectSerializer.toString(epr, new QName(GenesisIIConstants.GENESISII_NS, "endpoint"), false);
			gof = new GenericGeniiOpenFile(path, ByteBuffer.wrap(eprString.getBytes()), true, mode == OpenModes.READ_WRITE, flags.isAppend());
		}

		return _fileTable.allocate(gof);
	}

	@Override
	public long open(String[] path, OpenFlags flags, OpenModes mode, Permissions initialPermissions) throws FSException
	{
		checkCredentialsAreGood(null, path);

		RNSPath target = lookup(path);
		EndpointReferenceType epr;

		try {
			if (target.exists()) {
				if (flags.isTruncate())
					truncate(path, 0L);

				epr = target.getEndpoint();

				if (flags.isExclusive())
					throw new FSEntryAlreadyExistsException(String.format("Path %s already exists.", target.pwd()));

				return open(path, false, target, epr, flags, mode);
			} else {
				if (!flags.isCreate())
					throw new FSEntryNotFoundException(String.format("Couldn't find path %s.", target.pwd()));

				epr = target.createNewFile();
				if (initialPermissions != null)
					(new GenesisIIACLManager(epr, _callerIdentities)).setCreatePermissions(initialPermissions);

				DirectoryManager.addNewDirEntry(target, this, true);
				return open(path, true, target, epr, flags, mode);
			}
		} catch (Throwable cause) {
			throw FSExceptions.translate("Unable to open file.", cause);
		}
	}

	@Override
	public void read(long fileHandle, long offset, ByteBuffer target) throws FSException
	{
		GeniiOpenFile gof = lookup(fileHandle);
		gof.read(offset, target);
	}

	@Override
	public void write(long fileHandle, long offset, ByteBuffer source) throws FSException
	{
		GeniiOpenFile gof = lookup(fileHandle);
		gof.write(offset, source);
	}

	@Override
	public FilesystemStatStructure stat(String[] path) throws FSException
	{
		checkCredentialsAreGood(null, path);

		RNSPath target = lookup(path);
		if (!target.exists())
			throw new FSEntryNotFoundException(String.format("Unable to locate path %s.", target.pwd()));

		try {
			return stat(target);
		} catch (Throwable cause) {
			throw FSExceptions.translate("Unable to stat path.", cause);
		}
	}

	@Override
	public void truncate(String[] path, long newSize) throws FSException
	{
		checkCredentialsAreGood(null, path);

		RNSPath target = lookup(path);
		if (!target.exists())
			throw new FSEntryNotFoundException(String.format("Couldn't find path %s.", target.pwd()));

		try {
			TypeInformation info = new TypeInformation(target.getEndpoint());
			if (info.isRByteIO()) {
				RandomByteIOTransferer transferer =
					RandomByteIOTransfererFactory.createRandomByteIOTransferer(ClientUtils.createProxy(RandomByteIOPortType.class,
						target.getEndpoint()));
				transferer.truncAppend(newSize, new byte[0]);

			} else if (info.isSByteIO()) {
				// Can't do this.
			} else if (info.isSByteIOFactory()) {
				// Can't do this.
			} else if (info.isRNS()) {
				throw new FSNotAFileException(String.format("Path %s is not a file.", target.pwd()));
			} else
				throw new FSIllegalAccessException(String.format("Path %s is read only.", target.pwd()));
		} catch (Throwable cause) {
			throw FSExceptions.translate("Unable to truncate file.", cause);
		}
	}

	@Override
	public void unlink(String[] path) throws FSException
	{
		checkCredentialsAreGood(null, path);

		RNSPath target = lookup(path);
		if (!target.exists())
			throw new FSEntryNotFoundException(String.format("Couldn't locate path %s.", target.pwd()));

		try {
			MetadataManager.removeCachedAttributes(target);
			try {
				target.delete();
			}
			catch (RNSPathDoesNotExistException ne) {
				/* 
				 * Do not need to do anything, we are trying to delete it, and it is not there. ASG 2015-08-24			
				 */
			}
			catch (Throwable cause){
				if (cause instanceof FSNotADirectoryException) {
					// Swallow it
				}
				throw FSExceptions.translate("Unable to delete entry: " + target.pwd(), cause);
			}
			DirectoryManager.removeDirEntry(target);
		} 
		catch (Throwable cause) {
			throw FSExceptions.translate("Unable to delete entry: "  + target.pwd(), cause);
		}
	}

	@Override
	public void updateTimes(String[] path, long accessTime, long modificationTime) throws FSException
	{
		checkCredentialsAreGood(null, path);

		RNSPath target = lookup(path);
		if (!target.exists())
			throw new FSEntryNotFoundException(String.format("Unable to find path %s.", target.pwd()));

		try {
			TypeInformation info = new TypeInformation(target.getEndpoint());
			if (info.isRByteIO()) {
				RandomByteIORP rp = (RandomByteIORP) ResourcePropertyManager.createRPInterface(target.getEndpoint(), RandomByteIORP.class);
				rp.setAccessTime(toCalendar(accessTime));
				rp.setModificationTime(toCalendar(modificationTime));
			} else if (info.isSByteIO()) {
				StreamableByteIORP rp =
					(StreamableByteIORP) ResourcePropertyManager.createRPInterface(target.getEndpoint(), StreamableByteIORP.class);
				rp.setAccessTime(toCalendar(accessTime));
				rp.setModificationTime(toCalendar(modificationTime));
			}
		} catch (Throwable cause) {
			throw FSExceptions.translate(String.format("Unable to update timestamps for %s.", target.pwd()), cause);
		}
	}

	@Override
	public void rename(String[] fromPath, String[] toPath) throws FSException
	{
		checkCredentialsAreGood(null, fromPath);

		RNSPath from = lookup(fromPath);
		RNSPath to = lookup(toPath);

		if (!from.exists())
			throw new FSEntryNotFoundException(String.format("Unable to locate path %s.", from.pwd()));
		if (to.exists())
			throw new FSEntryAlreadyExistsException(String.format("Path %s already exists.", to.pwd()));

		try {
			to.link(from.getEndpoint());
			from.unlink();
		} catch (Throwable cause) {
			throw FSExceptions.translate(String.format("Unable to rename %s to %s.", from.pwd(), to.pwd()), cause);
		}
	}

	public static void testCredentialCheckingCost(String name, String[] names)
	{
		Date now = new Date();
		_logger.debug("starting timing test of cred checker at: " + now.toString());
		double REPEAT_COUNT = 10000;
		for (int i = 0; i < REPEAT_COUNT; i++) {
			try {
				checkCredentialsAreGood(name, names);
			} catch (FSException e) {
				_logger.error("failed during credential timing test with exception", e);
			}
		}
		Date newNow = new Date();
		_logger.debug("ended timing test of cred checker at: " + newNow.toString());
		double msSpent = newNow.getTime() - now.getTime();
		_logger.debug("total time taken = " + msSpent + "ms, which per operation is " + (double) (msSpent / REPEAT_COUNT));
	}
}
