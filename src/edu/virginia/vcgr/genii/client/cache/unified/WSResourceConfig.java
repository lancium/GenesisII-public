package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;

/*
 * This class maintains the mapping between the different views of a web services resource. 
 * This is needed because FUSE and Grid shell access the same resource using different 
 * identifiers. This is also very useful for refreshing/invalidating cached items in multiple
 * caches. As our caching philosophy is to minimize the duplication of or cached 
 * information we need this mapping to locate the components of cached resource that a 
 * particular client -- FUSE or Grid Shell -- is interested in. Finally, since this is a 
 * lightweight class and has multiple use cases the cache holding instances of this class
 * should have both a larger lifetime and capacity.
 * */
public class WSResourceConfig {

	public enum IdentifierType {WS_ENDPOINT_IDENTIFIER, RNS_PATH_IDENTIFIER, INODE_NUMBER_IDENTIFIER}
	
	private enum ResourceType {FILE, DIRECTORY}
	
	private URI wsIdentifier;
	private Set<String> rnsPaths;
	private Integer inodeNumber;
	private ResourceType type;
	
	/*
	 * These two properties indicate that the concerned resource will remain synchronized 
	 * -- using container to client update notifications or by polling status -- with the 
	 * container holding it for the specified period of time. This information is used to
	 * increase the lifetime of the related items in different caches. 
	 * */
	private boolean hasRegisteredCallback;
	private Date callbackExpiryTime;
	
	/*
	 * This two fields are used to check whether the container has notified the client not to 
	 * rely on cache for a specific period of time. The container does that when it receive too
	 * many notification to be delivered on behave of the concerned resource.
	 * */
	private boolean cacheAccessBlocked;
	private Date cacheBlockageExpiryTime;
	
	/*
	 * Represents in which GenesisII container the resource is located. For non-GenesisII resources
	 * this should be NULL.
	 * */
	private String containerId;
	
	public WSResourceConfig(WSName wsName) {
		this.wsIdentifier = wsName.getEndpointIdentifier();
		TypeInformation typeInfo = new TypeInformation(wsName.getEndpoint());
		if (typeInfo.isRNS()) {
			type = ResourceType.DIRECTORY;
		} else {
			type = ResourceType.FILE;
		}
		if (wsName.isValidWSName())  {
			inodeNumber = wsIdentifier.toString().hashCode();
		}
		rnsPaths = new HashSet<String>(3);
		containerId = CacheUtils.getContainerId(wsName); 
	}
	
	public WSResourceConfig(WSName wsName, String rnsPath) {
		this(wsName);
		rnsPaths.add(rnsPath);
	}
	
	public boolean isMappedToMultiplePath() {
		return (rnsPaths.size() > 1);
	}

	public URI getWsIdentifier() {
		return wsIdentifier;
	}

	public void setWsIdentifier(URI wsIdentifier) {
		this.wsIdentifier = wsIdentifier;
	}

	public String getRnsPath() {
		if (rnsPaths == null || rnsPaths.isEmpty()) return null;
		return rnsPaths.toArray(new String[rnsPaths.size()])[0];
	}

	public Collection<String> getRnsPaths() {
		return rnsPaths;
	}
	
	public void addRNSPath(String rnsPath) {
		rnsPaths.add(rnsPath);
	}
	
	public void addRNSPaths(Collection<String> rnsPaths) {
		if (rnsPaths != null) {
			this.rnsPaths.addAll(rnsPaths);
		}
	}
	
	public void removeRNSPath(String rnsPath) {
		rnsPaths.remove(rnsPath);
	}

	public Integer getInodeNumber() {
		return inodeNumber;
	}

	public void setInodeNumber(Integer inodeNumber) {
		this.inodeNumber = inodeNumber;
	}
	
	public boolean isHasRegisteredCallback() {
		
		// A posterior update on the flag when a query about callback is made.
		// This is done to ensure correctness as otherwise we would need something
		// like a cronjob to reset the flags of cached configurations.
		if ((callbackExpiryTime != null) && callbackExpiryTime.before(new Date())) {
			hasRegisteredCallback = false;
		}
		
		return hasRegisteredCallback;
	}

	public void setHasRegisteredCallback(boolean hasRegisteredCallback) {
		this.hasRegisteredCallback = hasRegisteredCallback;
	}

	public Date getCallbackExpiryTime() {
		return callbackExpiryTime;
	}

	public void setCallbackExpiryTime(Date callbackExpiryTime) {
		this.callbackExpiryTime = callbackExpiryTime;
	}
	
	public boolean isCacheAccessBlocked() {
		if (cacheAccessBlocked) {
			if (cacheBlockageExpiryTime.before(new Date())) {
				cacheAccessBlocked = false;
				cacheBlockageExpiryTime = null;
			}
		}
		return cacheAccessBlocked;
	}

	public void blockCacheAccess() {
		this.cacheAccessBlocked = true;
	}

	public void setCacheBlockageExpiryTime(Date cacheBlockageExpiryTime) {
		this.cacheBlockageExpiryTime = cacheBlockageExpiryTime;
	}
	
	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public boolean identifierMatches(Object identifier) {
		if (identifier == null) return false;
		if (identifier instanceof URI) return wsIdentifier.equals(identifier);
		if (identifier instanceof String) return rnsPaths.contains(identifier);
		if (identifier instanceof Integer) return (identifier.equals(inodeNumber));
		return false;
	}
	
	public boolean isMappedToRNSPaths() {
		return (rnsPaths != null && rnsPaths.size() > 0);
	}
	
	public boolean isDirectory() {
		return (type == ResourceType.DIRECTORY);
	}
	
	public long getMillisecondTimeLeftToCallbackExpiry() {
		if (!hasRegisteredCallback) return 0;
		long expiryTimeInMillis = callbackExpiryTime.getTime();
		long currentTimeMillis = System.currentTimeMillis();
		if (expiryTimeInMillis <= currentTimeMillis) return 0;
		return (expiryTimeInMillis - currentTimeMillis);
	}
	
	public boolean isRoot() {
		return rnsPaths.contains("/");
	}
	
	public boolean isMatchingPath(String path) {
		return (getMatchingPath(path) != null);
	}
	
	public String getMatchingPath(String path) {
		for (String rnsPath : rnsPaths) {
			if (rnsPath.matches(path)) {
				return rnsPath;
			}
		}
		return null;
	}
}
