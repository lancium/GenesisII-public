package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.Map;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.genii.client.fuse.DirectoryManager;
import edu.virginia.vcgr.genii.client.fuse.MetadataManager;
import edu.virginia.vcgr.genii.client.fuse.UnixDirectory;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import fuse.FuseDirEnt;

/*
 * RNSNotificationHandler is originally written to update the RNS-lookup cache for both local and remote 
 * changes on RNS entries. For local changes the various pipelined processors directly call the appropriate 
 * methods of this handler class. Meanwhile, for remote changes the handler used to interpret the 
 * notification messages and take the appropriate actions. Currently, we are not incorporating remote 
 * change handling within the client-side cache. Therefore, we have removed all methods that were involved 
 * in notification message handling.
 * */
public class RNSNotificationHandler {

	public static void updateLookupAndDirectoryCacheAfterEntryAddition(EndpointReferenceType target, 
			EndpointReferenceType newEntry, String entryName) {
		
		WSName targetResourceName = new WSName(target);
		if (!targetResourceName.isValidWSName()) return;
		
		URI wsIdentifier = targetResourceName.getEndpointIdentifier();
		WSResourceConfig publisherConfig = (WSResourceConfig) CacheManager.getItemFromCache(
					wsIdentifier, WSResourceConfig.class);
		if (publisherConfig == null) return;

		// Although this is a loop, in typical cases we will find only one RNSPath for the publisher.
		for (String rnsPathOfPublisher : publisherConfig.getRnsPaths()) {
			
			// Store the new entry in the RNS Lookup cache.
			String pathForEntry = DirectoryManager.getPathForDirectoryEntry(rnsPathOfPublisher, entryName);
			CacheManager.putItemInCache(pathForEntry, newEntry);
			
			// Create a new resource configuration object for the entry and cache it.
			WSName childWSName = new WSName(newEntry);
			if (childWSName.isValidWSName()) {
				WSResourceConfig childConfig = new WSResourceConfig(childWSName, pathForEntry);
				CacheManager.putItemInCache(childWSName.getEndpointIdentifier(), childConfig);
			}
			
			// Update the cached directory to reflect the addition of new a entry
			updateDirectoryCacheAfterAddition(newEntry, rnsPathOfPublisher, pathForEntry);
		}
	}

	private static void updateDirectoryCacheAfterAddition(EndpointReferenceType newEntry, 
			String rnsPathOfPublisher, String pathForEntry) {
		
		UnixDirectory parentDirectory = (UnixDirectory) CacheManager.getItemFromCache(rnsPathOfPublisher, UnixDirectory.class);
		
		if (parentDirectory != null) {
			// MetadataManager tries to construct the STAT from cached information. This ensures
			// that we are not issuing new RPCs while updating the Unix directory cache.
			FilesystemStatStructure statOfChild = MetadataManager.retrieveStat(pathForEntry, newEntry);
			
			if (statOfChild != null) {
				parentDirectory.addEntry(DirectoryManager.createDirEntry(statOfChild));
				CacheManager.putItemInCache(rnsPathOfPublisher, parentDirectory);
			} else {
				// Since we could not update the directory with the received information, we are
				// removing directory from the cache.
				CacheManager.removeItemFromCache(rnsPathOfPublisher, UnixDirectory.class); 
			}
		}
	}

