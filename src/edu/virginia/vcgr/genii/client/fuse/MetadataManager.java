package edu.virginia.vcgr.genii.client.fuse;

import java.util.Calendar;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fsii.FilesystemEntryType;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.security.PermissionBits;
import edu.virginia.vcgr.fsii.security.Permissions;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cache.unified.CacheUtils;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.common.PermissionsStringTranslator;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rp.DefaultSingleResourcePropertyTranslator;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;
import fuse.FuseStat;

/*
 * This is the helper class for propagating ByteIO attribute updates to the local 
 * cache. Note that when an update is made directly on the RP interface by calling
 * some setProperty method, this class is not needed. It is only used when update
 * is made by some call that changes some attribute of the underlying resource 
 * directly. Like truncate or write system calls from FUSE. Additionally, the 
 * ByteIO to Unix file permission conversion, and INode number generation are 
 * abstracted in this class. This is done just to give a clean appearance outside.
 * 
 * One of the most important responsibilities of this helper class is to bridge the
 * RP-attribute cache with the INode of Unix file system. As the cache is modeled to
 * support both the Grid-Shell and the FUSE driver, and therefore works at the RP-
 * attributes level, we needed a bridging facility that can derive INodes from the 
 * cached attributes.  
 * */
public class MetadataManager
{

	static private Log _logger = LogFactory.getLog(MetadataManager.class);
	static private SingleResourcePropertyTranslator permissionTranslater = new PermissionsStringTranslator();
	static private SingleResourcePropertyTranslator translator = new DefaultSingleResourcePropertyTranslator();

	public static void updateFileSizeAttribute(RNSPath entry, Long newSize)
	{
		try {
			EndpointReferenceType EPR = entry.getEndpoint();
			QName qName = getSizeAttributeQName(EPR);
			if (qName != null) {
				MessageElement sizeElement = new MessageElement(qName, newSize);
				CacheManager.putItemInCache(EPR, qName, sizeElement);
			}
		} catch (RNSPathDoesNotExistException e) {
			_logger.info("failed to update cache", e);
		}
	}

	public static void updateAttributesAfterWrite(RNSPath entry)
	{
		try {
			EndpointReferenceType EPR = entry.getEndpoint();
			updateAttributesAfterWrite(EPR);
		} catch (RNSPathDoesNotExistException e) {
			_logger.info("failed to update cache", e);
		}
	}

	public static void updateAttributesAfterWrite(EndpointReferenceType EPR)
	{

		String nameSpaceForAttributes = CacheUtils.getNamespaceForByteIOAttributes(EPR);
		if (nameSpaceForAttributes != null) {
			CacheManager.removeItemFromCache(EPR, new QName(nameSpaceForAttributes, ByteIOConstants.SIZE_ATTR_NAME),
				MessageElement.class);

			Calendar modificationTime = Calendar.getInstance();
			QName modTimeQName = new QName(nameSpaceForAttributes, ByteIOConstants.MODTIME_ATTR_NAME);
			MessageElement modTimeElement = new MessageElement(modTimeQName, modificationTime);
			CacheManager.putItemInCache(EPR, modTimeQName, modTimeElement);

			CacheManager.putItemInCache(EPR, new QName(nameSpaceForAttributes, ByteIOConstants.ACCESSTIME_ATTR_NAME),
				modTimeElement);
		}
	}

	public static void removeCachedAttributes(RNSPath entry)
	{
		try {
			EndpointReferenceType EPR = entry.getEndpoint();
			CacheManager.removeAllRelevantInfoFromCache(EPR, MessageElement.class);
		} catch (RNSPathDoesNotExistException e) {
			_logger.info("failed to update cache", e);
		}
	}

