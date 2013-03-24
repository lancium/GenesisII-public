package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;

import edu.virginia.vcgr.fsii.security.Permissions;
import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.common.PermissionsStringTranslator;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIACLManager;
import edu.virginia.vcgr.genii.client.rp.DefaultSingleResourcePropertyTranslator;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.client.security.axis.AxisAcl;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import edu.virginia.vcgr.genii.security.identity.Identity;

public class AuthZConfigCache extends CommonAttributeCache
{

	private SingleResourcePropertyTranslator authZTranslator;
	private SingleResourcePropertyTranslator permissionTranslator;
	private TimedOutLRUCache<String, Permissions> permissionCache;
	private TimedOutLRUCache<String, AuthZConfig> authZCache;

	public AuthZConfigCache(int priorityLevel, int capacity, long cacheLifeTime, boolean monitoingEnabled)
	{

		super(priorityLevel, capacity, cacheLifeTime, monitoingEnabled);

		authZCache = new TimedOutLRUCache<String, AuthZConfig>(capacity, cacheLifeTime);
		permissionCache = new TimedOutLRUCache<String, Permissions>(capacity, cacheLifeTime);

		authZTranslator = new DefaultSingleResourcePropertyTranslator();
		permissionTranslator = new PermissionsStringTranslator();
	}

	@Override
	public Object getItem(Object cacheKey, Object target)
	{
		String EPI = getEPI(target);
		QName qName = (QName) cacheKey;
		if (qName.equals(GenesisIIBaseRP.AUTHZ_CONFIG_QNAME)) {
			AuthZConfig config = authZCache.get(EPI);
			if (config == null)
				return null;
			return new MessageElement(qName, config);
		} else if (qName.equals(GenesisIIBaseRP.PERMISSIONS_STRING_QNAME)) {
			Permissions permissions = permissionCache.get(EPI);
			if (permissions == null) {
				permissions = getPermissions(authZCache.get(EPI));
			}
			if (permissions == null)
				return null;
			return new MessageElement(qName, permissions.toString());
		}
		return null;
	}

	@Override
	public void putItem(Object cacheKey, Object target, Object value) throws Exception
	{

		URI wsEndpointIdenfierURI = getEndpointIdentifierURI(target);
		String EPI = wsEndpointIdenfierURI.toString();
		long lifetime = getCacheLifeTime(wsEndpointIdenfierURI);
		QName qName = (QName) cacheKey;
		MessageElement element = (MessageElement) value;

		if (qName.equals(GenesisIIBaseRP.PERMISSIONS_STRING_QNAME)) {
			Permissions permissions = permissionTranslator.deserialize(Permissions.class, element);
			permissionCache.put(EPI, permissions, lifetime);

			// Removing authZConfig from the cache, as the information may be stale.
			authZCache.remove(EPI);

		} else if (qName.equals(GenesisIIBaseRP.AUTHZ_CONFIG_QNAME)) {

			AuthZConfig authZConfig = authZTranslator.deserialize(AuthZConfig.class, element);

			// Since an authZConfig holds reference to a set of MessageElements, there are
			// references from
			// it to the SOAPMessage that has returned this authZConfig. We have to cleanup all such
			// references
			// before saving the authZConfig.
			AuthZConfig sanitizedConfigAuthZConfig = Sanitizer.getSanitizedAuthZConfig(authZConfig);

			authZCache.put(EPI, sanitizedConfigAuthZConfig, lifetime);

			// We can translate authZConfig into permissions, so there is no need to keep
			// permissions in the cache for the same cache-key.
			permissionCache.remove(EPI);
		}
	}

	@Override
	public void invalidateCachedItem(Object target)
	{
		String EPI = getEPI(target);
		permissionCache.remove(EPI);
		authZCache.remove(EPI);
	}

	@Override
	public void invalidateCachedItem(Object cacheKey, Object target)
	{
		String EPI = getEPI(target);
		QName qName = (QName) cacheKey;
		if (qName.equals(GenesisIIBaseRP.AUTHZ_CONFIG_QNAME)) {
			authZCache.remove(EPI);
		} else if (qName.equals(GenesisIIBaseRP.PERMISSIONS_STRING_QNAME)) {
			permissionCache.remove(EPI);
		}
	}

	@Override
	public void invalidateEntireCache()
	{
		authZCache.clear();
		permissionCache.clear();
	}

	@Override
	public boolean cacheKeyMatches(Object cacheKey)
	{
		if (cacheKey instanceof QName) {
			return (cacheKey.equals(GenesisIIBaseRP.AUTHZ_CONFIG_QNAME) || cacheKey
				.equals(GenesisIIBaseRP.PERMISSIONS_STRING_QNAME));
		}
		return false;
	}

	@Override
	public void updateCacheLifeTimeOfItems(Object commonIdentifierForItems, long newCacheLifeTime)
	{

		URI wsIdentifier = (URI) commonIdentifierForItems;
		Collection<String> epiStrings = getCacheKeysForLifetimeUpdateRequest(wsIdentifier);
		if (epiStrings == null)
			return;

		for (String EPI : epiStrings) {
			AuthZConfig config = authZCache.get(EPI);
			if (config != null) {
				authZCache.put(EPI, config, newCacheLifeTime);
			}
			Permissions permissions = permissionCache.get(EPI);
			if (permissions != null) {
				permissionCache.put(EPI, permissions, newCacheLifeTime);
			}
		}
	}

	private Permissions getPermissions(AuthZConfig authZConfig)
	{
		if (authZConfig == null)
			return null;
		try {
			ICallingContext callingContext = ContextManager.getExistingContext();
			Collection<Identity> callerIdentities = SecurityUtils.getCallerIdentities(callingContext);
			return GenesisIIACLManager.getPermissions(AxisAcl.decodeAcl(authZConfig), callerIdentities);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