	public static void updateCacheAfterEntryRemoval(EndpointReferenceType target, String[] removedEntries) {
		
		WSName targetResourceName = new WSName(target);
		if (!targetResourceName.isValidWSName()) return;
		
		URI wsIdentifier = targetResourceName.getEndpointIdentifier();
		WSResourceConfig publisherConfig = (WSResourceConfig) CacheManager.getItemFromCache(wsIdentifier, WSResourceConfig.class);
		if (publisherConfig == null) return;

		// Although this is a loop, in typical cases we will find only one RNSPath for the publisher.
		for (String rnsPathOfPublisher : publisherConfig.getRnsPaths()) {
			
			// update the cached directory to reflect the removal of entries 
			UnixDirectory publisherDirectory = 
				(UnixDirectory) CacheManager.getItemFromCache(rnsPathOfPublisher, UnixDirectory.class);
			if (publisherDirectory != null) {
				for (String affectedEntry : removedEntries) {
					publisherDirectory.removeEntry(affectedEntry);
				}
				CacheManager.putItemInCache(rnsPathOfPublisher, publisherDirectory);
			}

			// For each deleted entry remove all attributes; cached EPR; Unix directory, if it is a directory; 
			// and, finally, remove resource configuration.
			for (String affectedEntry : removedEntries) {
				
				String pathForEntry = DirectoryManager.getPathForDirectoryEntry(rnsPathOfPublisher, affectedEntry);
				CacheManager.removeItemFromCache(pathForEntry, EndpointReferenceType.class);
				CacheManager.removeItemFromCache(pathForEntry, UnixDirectory.class);

				// Remove the resource configuration and attributes only when the removed entry is not accessible through
				// another RNS path.
				WSResourceConfig entryConfig = 
					(WSResourceConfig) CacheManager.getItemFromCache(pathForEntry, WSResourceConfig.class);
				if (entryConfig != null) {
					if (entryConfig.isMappedToMultiplePath()) {

						// When the removed entry is mapped to multiple RNS paths the most likely case is, it has been moved
						// from one directory to another directory. As we simulate move across directories as first an add 
						// in the target directory and then a remove in the source, we have reached this point. Now we have
						// to update the RNS path of the moved entry as well as the RNS paths of its all descendant resources.
						entryConfig.removeRNSPath(pathForEntry);
						CacheManager.putItemInCache(entryConfig.getWsIdentifier(), entryConfig);
						
						for (String otherEntryPath : entryConfig.getRnsPaths()) {
							String rnsPathOfCurrentParent = DirectoryManager.getParentPath(otherEntryPath);
							String entryNameInParent = DirectoryManager.getEntryName(otherEntryPath);
							String entryNameInPublisherDirectory = affectedEntry;
							renameDescendantsOfRenamedEntry(rnsPathOfPublisher, entryNameInPublisherDirectory, 
									entryNameInParent, rnsPathOfCurrentParent);
						}
					} else {
						// When the entry was not mapped to multiple paths, we always have a normal remove operation. 
						removeAllAttributesOfRemovedEntry(pathForEntry);
						CacheManager.removeItemFromCache(pathForEntry, WSResourceConfig.class);
						removeDescendantsOfRemovedEntry(pathForEntry);
					} 
				}
			} 
		}
	}

	public static void updateElementCountAttribute(EndpointReferenceType target, int countChange) {
		WSName targetResourceName = new WSName(target);
		if (!targetResourceName.isValidWSName()) return;
		
		MessageElement element = (MessageElement) CacheManager.getItemFromCache(
				targetResourceName, RNSConstants.ELEMENT_COUNT_QNAME, MessageElement.class);
		if (element == null) return;
		int elementCount = Integer.parseInt(element.getValue());
		int newCount = elementCount + countChange;
		MessageElement newCountElement = 
			new MessageElement(RNSConstants.ELEMENT_COUNT_QNAME, newCount);
		CacheManager.putItemInCache(targetResourceName, 
				RNSConstants.ELEMENT_COUNT_QNAME, newCountElement);
	}
	
