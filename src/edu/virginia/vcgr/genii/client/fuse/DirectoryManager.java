package edu.virginia.vcgr.genii.client.fuse;

import java.util.Collection;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fsii.FilesystemEntryType;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.exceptions.FSRuntimeException;
import edu.virginia.vcgr.fsii.path.UnixFilesystemPathRepresentation;
import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.gfs.FSExceptions;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIFilesystem;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import fuse.FuseDirEnt;

/*
 * This is a helper class for propagating local changes in cached directories and retrieving cached directories. 
 * Because of the unavoidable disparate nature of accessing and manipulating directory related information we 
 * introduced this helper class. It encapsulates the details of Unix directory level cache management from the 
 * codes that handles access and modification of information relevant to directory entries.
 * */
public class DirectoryManager
{

	static private Log _logger = LogFactory.getLog(DirectoryManager.class);

	public static FuseDirEnt[] getDir(String path)
	{
		UnixDirectory directory = (UnixDirectory) CacheManager.getItemFromCache(path, UnixDirectory.class);
		if (directory != null && isCacheContainNecessaryDirectoryInfo(path, directory)) {
			List<FuseDirEnt> dirEntries = directory.getEntries();
			return dirEntries.toArray(new FuseDirEnt[0]);
		}
		return null;
	}

	public static void createDir(String path, Collection<FuseDirEnt> entries)
	{
		UnixDirectory directory = new UnixDirectory(path, entries);
		CacheManager.putItemInCache(path, directory);
	}

	public static void addNewDirEntry(RNSPath path, GenesisIIFilesystem fs, boolean entryCreated)
	{
		String localPath = path.pwd();
		String parentPath = getParentPath(localPath);
		UnixDirectory directory = (UnixDirectory) CacheManager.getItemFromCache(parentPath, UnixDirectory.class);
		if (directory != null) {
			try {
				// In general cases the STAT call should not incur new RPC calls because
				// the results of these calls are already cached when update attribute
				// methods were invoked on related RP interfaces.
				FilesystemStatStructure structure = fs.stat(UnixFilesystemPathRepresentation.INSTANCE.parse(null, localPath));
				FuseDirEnt entry = createDirEntry(structure);
				directory.addEntry(entry);
				CacheManager.putItemInCache(null, parentPath, directory);
				FilesystemEntryType entryType = structure.getEntryType();

				// When creating a new directory we add a new item in the cache to avoid the
				// subsequent, and highly probable getDirectory calls on the created directory.
				if (entryCreated && entryType == FilesystemEntryType.DIRECTORY) {
					UnixDirectory newDirectory = new UnixDirectory(localPath, null);
					CacheManager.putItemInCache(localPath, newDirectory);
				}
			} catch (FSException e) {
				throw new FSRuntimeException(FSExceptions.translate("Unable to stat entry.", e));
			}
		}
		updateResourceConfigCache(path, true);
	}

	public static void removeDirEntry(RNSPath path)
	{
		String parentPath = getParentPath(path.pwd());
		UnixDirectory directory = (UnixDirectory) CacheManager.getItemFromCache(parentPath, UnixDirectory.class);
		if (directory != null) {
			directory.removeEntry(path.getName());
			CacheManager.putItemInCache(parentPath, directory);
		}
		CacheManager.removeItemFromCache(path.pwd(), UnixDirectory.class);
		updateResourceConfigCache(path, false);
	}

	public static String getParentPath(String path)
	{
		String[] pathComponents = UnixFilesystemPathRepresentation.INSTANCE.parse(null, path);
		int length = pathComponents.length;
		if (length > 1) {
			String[] parentsPathComponents = new String[length - 1];
			System.arraycopy(pathComponents, 0, parentsPathComponents, 0, length - 1);
			return UnixFilesystemPathRepresentation.INSTANCE.toString(parentsPathComponents);
		}
		return UnixFilesystemPathRepresentation.INSTANCE.toString(null);
	}

	public static FuseDirEnt createDirEntry(FilesystemStatStructure struct)
	{
		FuseDirEnt entry = new FuseDirEnt();
		entry.inode = struct.getINode();
		entry.mode = MetadataManager.getMode(struct);
		entry.name = struct.getName();
		return entry;
	}

	public static String getPathForDirectoryEntry(String parentPath, String entryName)
	{
		if (parentPath.equals("/"))
			return parentPath + entryName;
		return parentPath + "/" + entryName;
	}