	public static Permissions permissionsFromMode(int mode)
	{

		Permissions p = new Permissions();

		p.set(PermissionBits.OWNER_READ, (mode & FuseStat.OWNER_READ) > 0);
		p.set(PermissionBits.OWNER_WRITE, (mode & FuseStat.OWNER_WRITE) > 0);
		p.set(PermissionBits.OWNER_EXECUTE, (mode & FuseStat.OWNER_EXECUTE) > 0);

		p.set(PermissionBits.GROUP_READ, (mode & FuseStat.GROUP_READ) > 0);
		p.set(PermissionBits.GROUP_WRITE, (mode & FuseStat.GROUP_WRITE) > 0);
		p.set(PermissionBits.GROUP_EXECUTE, (mode & FuseStat.GROUP_EXECUTE) > 0);

		p.set(PermissionBits.EVERYONE_READ, (mode & FuseStat.OTHER_READ) > 0);
		p.set(PermissionBits.EVERYONE_WRITE, (mode & FuseStat.OTHER_WRITE) > 0);
		p.set(PermissionBits.EVERYONE_EXECUTE, (mode & FuseStat.OTHER_EXECUTE) > 0);

		return p;
	}

	public static int getMode(FilesystemStatStructure statstruct)
	{

		int mode = 0x0;

		Permissions p = statstruct.getPermissions();
		if (p.isSet(PermissionBits.OWNER_READ))
			mode |= FuseStat.OWNER_READ;
		if (p.isSet(PermissionBits.OWNER_WRITE))
			mode |= FuseStat.OWNER_WRITE;
		if (p.isSet(PermissionBits.OWNER_EXECUTE))
			mode |= FuseStat.OWNER_EXECUTE;

		if (p.isSet(PermissionBits.GROUP_READ))
			mode |= FuseStat.GROUP_READ;
		if (p.isSet(PermissionBits.GROUP_WRITE))
			mode |= FuseStat.GROUP_WRITE;
		if (p.isSet(PermissionBits.GROUP_EXECUTE))
			mode |= FuseStat.GROUP_EXECUTE;

		if (p.isSet(PermissionBits.EVERYONE_READ))
			mode |= FuseStat.OTHER_READ;
		if (p.isSet(PermissionBits.EVERYONE_WRITE))
			mode |= FuseStat.OTHER_WRITE;
		if (p.isSet(PermissionBits.EVERYONE_EXECUTE))
			mode |= FuseStat.OTHER_EXECUTE;

		FilesystemEntryType entryType = statstruct.getEntryType();
		if (entryType == FilesystemEntryType.DIRECTORY)
			mode |= FuseStat.TYPE_DIR;
		else
			mode |= FuseStat.TYPE_FILE;

		return mode;
	}

	/*
	 * TODO: We have to modify the implementation to support caching of resource configurations of
	 * EndPointReferences that do not have any EndPointIdentifier.
	 */
	public static int generateInodeNumber(EndpointReferenceType target)
	{
		WSName name = new WSName(target);
		if (name.isValidWSName()) {
			URI endpointIdentifier = name.getEndpointIdentifier();
			int inodeNumber = endpointIdentifier.toString().hashCode();

			/*
			 * Updating or storing the resource configuration object in the cache. It is important
			 * to update the cache here as we do not expect to regenerate the INode number again as
			 * long as our concerned resource is already in the cache.
			 */
			WSResourceConfig config = (WSResourceConfig) CacheManager.getItemFromCache(endpointIdentifier,
				WSResourceConfig.class);
			if (config == null) {
				config = new WSResourceConfig(name);
			}
			config.setInodeNumber(inodeNumber);
			CacheManager.putItemInCache(endpointIdentifier, config);

			return inodeNumber;
		} else {
			_logger.warn("Trying to generate an INode number of a target which"
				+ "does not implement the WS-Naming specification.");
			try {
				byte[] array = EPRUtils.toBytes(target);
				long result = 0;
				for (byte d : array) {
					result ^= d;
				}
				return (int) result;
			} catch (ResourceException re) {
				_logger.fatal("Unexpected error while trying to serialize EPR.", re);
				throw new RuntimeException(re);
			}
		}
	}

