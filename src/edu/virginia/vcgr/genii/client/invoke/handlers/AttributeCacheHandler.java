package edu.virginia.vcgr.genii.client.invoke.handlers;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSMetadataType;
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

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.cache.AttributeCache;
import edu.virginia.vcgr.genii.client.cache.AttributeCacheFlushListener;
import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.invoke.InvocationContext;
import edu.virginia.vcgr.genii.client.invoke.PipelineProcessor;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.iterator.IterableElementType;
import edu.virginia.vcgr.genii.iterator.IterateRequestType;
import edu.virginia.vcgr.genii.iterator.IterateResponseType;
import edu.virginia.vcgr.genii.iterator.WSIteratorPortType;

public class AttributeCacheHandler
{
	static private Log _logger = LogFactory.getLog(AttributeCacheHandler.class);
	
	static private final int _MAX_CACHE_ELEMENTS = 1024;
	static private final long _DEFAULT_TIMEOUT_MS = 1000 * 45;
	
	private TimedOutLRUCache<WSName, CachedAttributeData> _attrCache =
		new TimedOutLRUCache<WSName, CachedAttributeData>(_MAX_CACHE_ELEMENTS, _DEFAULT_TIMEOUT_MS);
	
	static private QName rxferMechs = new QName(ByteIOConstants.RANDOM_BYTEIO_NS,
			ByteIOConstants.XFER_MECHS_ATTR_NAME);
	static private QName rsize = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.SIZE_ATTR_NAME);
	static private QName raccessTime = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.ACCESSTIME_ATTR_NAME);
	static private QName rmodTime = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.MODTIME_ATTR_NAME);
	static private QName rcreatTime = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.CREATTIME_ATTR_NAME);
	static private QName sxferMechs = new QName(ByteIOConstants.STREAMABLE_BYTEIO_NS,
			ByteIOConstants.XFER_MECHS_ATTR_NAME);
	static private QName ssize = new QName(ByteIOConstants.STREAMABLE_BYTEIO_NS, ByteIOConstants.SIZE_ATTR_NAME);
	static private QName saccessTime = new QName(ByteIOConstants.STREAMABLE_BYTEIO_NS, ByteIOConstants.ACCESSTIME_ATTR_NAME);
	static private QName smodTime = new QName(ByteIOConstants.STREAMABLE_BYTEIO_NS, ByteIOConstants.MODTIME_ATTR_NAME);
	static private QName screatTime = new QName(ByteIOConstants.STREAMABLE_BYTEIO_NS, ByteIOConstants.CREATTIME_ATTR_NAME);
	// static private QName authz = AuthZConfig.getTypeDesc().getXmlType();
	
	/*
	static private QName authz = new QName("http://tempuri.org", "Mark");
	*/
	
	private class FlushListener implements AttributeCacheFlushListener
	{
		@Override
		public void flush(WSName endpoint, QName... attributes)
		{
			synchronized(_attrCache)
			{
				if (endpoint == null)
				{
					if ((attributes == null) || (attributes.length == 0))
					{
						_attrCache.clear();
						return;
					}
					
					for (WSName e : _attrCache.keySet())
						flush(e, attributes);
				} else
				{
					if ((attributes == null) || (attributes.length == 0))
					{
						_attrCache.remove(endpoint);
					} else
					{
						CachedAttributeData data = _attrCache.get(endpoint);
						if (data != null)
						{
							data.flush(attributes);
						}
					}
				}
			}
		}
	}
	
	public AttributeCacheHandler()
	{
		AttributeCache.addFlushListener(new FlushListener());
		_attrCache.activelyTimeoutElements(true);
	}
	
	private Collection<MessageElement> findAttributes(QName []attrs, CachedAttributeData data)
	{
		Collection<MessageElement> ret = new ArrayList<MessageElement>();
		for (QName attr : attrs)
		{
			Collection<MessageElement> partial = data.getAttributes(attr);
			if (partial == null)
			{
				if (!data.isFull())
					return null;
			} else
			{
				ret.addAll(partial);
			}
		}
		
		return ret;
	}
	
	@PipelineProcessor(portType = GeniiCommon.class)
	public GetResourcePropertyDocumentResponse getResourcePropertyDocument(
		InvocationContext ctxt,
		GetResourcePropertyDocument request) throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		WSName name = new WSName(target);
		
		if (!name.isValidWSName())
		{
			// we can't cache if it doesn't have a valid EPI
			return (GetResourcePropertyDocumentResponse)ctxt.proceed();
		}
		
		_logger.debug("Looking for cached attribute data for " + name);
		
		CachedAttributeData data;
		synchronized(_attrCache)
		{
			data = _attrCache.get(name);
		}
		
		if (data == null || !data.isFull())
		{
			GetResourcePropertyDocumentResponse resp =
				(GetResourcePropertyDocumentResponse)ctxt.proceed();
			data = new CachedAttributeData(resp);
			synchronized(_attrCache)
			{
				_attrCache.put(name, data);
			}
		}
		
		return new GetResourcePropertyDocumentResponse(data.getAll());
	}
	
	@PipelineProcessor(portType = GeniiCommon.class)
	public UpdateResourcePropertiesResponse updateResourceProperties(
		InvocationContext ctxt, UpdateResourceProperties updateRequest) 
			throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		WSName name = new WSName(target);
		
		if (!name.isValidWSName())
		{
			// we can't cache if it doesn't have a valid EPI
			return (UpdateResourcePropertiesResponse)ctxt.proceed();
		}
		
		_logger.debug("Clearing cached attribute data.");
		
		synchronized(_attrCache)
		{
			_attrCache.remove(name);
		}
		
		return (UpdateResourcePropertiesResponse)ctxt.proceed();
	}
	
	@PipelineProcessor(portType = GeniiCommon.class)
	public DeleteResourcePropertiesResponse deleteResourceProperties(
		InvocationContext ctxt, DeleteResourceProperties deleteRequest) 
			throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		WSName name = new WSName(target);
		
		if (!name.isValidWSName())
		{
			// we can't cache if it doesn't have a valid EPI
			return (DeleteResourcePropertiesResponse)ctxt.proceed();
		}
		
		_logger.debug("Clearing cached attribute data.");
		
		synchronized(_attrCache)
		{
			_attrCache.remove(name);
		}
		
		return (DeleteResourcePropertiesResponse)ctxt.proceed();
	}
	
	@PipelineProcessor(portType = GeniiCommon.class)
	public InsertResourcePropertiesResponse insertResourceProperties(
		InvocationContext ctxt, InsertResourceProperties insertRequest) 
			throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		WSName name = new WSName(target);
		
		if (!name.isValidWSName())
		{
			// we can't cache if it doesn't have a valid EPI
			return (InsertResourcePropertiesResponse)ctxt.proceed();
		}
		
		_logger.debug("Clearing cached attribute data.");
		
		synchronized(_attrCache)
		{
			_attrCache.remove(name);
		}
		
		return (InsertResourcePropertiesResponse)ctxt.proceed();
	}
	
	@PipelineProcessor(portType = GeniiCommon.class)
	public SetResourcePropertiesResponse setResourceProperties(
		InvocationContext ctxt, SetResourceProperties setRequest) 
			throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		WSName name = new WSName(target);
		
		if (!name.isValidWSName())
		{
			// we can't cache if it doesn't have a valid EPI
			return (SetResourcePropertiesResponse)ctxt.proceed();
		}
		
		_logger.debug("Clearing cached attribute data.");
		
		synchronized(_attrCache)
		{
			_attrCache.remove(name);
		}
		
		return (SetResourcePropertiesResponse)ctxt.proceed();
	}

	@PipelineProcessor(portType = GeniiCommon.class)
	public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(InvocationContext ctxt,
			QName[] getMultipleResourcePropertiesRequest) throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		WSName name = new WSName(target);
		
		if (!name.isValidWSName())
		{
			// we can't cache if it doesn't have a valid EPI
			return (GetMultipleResourcePropertiesResponse)ctxt.proceed();
		}
		
		_logger.debug("Looking for cached attribute data for " + name);
		
		CachedAttributeData data;
		synchronized(_attrCache)
		{
			data = _attrCache.get(name);
		}
		
		Collection<MessageElement> ret = null;
		if (data != null)
			ret = findAttributes(getMultipleResourcePropertiesRequest, data);
		if (ret == null)
		{
			_logger.debug("Couldn't find attribute data...making outcall.");
			GetMultipleResourcePropertiesResponse resp = 
				(GetMultipleResourcePropertiesResponse)ctxt.proceed();
			data = new CachedAttributeData(resp.get_any());
			synchronized(_attrCache)
			{
				_attrCache.put(name, data);
			}
			
			ret = findAttributes(getMultipleResourcePropertiesRequest, data);
		}
		
		if (ret == null)
			return new GetMultipleResourcePropertiesResponse(new MessageElement[0]);;
		
		return new GetMultipleResourcePropertiesResponse(ret.toArray(new MessageElement[0]));
	}

	@PipelineProcessor(portType = GeniiCommon.class)
	public GetResourcePropertyResponse getResourceProperty(InvocationContext ctxt,
			QName getResourcePropertyRequest) throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		WSName name = new WSName(target);
		
		if (!name.isValidWSName())
		{
			// we can't cache if it doesn't have a valid EPI
			return (GetResourcePropertyResponse)ctxt.proceed();
		}
		
		_logger.debug("Looking for cached attribute data for " + name);
		
		CachedAttributeData data;
		synchronized(_attrCache)
		{
			data = _attrCache.get(name);
		}
		
		Collection<MessageElement> ret = null;
		if (data != null)
			ret = findAttributes(new QName[] { getResourcePropertyRequest }, data);
		if (ret == null)
		{
			GetResourcePropertyResponse resp = 
				(GetResourcePropertyResponse)ctxt.proceed();
			data = new CachedAttributeData(resp.get_any());
			synchronized(_attrCache)
			{
				_attrCache.put(name, data);
			}
			
			ret = findAttributes(new QName[] { getResourcePropertyRequest }, data);
		}
		
		if (ret == null)
			return new GetResourcePropertyResponse(new MessageElement[0]);;
		
		return new GetResourcePropertyResponse(ret.toArray(new MessageElement[0]));
	}

	private void cacheResponse(RNSEntryResponseType member){
		RNSMetadataType mdt = member.getMetadata();
		MessageElement []any = (mdt == null) ? null : mdt.get_any();
		if (any != null)
		{
			RNSEntryResponseType entry = member;
			WSName name = new WSName(entry.getEndpoint());
			if (name.isValidWSName())
			{
				if (any != null)
				{
					ArrayList<MessageElement> cachedAttrs = new ArrayList<MessageElement>();
					for (MessageElement elem : any)
					{
						QName elemName = elem.getQName();
						if (elemName.equals(rxferMechs) || elemName.equals(rsize) ||
								elemName.equals(raccessTime) || elemName.equals(rmodTime) ||
								elemName.equals(rcreatTime) ||
								elemName.equals(GenesisIIBaseRP.PERMISSIONS_STRING_QNAME) ||
								elemName.equals(sxferMechs) || elemName.equals(ssize) ||
								elemName.equals(saccessTime) || elemName.equals(smodTime) ||
								elemName.equals(screatTime))
						{
							_logger.debug("Adding " + elemName + " to " + name);
							cachedAttrs.add(elem);
						} else
						{
							_logger.debug("NOT Adding " + elemName + " to " + name);
						}
					}

					CachedAttributeData data = new CachedAttributeData(cachedAttrs);
					synchronized(_attrCache)
					{
						_attrCache.put(name, data);
					}
				}
			}
		}	
	}
	
	
	
	@PipelineProcessor(portType = EnhancedRNSPortType.class)
	public LookupResponseType lookup(InvocationContext ctxt, 
			String []names) throws Throwable
			{
		_logger.debug("Doing an RNS iterator listing so we can cache attribute data.");

		// We're going to let the list proceed, and then see if any meta data came back with it.
		LookupResponseType resp = (LookupResponseType)ctxt.proceed();
		RNSEntryResponseType []initMembers = resp.getEntryResponse();

		//Fill metadata cache
		if (initMembers != null){
			for (RNSEntryResponseType member : initMembers)
			{
				cacheResponse(member);
			}
		}
		
		
		return resp;
	}
	
	@PipelineProcessor(portType = WSIteratorPortType.class)
	public IterateResponseType iterate(InvocationContext ctxt,
		IterateRequestType iterateRequest) throws Throwable
	{
		_logger.debug("Doing an iterator iterate so we can cache attribute data.");
		
		// We're going to let the iterate proceed, and then see if any meta data came back with it.
		IterateResponseType resp = (IterateResponseType)ctxt.proceed();
		if (resp.getIterableElement() != null)
		{
			for (IterableElementType member : resp.getIterableElement())
			{
				MessageElement []any = member.get_any();
				if (any != null && any.length == 1)
				{
					QName type = any[0].getQName();
					if (type != null && type.equals(RNSEntryResponseType.getTypeDesc().getXmlType()))
					{
						RNSEntryResponseType entry = ObjectDeserializer.toObject(any[0], 
							RNSEntryResponseType.class);
						//Fill metadata cache
						cacheResponse(entry);
					
					}
				}
			}
		}
		
		return resp;
	}
}
