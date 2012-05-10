package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSMetadataType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.fuse.DirectoryManager;
import edu.virginia.vcgr.genii.client.naming.WSName;

/*
 * This class gets an RNSEntryResponse and extracts from it all cache-able attributes. 
 * */
public class RNSEntryResponseTranslator implements CacheableItemsGenerator {

	@Override
	public boolean isSupported(Class<?>... argumentTypes) {
		if (argumentTypes.length == 1) {
			return RNSEntryResponseType.class.equals(argumentTypes[0]);
		} else if (argumentTypes.length == 2) {
			return (WSResourceConfig.class.equals(argumentTypes[0]) 
					&& RNSEntryResponseType.class.equals(argumentTypes[1]));
		}
		return false;
	}
	
	@Override
	public Collection<CacheableItem> generateItems(Object... originalItems) {
		
		List<CacheableItem> itemList = new ArrayList<CacheableItem>();
		RNSEntryResponseType rnsEntry;
		EndpointReferenceType entryEPR;
		WSResourceConfig entryConfig = null;
		
		if (originalItems.length == 1) {
			rnsEntry = (RNSEntryResponseType) originalItems[0];
			entryEPR = rnsEntry.getEndpoint();
		} else {
			rnsEntry = (RNSEntryResponseType) originalItems[1];
			entryEPR = rnsEntry.getEndpoint();
		}
		
		WSName wsName = new WSName(entryEPR);
		if (wsName.isValidWSName()) {
			entryConfig = new WSResourceConfig(wsName);
		}

		if (originalItems.length == 2) {
			WSResourceConfig parentDirectoryConfig = (WSResourceConfig) originalItems[0];
			if (parentDirectoryConfig.isMappedToRNSPaths()) {
				String entryName = rnsEntry.getEntryName();
				for (String parentRnsPath : parentDirectoryConfig.getRnsPaths()) {
					String childRNSPath = DirectoryManager.getPathForDirectoryEntry(parentRnsPath, entryName);
					CacheableItem eprItem = new CacheableItem();
					eprItem.setKey(childRNSPath);
					eprItem.setValue(entryEPR);
					itemList.add(eprItem);
					if (entryConfig != null) entryConfig.addRNSPath(childRNSPath);
				}
			}
		}
		
		if (entryConfig != null) {
			CacheableItem resourceConfigItem = new CacheableItem();
			resourceConfigItem.setKey(entryConfig.getWsIdentifier());
			resourceConfigItem.setValue(entryConfig);
			itemList.add(resourceConfigItem);
		}
		
		RNSMetadataType metadataType = rnsEntry.getMetadata();
		
		if (metadataType != null && metadataType.get_any() != null) {
			for (MessageElement element : metadataType.get_any()) {
				CacheableItem item = new CacheableItem();
				item.setKey(element.getQName());
				item.setTarget(entryEPR);
				item.setValue(element);
				itemList.add(item);
			}
		}
		return itemList;
	}
}
