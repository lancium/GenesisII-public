package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSMetadataType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.fuse.DirectoryManager;
import edu.virginia.vcgr.genii.client.rp.DefaultSingleResourcePropertyTranslator;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;

/*
 * This class gets an RNSEntryResponse and extracts from it all cache-able attributes.
 */
public class RNSEntryResponseTranslator implements CacheableItemsGenerator
{
	static private Log _logger = LogFactory.getLog(RNSEntryResponseTranslator.class);
	static private SingleResourcePropertyTranslator translator = new DefaultSingleResourcePropertyTranslator();

	@Override
	public boolean isSupported(Class<?>... argumentTypes)
	{
		if (argumentTypes.length == 1) {
			return RNSEntryResponseType.class.equals(argumentTypes[0]);
		} else if (argumentTypes.length == 2) {
			return (WSResourceConfig.class.equals(argumentTypes[0]) && RNSEntryResponseType.class.equals(argumentTypes[1]));
		}
		return false;
	}

	@Override
	public Collection<CacheableItem> generateItems(Object... originalItems)
	{
		List<CacheableItem> itemList = new ArrayList<CacheableItem>();
		RNSEntryResponseType rnsEntry;
		EndpointReferenceType entryEPR;
		WSResourceConfig entryConfig = null;

		// extract the EPR out of an RNS entry from the appropriate argument
		if (originalItems.length == 1) {
			rnsEntry = (RNSEntryResponseType) originalItems[0];
			entryEPR = rnsEntry.getEndpoint();
		} else {
			rnsEntry = (RNSEntryResponseType) originalItems[1];
			entryEPR = rnsEntry.getEndpoint();
		}

		entryConfig = WSResourceConfig.CreateResourceConfigFromLookupResponse(rnsEntry);

		// generate a cache element for storing the EPR
		if (originalItems.length == 2) {
			WSResourceConfig parentDirectoryConfig = (WSResourceConfig) originalItems[0];
			if (parentDirectoryConfig.isMappedToRNSPaths()) {
				String entryName = rnsEntry.getEntryName();
				for (String parentRnsPath : parentDirectoryConfig.getRnsPaths()) {
					String childRNSPath = DirectoryManager.getPathForDirectoryEntry(parentRnsPath, entryName);
					CacheableItem eprItem = new CacheableItem();
					eprItem.setKey(childRNSPath);
					eprItem.setValue(entryEPR);
					if (entryEPR != null) {
						itemList.add(eprItem);
					} else {
						if (_logger.isDebugEnabled())
							_logger.debug("ignoring RNSEntryResponse with null EPR, has path: " + childRNSPath);
					}
					if (entryConfig != null) {
						entryConfig.addRNSPath(childRNSPath);
					}
				}
			}
		}

		// generate a cache element for storing the resource-config object that binds other cache
		// entries
		if (entryConfig != null) {
			CacheableItem resourceConfigItem = new CacheableItem();
			resourceConfigItem.setKey(entryConfig.getWsIdentifier());
			resourceConfigItem.setValue(entryConfig);
			itemList.add(resourceConfigItem);
		}

		// generate cache elements for resource properties that come along as prefetched metadata
		RNSMetadataType metadataType = rnsEntry.getMetadata();
		if (metadataType != null && metadataType.get_any() != null) {
			Object cacheTarget = entryEPR;
			for (MessageElement element : metadataType.get_any()) {
				QName qName = element.getQName();
				if (GenesisIIConstants.RESOURCE_URI_QNAME.equals(qName)) {
					try {
						cacheTarget = translator.deserialize(URI.class, element);
					} catch (ResourcePropertyException e) {
						_logger.debug("Property translation error in URI: " + e.getMessage());
					}
				}
			}
			if (cacheTarget != null) {
				for (MessageElement element : metadataType.get_any()) {
					CacheableItem item = new CacheableItem();
					item.setKey(element.getQName());
					item.setTarget(cacheTarget);
					item.setValue(element);
					itemList.add(item);
				}
			}
		}
		return itemList;
	}
}
