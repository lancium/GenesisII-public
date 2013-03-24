package edu.virginia.vcgr.genii.client.invoke.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.byteio.TransferInformationType;
import org.ggf.rbyteio.Append;
import org.ggf.rbyteio.AppendResponse;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ggf.rbyteio.TruncAppend;
import org.ggf.rbyteio.TruncAppendResponse;
import org.ggf.rbyteio.Write;
import org.ggf.rbyteio.WriteResponse;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ggf.sbyteio.SeekWrite;
import org.ggf.sbyteio.SeekWriteResponse;
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.oasis_open.docs.wsrf.rp_2.DeleteResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.oasis_open.docs.wsrf.rp_2.InsertResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.SetResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.SetResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.UpdateResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesResponse;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.cache.AttributeCache;
import edu.virginia.vcgr.genii.client.cache.AttributeCacheFlushListener;
import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.RNSNotificationHandler;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.fuse.MetadataManager;
import edu.virginia.vcgr.genii.client.invoke.InvocationContext;
import edu.virginia.vcgr.genii.client.invoke.PipelineProcessor;
import edu.virginia.vcgr.genii.client.naming.WSAddressingConstants;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileRequestType;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileResponseType;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.iterator.IterableElementType;
import edu.virginia.vcgr.genii.iterator.IterateRequestType;
import edu.virginia.vcgr.genii.iterator.IterateResponseType;
import edu.virginia.vcgr.genii.iterator.WSIteratorPortType;

/*
 * This class intercepts RPC calls to GenesisII containers. Its only purpose is to 
 * fill up the attribute cache by propagating local modifications of attributes and
 * storing prefetched attributes to the unified client side cache. An intercepter is 
 * used to keep the rest of the code oblivious of the attribute caching. Furthermore, 
 * it is easier to ensure that all relevant attributes are cached when a single 
 * intercepter handles the task, instead of put-to-cache calls from arbitrary locations.
 * 
 * One of the most important task of this handler is to reconfigure the getMultipleAttributes
 * call and requests for additional attributes that we hope the client will look for 
 * immediately or that may come useful for managing the cache.   
 * */
public class AttributeCacheHandler2
{

	private static Log _logger = LogFactory.getLog(AttributeCacheHandler2.class);

	private class FlushListener implements AttributeCacheFlushListener
	{
		@Override
		public void flush(WSName endpoint, QName... attributes)
		{
			if (endpoint == null) {
				CacheManager.clearCache(MessageElement.class);
			} else {
				if ((attributes == null) || (attributes.length == 0)) {
					CacheManager.removeAllRelevantInfoFromCache(endpoint, MessageElement.class);
				} else {
					for (QName attr : attributes) {
						CacheManager.removeItemFromCache(endpoint, attr, MessageElement.class);
					}
				}
			}
		}
	}

	public AttributeCacheHandler2()
	{
		AttributeCache.addFlushListener(new FlushListener());
	}