	/*
	 * This is the bridge between FUSE and RP-level cache. Here, we construct a file system INode
	 * structure from information stored in the resource configuration and the RP-attribute caches.
	 * The method returns null if any required attribute is found missing; in that case an RPC is
	 * initiated by the caller to retrieve the missing piece information, and INode is constructed
	 * in a normal fashion without the supervision of MetadataManager.
	 */
	public static FilesystemStatStructure retrieveStat(String rnsPathString)
	{
		WSResourceConfig config = (WSResourceConfig) CacheManager.getItemFromCache(rnsPathString, WSResourceConfig.class);
		if (config == null)
			return null;
		Integer inodeNumber = config.getInodeNumber();
		if (inodeNumber == null)
			return null;

		URI wsIdentifier = config.getWsIdentifier();
		MessageElement permissionElement = getAttributeFromCache(wsIdentifier, GenesisIIBaseRP.PERMISSIONS_STRING_QNAME);
		if (permissionElement == null)
			return null;

		FilesystemEntryType type = (config.isDirectory()) ? FilesystemEntryType.DIRECTORY : FilesystemEntryType.FILE;
		Permissions permissions = null;
		String name = getNameFromPath(rnsPathString);
		Long size = null;
		Long created, modified, accessed;

		try {
			permissions = permissionTranslater.deserialize(Permissions.class, permissionElement);
			if (config.isDirectory()) {
				size = 0L;
				created = 0L;
				modified = accessed = System.currentTimeMillis();
			} else {
				size = (Long) getDeserializedAttributeFromCache(Long.class, wsIdentifier, ByteIOConstants.rsize,
					ByteIOConstants.ssize);
				if (size == null)
					return null;

				Calendar createTime = (Calendar) getDeserializedAttributeFromCache(Calendar.class, wsIdentifier,
					ByteIOConstants.rcreatTime, ByteIOConstants.screatTime);
				if (createTime == null)
					return null;
				created = createTime.getTimeInMillis();

				Calendar modificationTime = (Calendar) getDeserializedAttributeFromCache(Calendar.class, wsIdentifier,
					ByteIOConstants.rmodTime, ByteIOConstants.smodTime);
				if (modificationTime == null)
					return null;
				modified = modificationTime.getTimeInMillis();

				Calendar accessTime = (Calendar) getDeserializedAttributeFromCache(Calendar.class, wsIdentifier,
					ByteIOConstants.raccessTime, ByteIOConstants.saccessTime);
				if (accessTime == null)
					return null;
				accessed = accessTime.getTimeInMillis();
			}
			return new FilesystemStatStructure(inodeNumber, name, type, size, created, modified, accessed, permissions);
		} catch (Exception e) {
			_logger.info("failed to generate stat from cached information", e);
		}
		return null;
	}

	public static FilesystemStatStructure retrieveStat(String rnsPathString, EndpointReferenceType EPR)
	{
		WSName wsName = new WSName(EPR);
		if (wsName.isValidWSName()) {
			WSResourceConfig resourceConfig = new WSResourceConfig(wsName, rnsPathString);
			URI wsIdentifier = wsName.getEndpointIdentifier();
			CacheManager.putItemInCache(wsIdentifier, resourceConfig);
		}
		return retrieveStat(rnsPathString);
	}

	private static QName getSizeAttributeQName(EndpointReferenceType EPR)
	{
		String namespace = CacheUtils.getNamespaceForByteIOAttributes(EPR);
		return (namespace == null) ? null : new QName(namespace, ByteIOConstants.SIZE_ATTR_NAME);
	}

	private static MessageElement getAttributeFromCache(URI wsIdentifier, QName... names)
	{
		for (QName name : names) {
			MessageElement element = (MessageElement) CacheManager.getItemFromCache(wsIdentifier, name, MessageElement.class);
			if (element != null)
				return element;
		}
		return null;
	}

	private static Object getDeserializedAttributeFromCache(Class<?> attributeType, URI wsIdentifier, QName... names)
		throws Exception
	{
		MessageElement element = getAttributeFromCache(wsIdentifier, names);
		if (element == null)
			return null;
		return translator.deserialize(attributeType, element);
	}

	/*
	 * This method is adequate for Unix, but not for Windows. If in future we managed to make FUSE
	 * works for Windows, we will have to change the implementation.
	 */
	private static String getNameFromPath(String path)
	{
		if (path == null)
			return "/";
		String trimmedPath = path.trim();
		if (trimmedPath.length() == 1)
			return "/";
		int indexOfLastSeperator = path.lastIndexOf('/');
		return trimmedPath.substring(indexOfLastSeperator + 1);
	}
}
