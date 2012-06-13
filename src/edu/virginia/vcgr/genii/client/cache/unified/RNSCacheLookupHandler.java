package edu.virginia.vcgr.genii.client.cache.unified;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.RNSEntryResponseType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.ResourceAccessMonitor;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.fuse.DirectoryManager;
import edu.virginia.vcgr.genii.client.fuse.GeniiFuseMount;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIFilesystem;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;

/*
 * Handler class for returning a response of RNS lookup call from the cache instead of making an 
 * RPC on the related container. Note that this should not be used unless we subscribe the EPR 
 * on which the lookup operation is invoked. This is because the resulting entries of the lookup
 * call will be again inserted by the the caller in the cache, which will unwontedly update the
 * cache lifetime of the entries. A subscription on the target EPR ensures that lookup entries 
 * are always valid so we can safely increase the cache lifetime of the component RNS entries.
 * */

public class RNSCacheLookupHandler {
	
	static private Log _logger = LogFactory.getLog(RNSCacheLookupHandler.class);

	@SuppressWarnings("unchecked")
	public static LookupResponseType getCachedLookupResponse(EndpointReferenceType target, String filteredNames[]) {
		try {
			
			WSName wsName = new WSName(target);
			if (!wsName.isValidWSName()) return null;

			WSResourceConfig resourceConfig = (WSResourceConfig) CacheManager
					.getItemFromCache(wsName.getEndpointIdentifier(), WSResourceConfig.class);
			if (resourceConfig == null) {
				// handling the current RNS path separately. 
				RNSPath currentPath = ContextManager.getCurrentContext().getCurrentPath();
				EndpointReferenceType currentPathEpr = currentPath.getEndpoint();
				WSName currentPathWsName = new WSName(currentPathEpr);
				if (currentPathWsName.equals(wsName)) {
					WSResourceConfig currentPathConfig = new WSResourceConfig(currentPathWsName, currentPath.pwd());
					URI wsEndpointIdentifier = currentPathWsName.getEndpointIdentifier();
					CacheManager.putItemInCache(wsEndpointIdentifier, currentPathConfig);
					resourceConfig = currentPathConfig;
				} else {
					return null;
				}
			}
			
			// this is a safety checking as cache access may be blocked and we got the original null 
			// return for the resource configuration object because of that, instead of absence of the 
			// object in the cache.
			resourceConfig = (WSResourceConfig) CacheManager
					.getItemFromCache(wsName.getEndpointIdentifier(), WSResourceConfig.class);
			if (resourceConfig == null) return null;
			 
			if (resourceConfig.isCacheAccessBlocked()) return null;

			MessageElement element = (MessageElement) CacheManager
					.getItemFromCache(wsName, RNSConstants.ELEMENT_COUNT_QNAME, MessageElement.class);
			if (element == null) {
				processHotspot(resourceConfig, target);
				return null;
			}

			int elementCount = Integer.parseInt(element.getValue());
			if (elementCount == 0) return new LookupResponseType();

			Map<String, EndpointReferenceType> matchings = null;
			List<RNSEntryResponseType> entries = new ArrayList<RNSEntryResponseType>();
			for (String targetPath : resourceConfig.getRnsPaths()) {
				String childPaths = DirectoryManager.getPathForDirectoryEntry(targetPath, "[^//]+");
				matchings = CacheManager.getMatchingItemsWithKeys(childPaths, EndpointReferenceType.class);
				if (matchings != null) {
					addMatchingEntriesInMap(entries, matchings);
				}
			}
			
			if (filteredNames == null || filteredNames.length == 0) {
				if (entries.size() != elementCount) {
					// There is a mismatch between cached element count and actual number of child entries in cache. This
					// can happen during concurrent update in same RNS directory and if no element listing has been done on
					// target previously. Regardless of the cause, if the target has been subscribed for notifications we 
					// should keep the element count property up-to-date.
					processHotspot(resourceConfig, target);
					
					// In case there are stale entries in the cache that are not updated/removed because of some notification
					// blockage, we are removing all entry EPRS from the cache too. 
					removePossiblyStaleEntries(matchings);
					
					return null;
				}
				// When the call is initiated from FUSE, most of the time it will be succeeded by a bunch o get-attributes
				// call for the entries. If those attributes are not in cache then an RPC will be issued for each entry
				// missing attributes. As when looking up entries from the container we can prefetch attributes for entries
				// that are in the same container as the target, sometimes it is better to let the lookup RPC to pass than
				// satisfying request from the cache. The following IF condition does that assessment.
				if (isCallMadeFromFuse() && !isCacheContainsAllEntryAttributes(resourceConfig, entries)) return null;
			} else {
				int totalCachedEntries = entries.size();
				filterEntries(entries, filteredNames);
				// This is not always right as the caller can pass an entry name that does not
				// exists in the RNS directory. We can't cover all the cases where such scenarios
				// can spur. However the checking of total-cached-entry counts will save us in 
				// cases where the client has made a directory listing for all elements in the 
				// RNS directory before making the vein lookup call.
				if ((filteredNames.length != entries.size()) && (totalCachedEntries < elementCount)) {
					return null;
				}
			}
			
			LookupResponseType lookupResponseType = new LookupResponseType(
					entries.toArray(new RNSEntryResponseType[entries.size()]), null);
			return lookupResponseType;

		} catch (Exception ex) {
			_logger.debug("Exception occurred while looking up the cache", ex);
		}
		return null;
	}