	/*
	 * Updating the resource configuration because of addition, removal, or movement of directory
	 * entries is not a required operation. This is done just to reflect the local updates in the
	 * resource configuration as soon as possible, which will subsequently help to reduce possible
	 * redundant lookup calls.
	 */
	private static void updateResourceConfigCache(RNSPath path, boolean pathAdded)
	{
		try {
			String rnsPathString = path.pwd();
			EndpointReferenceType endpoint = path.getEndpoint();
			WSName wsName = new WSName(endpoint);
			URI wsIdentifier = wsName.getEndpointIdentifier();
			WSResourceConfig config = (WSResourceConfig) CacheManager.getItemFromCache(wsIdentifier, WSResourceConfig.class);
			if (config == null && pathAdded) {
				config = new WSResourceConfig(wsName, rnsPathString);
				CacheManager.putItemInCache(wsIdentifier, config);
				return;
			}
			if (pathAdded) {
				config.addRNSPath(rnsPathString);
				CacheManager.putItemInCache(wsIdentifier, config);
			} else {
				config.removeRNSPath(rnsPathString);
				if (!config.isMappedToRNSPaths()) {
					CacheManager.removeAllRelevantInfoFromCache(wsIdentifier, WSResourceConfig.class);
				}
			}
		} catch (RNSPathDoesNotExistException e) {
			if (_logger.isTraceEnabled())
				_logger.trace("RNS path not found while updating resource configuration");
		}
	}

	/*
	 * This method is used to judge the effectiveness of using the local-cache for a directory
	 * lookup operation. As we prefetch attributes with lookup call, when attributes are absent in
	 * the cache but the directory entry is not, it is better to let the RPC to pass instead of
	 * returning result from the local-cache since at the end it will reduce the overall RPCs.
	 */
	private static boolean isCacheContainNecessaryDirectoryInfo(String directoryPath, UnixDirectory directory)
	{

		WSResourceConfig directoryConfig = (WSResourceConfig) CacheManager.getItemFromCache(directoryPath,
			WSResourceConfig.class);

		// We adhere to a pessimistic approach here. Additionally, since we track last time usage of
		// per-container
		// resources by tracking down when an EPR or a ResourceConfiguration has last been accessed,
		// it is safer to
		// be pessimistic to avoid collecting invalid statistics.
		if (directoryConfig == null)
			return false;
		String directoryContainerId = directoryConfig.getContainerId();
		if (directoryContainerId == null)
			return false;

		// If the number of elements in the directory does not match with the element-count property
		// of the RNS resource
		// then the cached directory is certainly stale. On the other hand, when the element-count
		// property is not in
		// the cache it is safer to assume that the cached directory is invalid. In both cases, an
		// RPC will be issued to
		// replenish the missing contents in the directory.
		MessageElement elementCountElement = (MessageElement) CacheManager.getItemFromCache(directoryConfig.getWsIdentifier(),
			RNSConstants.ELEMENT_COUNT_QNAME, MessageElement.class);
		if (elementCountElement == null)
			return false;
		int elementCount = Integer.parseInt(elementCountElement.getValue());
		if (elementCount != directory.getEntries().size())
			return false;

		boolean attributesMissingFromResourcesOfSameContainer = false;

		for (FuseDirEnt entry : directory.getEntries()) {
			String entryPath = getPathForDirectoryEntry(directoryPath, entry.name);
			WSResourceConfig entryConfig = (WSResourceConfig) CacheManager.getItemFromCache(entryPath, WSResourceConfig.class);
			if (entryConfig == null)
				continue;
			String entryContainerId = entryConfig.getContainerId();
			if (directoryContainerId.equals(entryContainerId)) {
				// Make a probabilistic judgment about the effectiveness of returning result from
				// cache
				// based on the availability of permissions-string attribute. This attribute is
				// chosen
				// because it is needed for both RNS and ByteIO.
				Object permissionProperty = CacheManager.getItemFromCache(entryConfig.getWsIdentifier(),
					GenesisIIBaseRP.PERMISSIONS_STRING_QNAME, MessageElement.class);
				if (permissionProperty == null) {
					attributesMissingFromResourcesOfSameContainer = true;
					break;
				}
			}
		}
		return !attributesMissingFromResourcesOfSameContainer;
	}

	public static String getEntryName(String entryPath)
	{
		String rootPath = UnixFilesystemPathRepresentation.INSTANCE.toString(null);
		if (rootPath.equals(entryPath))
			return "";
		String parentPath = getParentPath(entryPath);
		return entryPath.substring(parentPath.length() + 1);
	}
}