	/*
	 * In new implementation this is the only method that lost the ability to retrieve the response
	 * from the cache. However, we don't think it is not a likely scenario that this method will be
	 * called multiple times from anywhere within our code base. So the loss of caching should not
	 * have any significant impact.
	 */
	@PipelineProcessor(portType = GeniiCommon.class)
	public GetResourcePropertyDocumentResponse getResourcePropertyDocument(InvocationContext ctxt,
		GetResourcePropertyDocument request) throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		GetResourcePropertyDocumentResponse resp = (GetResourcePropertyDocumentResponse) ctxt.proceed();
		MessageElement[] attributes = resp.get_any();
		storeAttributesInCache(target, attributes);
		return resp;
	}

	@PipelineProcessor(portType = GeniiCommon.class)
	public UpdateResourcePropertiesResponse updateResourceProperties(InvocationContext ctxt,
		UpdateResourceProperties updateRequest) throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		UpdateResourcePropertiesResponse response = (UpdateResourcePropertiesResponse) ctxt.proceed();
		storeAttributesInCache(target, updateRequest.getUpdate().get_any());
		return response;
	}

	@PipelineProcessor(portType = GeniiCommon.class)
	public DeleteResourcePropertiesResponse deleteResourceProperties(InvocationContext ctxt,
		DeleteResourceProperties deleteRequest) throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		DeleteResourcePropertiesResponse response = (DeleteResourcePropertiesResponse) ctxt.proceed();
		CacheManager.removeItemFromCache(target, deleteRequest.getDelete().getResourceProperty(), MessageElement.class);
		return response;
	}

	@PipelineProcessor(portType = GeniiCommon.class)
	public InsertResourcePropertiesResponse insertResourceProperties(InvocationContext ctxt,
		InsertResourceProperties insertRequest) throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		InsertResourcePropertiesResponse response = (InsertResourcePropertiesResponse) ctxt.proceed();
		storeAttributesInCache(target, insertRequest.getInsert().get_any());
		return response;
	}

	@PipelineProcessor(portType = GeniiCommon.class)
	public SetResourcePropertiesResponse setResourceProperties(InvocationContext ctxt, SetResourceProperties setRequest)
		throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		SetResourcePropertiesResponse response = (SetResourcePropertiesResponse) ctxt.proceed();
		storeAttributesInCache(target, setRequest.getInsert().get_any());
		storeAttributesInCache(target, setRequest.getUpdate().get_any());
		CacheManager.removeItemFromCache(target, setRequest.getDelete().getResourceProperty(), MessageElement.class);
		return response;
	}

	@PipelineProcessor(portType = GeniiCommon.class)
	public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(InvocationContext ctxt,
		QName[] getMultipleResourcePropertiesRequest) throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();

		Collection<MessageElement> result = findAttributes(target, getMultipleResourcePropertiesRequest);
		if (result != null) {
			return new GetMultipleResourcePropertiesResponse(result.toArray(new MessageElement[0]));
		}

		Object[] originalParameters = ctxt.getParams();

		for (QName orig : getMultipleResourcePropertiesRequest) {
			if (_logger.isDebugEnabled())
				_logger.debug("Reqested parameter: " + orig);
		}

		List<QName> addedAttributeNames = reconfigureToPrefetchAdditionalAttributes(ctxt, getMultipleResourcePropertiesRequest);
		GetMultipleResourcePropertiesResponse response = (GetMultipleResourcePropertiesResponse) ctxt.proceed();
		storeAttributesInCache(target, response.get_any());
		filterAddedAttributesFromTheResponse(response, addedAttributeNames);

		ctxt.updateParams(originalParameters);
		return response;
	}

	@PipelineProcessor(portType = GeniiCommon.class)
	public GetResourcePropertyResponse getResourceProperty(InvocationContext ctxt, QName getResourcePropertyRequest)
		throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		Collection<MessageElement> result = findAttribute(target, getResourcePropertyRequest);
		if (result != null) {
			return new GetResourcePropertyResponse(result.toArray(new MessageElement[0]));
		}
		if (_logger.isTraceEnabled())
			_logger.trace("Reqested resource property: " + getResourcePropertyRequest);

		GetResourcePropertyResponse response = (GetResourcePropertyResponse) ctxt.proceed();
		storeAttributesInCache(target, response.get_any());
		return response;
	}

	@PipelineProcessor(portType = EnhancedRNSPortType.class)
	public LookupResponseType lookup(InvocationContext ctxt, String[] names) throws Throwable
	{

		LookupResponseType resp = (LookupResponseType) ctxt.proceed();
		RNSEntryResponseType[] initMembers = resp.getEntryResponse();

		EndpointReferenceType target = ctxt.getTarget();
		WSName wsName = new WSName(target);
		WSResourceConfig targetConfig = null;
		if (wsName.isValidWSName()) {
			targetConfig = (WSResourceConfig) CacheManager.getItemFromCache(wsName.getEndpointIdentifier(),
				WSResourceConfig.class);
		}

		if (initMembers != null) {
			// let the cache manager to inspect the returned entries and store any important
			// information, if found.
			for (RNSEntryResponseType member : initMembers) {
				if (targetConfig == null) {
					CacheManager.cacheReleventInformation(member);
				} else {
					CacheManager.cacheReleventInformation(targetConfig, member);
				}
			}
		}
		return resp;
	}

	@PipelineProcessor(portType = WSIteratorPortType.class)
	public IterateResponseType iterate(InvocationContext ctxt, IterateRequestType iterateRequest) throws Throwable
	{

		IterateResponseType resp = (IterateResponseType) ctxt.proceed();

		EndpointReferenceType target = ctxt.getTarget();
		WSName wsName = new WSName(target);
		WSResourceConfig targetConfig = null;
		if (wsName.isValidWSName()) {
			targetConfig = (WSResourceConfig) CacheManager.getItemFromCache(wsName.getEndpointIdentifier(),
				WSResourceConfig.class);
		}

		if (resp.getIterableElement() != null) {
			for (IterableElementType member : resp.getIterableElement()) {
				MessageElement[] any = member.get_any();
				if (any != null && any.length == 1) {
					QName type = any[0].getQName();
					if (type != null && type.equals(RNSEntryResponseType.getTypeDesc().getXmlType())) {
						RNSEntryResponseType entry = ObjectDeserializer.toObject(any[0], RNSEntryResponseType.class);
						if (targetConfig == null) {
							CacheManager.cacheReleventInformation(entry);
						} else {
							CacheManager.cacheReleventInformation(targetConfig, entry);
						}
					}
				}
			}
		}
		return resp;
	}

	@PipelineProcessor(portType = EnhancedRNSPortType.class)
	public RNSEntryResponseType[] add(InvocationContext ctxt, RNSEntryType[] addRequest) throws Throwable
	{
		RNSEntryResponseType[] resp = (RNSEntryResponseType[]) ctxt.proceed();
		if (resp != null) {
			for (RNSEntryResponseType entry : resp) {
				CacheManager.cacheReleventInformation(entry);
			}
			EndpointReferenceType target = ctxt.getTarget();
			int addedEntryCount = resp.length;
			RNSNotificationHandler.updateElementCountAttribute(target, addedEntryCount);
		}
		return resp;
	}

	@PipelineProcessor(portType = EnhancedRNSPortType.class)
	public CreateFileResponseType createFile(InvocationContext ctxt, CreateFileRequestType createFile) throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		CreateFileResponseType resp = (CreateFileResponseType) ctxt.proceed();
		if (resp != null) {
			RNSNotificationHandler.updateElementCountAttribute(target, 1);
		}
		return resp;
	}

	@PipelineProcessor(portType = EnhancedRNSPortType.class)
	public RNSEntryResponseType[] remove(InvocationContext ctxt, String[] removeRequest) throws Throwable
	{
		RNSEntryResponseType[] resp = (RNSEntryResponseType[]) ctxt.proceed();
		if (resp != null) {
			int numberOfRemovedEntries = resp.length;
			EndpointReferenceType target = ctxt.getTarget();
			RNSNotificationHandler.updateElementCountAttribute(target, numberOfRemovedEntries * -1);
		}
		return resp;
	}

	@PipelineProcessor(portType = RandomByteIOPortType.class)
	public WriteResponse write(InvocationContext ctxt, Write write) throws Throwable
	{
		removeByteIOAttributesBeforeWrite(ctxt);
		WriteResponse resp = (WriteResponse) ctxt.proceed();
		if (resp != null)
			storePiggyBackedByteIOAttributes(ctxt, resp.getTransferInformation());
		return resp;
	}

	@PipelineProcessor(portType = RandomByteIOPortType.class)
	public AppendResponse append(InvocationContext ctxt, Append append) throws Throwable
	{
		removeByteIOAttributesBeforeWrite(ctxt);
		AppendResponse resp = (AppendResponse) ctxt.proceed();
		if (resp != null)
			storePiggyBackedByteIOAttributes(ctxt, resp.getTransferInformation());
		return resp;
	}

	@PipelineProcessor(portType = RandomByteIOPortType.class)
	public TruncAppendResponse truncAppend(InvocationContext ctxt, TruncAppend truncAppend) throws Throwable
	{
		removeByteIOAttributesBeforeWrite(ctxt);
		TruncAppendResponse resp = (TruncAppendResponse) ctxt.proceed();
		if (resp != null)
			storePiggyBackedByteIOAttributes(ctxt, resp.getTransferInformation());
		return resp;
	}

	@PipelineProcessor(portType = StreamableByteIOPortType.class)
	public SeekWriteResponse seekWrite(InvocationContext ctxt, SeekWrite seekWriteRequest) throws Throwable
	{
		removeByteIOAttributesBeforeWrite(ctxt);
		SeekWriteResponse resp = (SeekWriteResponse) ctxt.proceed();
		if (resp != null)
			storePiggyBackedByteIOAttributes(ctxt, resp.getTransferInformation());
		return resp;
	}

	private void storePiggyBackedByteIOAttributes(InvocationContext ctxt, TransferInformationType transferInformation)
	{
		EndpointReferenceType target = ctxt.getTarget();
		if (transferInformation != null) {
			MessageElement[] piggyBackedAttributes = transferInformation.get_any();
			storeAttributesInCache(target, piggyBackedAttributes);
		}
	}

	private void removeByteIOAttributesBeforeWrite(InvocationContext ctxt)
	{
		EndpointReferenceType target = ctxt.getTarget();
		MetadataManager.updateAttributesAfterWrite(target);
	}

	private void storeAttributesInCache(EndpointReferenceType target, MessageElement[] attributes)
	{
		if (attributes != null) {
			for (MessageElement element : attributes) {
				CacheManager.putItemInCache(target, element.getQName(), element);
			}
		}
	}

	private Collection<MessageElement> findAttribute(EndpointReferenceType target, QName attr)
	{
		QName[] attrArray = new QName[] { attr };
		return findAttributes(target, attrArray);
	}

	private Collection<MessageElement> findAttributes(EndpointReferenceType target, QName[] attrs)
	{
		Collection<MessageElement> result = new ArrayList<MessageElement>();
		for (QName attr : attrs) {
			Object cachedValue = CacheManager.getItemFromCache(target, attr, MessageElement.class);
			if (cachedValue == null)
				return null;
			if (cachedValue instanceof MessageElement) {
				result.add((MessageElement) cachedValue);
			} else {
				@SuppressWarnings("unchecked")
				Collection<MessageElement> collection = (Collection<MessageElement>) cachedValue;
				result.addAll(collection);
			}
		}
		return result;
	}

	/*
	 * reconfigureToPrefetchAdditionalAttributes and filterAddedAttributesFromTheResponse are the
	 * two methods that deal with prefetching additional attributes with a getMultipleAttributes
	 * request and remove those extra attributes from the response to keep the process transparent
	 * to the caller. Note that, we are relying on the fact that retrieving these additional
	 * attributes will not increase the cost on the container significantly and most often the
	 * client will need to access the added attributes subsequently.
	 * 
	 * Note that we are not checking whether or not the requested attributes are already in the
	 * cache. This is done to improve the freshness of the cached attributes.
	 */
	private List<QName> reconfigureToPrefetchAdditionalAttributes(InvocationContext ctxt, QName[] originallyRequestedAttributes)
	{

		EndpointReferenceType target = ctxt.getTarget();
		TypeInformation typeInformation = new TypeInformation(target);

		if (!isSafePortTypeForCallAggregation(typeInformation, target))
			return null;

		List<QName> potentialToBeAddedAttributes = new ArrayList<QName>();
		potentialToBeAddedAttributes.add(GenesisIIBaseRP.PERMISSIONS_STRING_QNAME);
		if (typeInformation.isEnhancedRNS()) {
			potentialToBeAddedAttributes.add(RNSConstants.ELEMENT_COUNT_QNAME);
		} else if (typeInformation.isByteIO()) {
			if (typeInformation.isRByteIO()) {
				potentialToBeAddedAttributes.add(ByteIOConstants.rsize);
				potentialToBeAddedAttributes.add(ByteIOConstants.rcreatTime);
				potentialToBeAddedAttributes.add(ByteIOConstants.raccessTime);
				potentialToBeAddedAttributes.add(ByteIOConstants.rmodTime);
				potentialToBeAddedAttributes.add(ByteIOConstants.rxferMechs);
			} else if (typeInformation.isSByteIO()) {
				potentialToBeAddedAttributes.add(ByteIOConstants.ssize);
				potentialToBeAddedAttributes.add(ByteIOConstants.screatTime);
				potentialToBeAddedAttributes.add(ByteIOConstants.saccessTime);
				potentialToBeAddedAttributes.add(ByteIOConstants.smodTime);
				potentialToBeAddedAttributes.add(ByteIOConstants.sxferMechs);
			}
		}
		Iterator<QName> iterator = potentialToBeAddedAttributes.iterator();
		while (iterator.hasNext()) {
			QName potentialAttribute = iterator.next();
			boolean attributeAlreadyRequested = false;
			for (QName originalAttribute : originallyRequestedAttributes) {
				if (originalAttribute.equals(potentialAttribute)) {
					attributeAlreadyRequested = true;
					break;
				}
			}
			if (attributeAlreadyRequested)
				iterator.remove();
		}
		if (!potentialToBeAddedAttributes.isEmpty()) {
			int totalAttributes = originallyRequestedAttributes.length + potentialToBeAddedAttributes.size();
			int index = 0;
			QName[] modifiedRequest = new QName[totalAttributes];
			for (QName originalAttribute : originallyRequestedAttributes) {
				modifiedRequest[index] = originalAttribute;
				index++;
			}
			for (QName newAttribute : potentialToBeAddedAttributes) {
				modifiedRequest[index] = newAttribute;
				index++;
			}
			ctxt.updateParams(new Object[] { modifiedRequest });
			return potentialToBeAddedAttributes;
		}
		return null;
	}

	private void filterAddedAttributesFromTheResponse(GetMultipleResourcePropertiesResponse response,
		List<QName> addedAttributeList)
	{
		MessageElement[] elements = response.get_any();
		if (elements == null)
			return;
		if (addedAttributeList == null || addedAttributeList.isEmpty())
			return;
		Set<QName> addedAttributes = new HashSet<QName>(addedAttributeList);
		List<MessageElement> baseResponse = new ArrayList<MessageElement>();
		for (MessageElement element : elements) {
			QName qName = element.getQName();
			if (!addedAttributes.contains(qName)) {
				baseResponse.add(element);
			}
		}
		response.set_any(baseResponse.toArray(new MessageElement[baseResponse.size()]));
	}

	/*
	 * Ideally any port-type that extends byteIO or enhanced-RNS port-types should have the
	 * additional attributes that we chunk with a request for retrieving resource properties.
	 * Unfortunately, this is not the case. For many port-types in our system we don't have the
	 * required attributes of byteIOs or enhanced-RNSes, depending on which one is applicable.
	 * Therefore, this method is used to explicitly validate that the target is a ByteIO or
	 * Enhanced-RNS, where our property aggregation operation is safe to do.
	 */
	private boolean isSafePortTypeForCallAggregation(TypeInformation typeInfo, EndpointReferenceType target)
	{

		if (typeInfo.isResourceFork())
			return false;
		if (!typeInfo.isRNS() && !typeInfo.isByteIO())
			return false;

		try {
			MetadataType metaData = target.getMetadata();
			if (metaData != null && metaData.get_any() != null) {

				QName portTypeAttributeName = new QName(WSAddressingConstants.WSA_NS, "PortType");
				String portTypeValue = null;
				for (MessageElement element : metaData.get_any()) {
					if (element.getQName().equals(portTypeAttributeName)) {
						portTypeValue = element.getValue();
						break;
					}
				}
				if (portTypeValue != null) {
					if (portTypeValue.endsWith("RandomByteIOPortType") || portTypeValue.endsWith("StreamableByteIOPortType")) {
						if (_logger.isDebugEnabled())
							_logger.debug("matching ByteIO port-type has been found");
						return true;
					} else if (portTypeValue.endsWith("EnhancedRNSPortType")) {
						if (_logger.isDebugEnabled())
							_logger.debug("matching RNS port-type has been found");
						return true;
					}
				}
			}
		} catch (Exception ex) {
			if (_logger.isDebugEnabled())
				_logger.debug("Failed to parse EPR to retrieve port-type information" + ex.getMessage());
		}
		return false;
	}
}
