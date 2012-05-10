package edu.virginia.vcgr.genii.client.invoke.handlers;

import org.ggf.rns.NameMappingType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.unified.RNSNotificationHandler;
import edu.virginia.vcgr.genii.client.invoke.InvocationContext;
import edu.virginia.vcgr.genii.client.invoke.PipelineProcessor;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileRequestType;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileResponseType;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;

/*
 * Like the AttributeCacheHandler, this class intercepts RPC to GenesisII containers. Its sole purpose is to 
 * propagate all RNS related update to local RNS lookup caches.
 * */
public class LookupCacheHandler {

	@PipelineProcessor(portType = EnhancedRNSPortType.class)
	public RNSEntryResponseType[] add(InvocationContext ctxt, RNSEntryType[] addRequest) throws Throwable {
		RNSEntryResponseType[] resp = (RNSEntryResponseType[]) ctxt.proceed();
		EndpointReferenceType target = ctxt.getTarget();
		if (resp != null) {
			for (RNSEntryResponseType entry : resp) {
				EndpointReferenceType child = entry.getEndpoint();
				String entryName = entry.getEntryName();
				RNSNotificationHandler.updateLookupAndDirectoryCacheAfterEntryAddition(target, child, entryName);
			}
		}
		return resp;
	}
	
	@PipelineProcessor(portType = EnhancedRNSPortType.class)
	public CreateFileResponseType createFile(InvocationContext ctxt, CreateFileRequestType createFile) throws Throwable {
		CreateFileResponseType resp = (CreateFileResponseType) ctxt.proceed();
		EndpointReferenceType target = ctxt.getTarget();
		if (resp != null) {
			EndpointReferenceType child = resp.getEndpoint();
			String entryName = createFile.getFilename();
			RNSNotificationHandler.updateLookupAndDirectoryCacheAfterEntryAddition(target, child, entryName);
		}
		return resp;
	}

	@PipelineProcessor(portType = EnhancedRNSPortType.class)
	public RNSEntryResponseType[] remove(InvocationContext ctxt, String[] removeRequest) throws Throwable {
		RNSEntryResponseType[] resp = (RNSEntryResponseType[]) ctxt.proceed();
		EndpointReferenceType target = ctxt.getTarget();
		if (resp != null) {
			RNSNotificationHandler.updateCacheAfterEntryRemoval(target, removeRequest);
		}
		return resp;
	}

	@PipelineProcessor(portType = EnhancedRNSPortType.class)
	public RNSEntryResponseType[] rename(InvocationContext ctxt, NameMappingType[] renameRequest) throws Throwable {
		RNSEntryResponseType[] resp = (RNSEntryResponseType[]) ctxt.proceed();
		EndpointReferenceType target = ctxt.getTarget();
		for (NameMappingType mapping : renameRequest) {
			RNSNotificationHandler.updateCacheAfterEntryRename(target, mapping.getSourceName(), mapping.getTargetName());
		}
		return resp;
	}
}