	private static void addMatchingEntriesInMap(List<RNSEntryResponseType> entries,
			Map<String, EndpointReferenceType> matchingEntries) {
		for (Map.Entry<String, EndpointReferenceType> entry : matchingEntries
				.entrySet()) {
			String entryRNSPath = entry.getKey();
			int indexOfLastSeperator = entryRNSPath.lastIndexOf('/');
			String entryName = entryRNSPath.substring(indexOfLastSeperator + 1);
			RNSEntryResponseType response = new RNSEntryResponseType(
					entry.getValue(), null, null, entryName);
			entries.add(response);
		}
	}

	private static void filterEntries(List<RNSEntryResponseType> entries, String[] filteredNames) {

		if (filteredNames == null || filteredNames.length == 0)
			return;

		Set<String> filterSet = new HashSet<String>(Arrays.asList(filteredNames));
		Iterator<RNSEntryResponseType> iterator = entries.iterator();
		while (iterator.hasNext()) {
			RNSEntryResponseType response = iterator.next();
			if (!filterSet.contains(response.getEntryName())) {
				iterator.remove();
			}
		}
	}
	
	/*
	 * Some RNS resource has been subscribed means the client is interested in its contents, that in turn of the time
	 * should be available in the client-cache, therefore we are fetching the element-count property to later on to 
	 * be able to satisfy lookup requests from the cache.
	 * */
	private static void processHotspot(WSResourceConfig config, EndpointReferenceType target) {
		if (!config.isHasRegisteredCallback()) return;
		ICallingContext currentContext;
		try {
			currentContext = ContextManager.getCurrentContext();
			ElementCountPropertyRetriever retriever = new ElementCountPropertyRetriever(target, currentContext);
			retriever.start();
		} catch (Exception e) {
			_logger.info("could not retrieve calling context information", e);
		} 
	}
	
	/*
	 * If a target RNS directory was blocked because of rapid updates by some other user, there is a 
	 * chance that current user's cache has already removed entries. The existence of deleted entries
	 * in the cache will cause continuous cache failure as the element-count of the target will 
	 * always have a smaller value than the number of entries in the cache. To avoid this problem,
	 * this method is used to remove the cached EPRs of contents of the target RNS directory.
	 * */
	private static void removePossiblyStaleEntries(Map<String, EndpointReferenceType> matchedEntries) {
		
		if (matchedEntries == null || matchedEntries.isEmpty()) return;
		
		for (String entryPath : matchedEntries.keySet()) {
			CacheManager.removeItemFromCache(entryPath, EndpointReferenceType.class);
		}
	}
	
	/*
	 * This is the thread for asynchronously retrieving elementCount property from
	 * RNS directories that are hot-spots of lookup operation.
	 * */
	private static class ElementCountPropertyRetriever extends Thread {
		
		private EndpointReferenceType endpoint;
		private ICallingContext callingContext;

		public ElementCountPropertyRetriever(EndpointReferenceType endpoint, 
				ICallingContext callingContext) {
			this.endpoint = endpoint;
			this.callingContext = callingContext;
		}

		@Override
		public void run() {
			
			// Every cache management related thread that load or store information from the Cache should have 
			// unaccounted access to both CachedManager and RPCs to avoid getting mingled with Cache access and 
			// RPCs initiated by some user action. This is important to provide accurate statistics on per container
			// resource usage.
			ResourceAccessMonitor.getUnaccountedAccessRight();
			
			try {
				Closeable assumedContext = 
					ContextManager.temporarilyAssumeContext(callingContext);
				EnhancedRNSPortType portType = 
					ClientUtils.createProxy(EnhancedRNSPortType.class, endpoint);
				portType.getResourceProperty(RNSConstants.ELEMENT_COUNT_QNAME);
				assumedContext.close();
			} catch (Exception ex) {
				//ignore
			}
		}
	}
	
	/*
	 * Determine whether or not a lookup request is initiated from FUSE by examining the call-stack.
	 * This information is subsequently used to assess the effectiveness returning a lookup result from 
	 * client-cache.
	 * */
	private static boolean isCallMadeFromFuse() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement traceElement : stackTrace) {
			String className = traceElement.getClassName();
			if (GenesisIIFilesystem.class.getName().equals(className) 
					|| GeniiFuseMount.class.getName().equals(className)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isCacheContainsAllEntryAttributes(WSResourceConfig targetConfig, List<RNSEntryResponseType> entries) {

		String targetContainerId = targetConfig.getContainerId();
		if (targetContainerId == null) return true;

		boolean attributesMissingFromResourcesOfSameContainer = false;

		for (RNSEntryResponseType entry : entries) {
			URI entryWSIndentifier = CacheUtils.getEPI(entry.getEndpoint());
			WSResourceConfig entryConfig = 
				(WSResourceConfig) CacheManager.getItemFromCache(entryWSIndentifier, WSResourceConfig.class);
			if (entryConfig == null) continue;
			
			String entryContainerId = entryConfig.getContainerId();
			if (targetContainerId.equals(entryContainerId)) {
				// A checking on permissions-string attribute has been made because it is applicable for both RNS and ByteIO.
				// A thorough testing on all attributes will be an overkill.
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
}