	public static void updateCacheAfterEntryRename(EndpointReferenceType target, String oldEntryName, String newEntryName) {
		
		WSName targetResourceName = new WSName(target);
		if (!targetResourceName.isValidWSName()) return;
		
		URI wsIdentifier = targetResourceName.getEndpointIdentifier();
		WSResourceConfig targetConfig = (WSResourceConfig) CacheManager.getItemFromCache(wsIdentifier, WSResourceConfig.class);
		if (targetConfig == null) return;
		
		// Although this is a loop, in typical cases we will find only one RNSPath for the publisher.
		for (String rnsPathOfTarget : targetConfig.getRnsPaths()) {
			
			// change in the name of the directory entry within the target directory 
			UnixDirectory targetDirectory = 
				(UnixDirectory) CacheManager.getItemFromCache(rnsPathOfTarget, UnixDirectory.class);
			if (targetDirectory != null) {
				FuseDirEnt entry = targetDirectory.getEntry(oldEntryName);
				entry.name = newEntryName;
				CacheManager.putItemInCache(rnsPathOfTarget, targetDirectory);
			}
			
			String oldEntryPath = DirectoryManager.getPathForDirectoryEntry(rnsPathOfTarget, oldEntryName);
			final String newEntryPath = DirectoryManager.getPathForDirectoryEntry(rnsPathOfTarget, newEntryName);

			// update the RNSPath list of the renamed entry.
			WSResourceConfig entryConfig = (WSResourceConfig) CacheManager.getItemFromCache(oldEntryPath, WSResourceConfig.class);
			if (entryConfig != null) {
				entryConfig.removeRNSPath(oldEntryPath);
				entryConfig.addRNSPath(newEntryPath);
				CacheManager.putItemInCache(entryConfig.getWsIdentifier(), entryConfig);
			}
			
			// store the EPR for entry under the new RNSPath name
			EndpointReferenceType entryEPR = 
				(EndpointReferenceType) CacheManager.getItemFromCache(oldEntryPath, EndpointReferenceType.class);
			if (entryEPR != null) {
				CacheManager.removeItemFromCache(oldEntryPath, EndpointReferenceType.class);
				CacheManager.putItemInCache(newEntryPath, entryEPR);
			}
			
			// rename the descendant resources in case the renamed entry is a directory
			renameDescendantsOfRenamedEntry(rnsPathOfTarget, oldEntryName, newEntryName, null);
		}
	}
	
	
	private static void removeAllAttributesOfRemovedEntry(String pathForEntry) {
		URI entryURI = null;
		EndpointReferenceType entryEPR = (EndpointReferenceType) CacheManager.getItemFromCache(
				pathForEntry, EndpointReferenceType.class);
		if (entryEPR != null) {
			entryURI = CacheUtils.getEPI(entryEPR);
		} else {
			WSResourceConfig entryConfig = (WSResourceConfig) CacheManager.getItemFromCache(
					pathForEntry, WSResourceConfig.class);
			if (entryConfig != null) {
				entryURI = entryConfig.getWsIdentifier();
			}
		}
		if (entryURI != null) {
			CacheManager.removeAllRelevantInfoFromCache(entryURI, MessageElement.class);
		}
	}
	
	/*
	 * After a directory rename, all elements that are within the RNS namespace rooted under the renamed directory get 
	 * invalid path reference. This method find all those descendant elements and update their RNS paths to reflected 
	 * the name change. When the parameter 'targetParentDir' is null it is assumed the rename was done within a single
	 * directory (RNS rename operation), and across directories otherwise (a rename simulated by an add followed by a 
	 * remove). 
	 * */
	@SuppressWarnings("unchecked")
	private static void renameDescendantsOfRenamedEntry(String srcParentDir, String nameInSourceDir, 
			String nameInTargetDir, String targetParentDir) {
		
		String oldPathForEntry = DirectoryManager.getPathForDirectoryEntry(srcParentDir, nameInSourceDir);
		String descendantPaths = DirectoryManager.getPathForDirectoryEntry(oldPathForEntry, ".+");
		String newPathForEntry = DirectoryManager.getPathForDirectoryEntry(
				(targetParentDir == null) ? srcParentDir : targetParentDir, nameInTargetDir);
		
		Map<String, EndpointReferenceType> matchingEPRs = 
			CacheManager.getMatchingItemsWithKeys(descendantPaths, EndpointReferenceType.class);
		if (matchingEPRs != null) {
			for (Map.Entry<String, EndpointReferenceType> entry : matchingEPRs.entrySet()) {
				String oldDescendantPath = entry.getKey();
				String newDescendantPath = getNewDescendantPathFromOld(oldPathForEntry, oldDescendantPath, newPathForEntry);
				CacheManager.removeItemFromCache(oldDescendantPath, EndpointReferenceType.class);
				CacheManager.putItemInCache(newDescendantPath, entry.getValue());
			}
		}
		
		Map<String, WSResourceConfig> matchingConfigs = 
			CacheManager.getMatchingItemsWithKeys(descendantPaths, WSResourceConfig.class);
		if (matchingConfigs != null) {
			for (Map.Entry<String, WSResourceConfig> entry : matchingConfigs.entrySet()) {
				WSResourceConfig descendant = entry.getValue();
				String oldDescendantPath = descendant.getMatchingPath(descendantPaths);
				String newDescendantPath = getNewDescendantPathFromOld(oldPathForEntry, oldDescendantPath, newPathForEntry);
				descendant.addRNSPath(newDescendantPath);
				descendant.removeRNSPath(oldDescendantPath);
				CacheManager.putItemInCache(descendant.getWsIdentifier(), descendant);
			}
		}
		
		Map<String, UnixDirectory> matchingDirectories = 
			CacheManager.getMatchingItemsWithKeys(descendantPaths, UnixDirectory.class);
		if (matchingDirectories != null) {
			for (Map.Entry<String, UnixDirectory> entry : matchingDirectories.entrySet()) {
				String oldDescendantPath = entry.getKey();
				String newDescendantPath = getNewDescendantPathFromOld(oldPathForEntry, oldDescendantPath, newPathForEntry);
				CacheManager.removeItemFromCache(oldDescendantPath, UnixDirectory.class);
				CacheManager.putItemInCache(newDescendantPath, entry.getValue());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void removeDescendantsOfRemovedEntry(String pathOfRemovedEntry) {
		
		String descendantPaths = DirectoryManager.getPathForDirectoryEntry(pathOfRemovedEntry, ".+");

		Map<String, EndpointReferenceType> matchingEPRs = 
			CacheManager.getMatchingItemsWithKeys(descendantPaths, EndpointReferenceType.class);
		if (matchingEPRs != null) {
			for (Map.Entry<String, EndpointReferenceType> entry : matchingEPRs.entrySet()) {
				CacheManager.removeItemFromCache(entry.getKey(), EndpointReferenceType.class);
			}
		}
		
		Map<String, WSResourceConfig> matchingConfigs = 
			CacheManager.getMatchingItemsWithKeys(descendantPaths, WSResourceConfig.class);
		if (matchingConfigs != null) {
			for (Map.Entry<String, WSResourceConfig> entry : matchingConfigs.entrySet()) {
				WSResourceConfig descendant = entry.getValue();
				CacheManager.removeItemFromCache(descendant.getWsIdentifier(), WSResourceConfig.class);
			}
		}
		
		Map<String, UnixDirectory> matchingDirectories = 
			CacheManager.getMatchingItemsWithKeys(descendantPaths, UnixDirectory.class);
		if (matchingDirectories != null) {
			for (Map.Entry<String, UnixDirectory> entry : matchingDirectories.entrySet()) {
				CacheManager.removeItemFromCache(entry.getKey(), UnixDirectory.class);
			}
		}
	}

	private static String getNewDescendantPathFromOld(String oldPathForEntry, String oldDescendantPath, String newPathForEntry) {
		int oldEntryNameEndsAt = oldPathForEntry.length();
		String descendantPathAfterOldEntryName = oldDescendantPath.substring(oldEntryNameEndsAt + 1);
		return DirectoryManager.getPathForDirectoryEntry(newPathForEntry, descendantPathAfterOldEntryName);
	}
}
