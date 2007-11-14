package edu.virginia.vcgr.genii.client.invoke.handlers;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.RNSPortType;
import org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.invoke.InvocationContext;
import edu.virginia.vcgr.genii.client.invoke.PipelineProcessor;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rattrs.GetAttributesDocumentResponse;
import edu.virginia.vcgr.genii.common.rattrs.GetAttributesResponse;
import edu.virginia.vcgr.genii.common.rattrs.SetAttributes;
import edu.virginia.vcgr.genii.common.rattrs.SetAttributesResponse;

public class AttributeCacheHandler
{
	static private Log _logger = LogFactory.getLog(AttributeCacheHandler.class);
	
	static private final int _MAX_CACHE_ELEMENTS = 1024;
	static private final long _DEFAULT_TIMEOUT_MS = 1000 * 45;
	
	private TimedOutLRUCache<WSName, CachedAttributeData> _attrCache =
		new TimedOutLRUCache<WSName, CachedAttributeData>(_MAX_CACHE_ELEMENTS, _DEFAULT_TIMEOUT_MS);
	
	public AttributeCacheHandler()
	{
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
	public GetAttributesResponse getAttributes(InvocationContext ctxt,
		QName[] getAttributesRequest) throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		WSName name = new WSName(target);
		
		if (!name.isValidWSName())
		{
			// we can't cache if it doesn't have a valid EPI
			return (GetAttributesResponse)ctxt.proceed();
		}
		
		CachedAttributeData data;
		synchronized(_attrCache)
		{
			data = _attrCache.get(name);
		}
		
		Collection<MessageElement> ret = null;
		if (data != null)
			ret = findAttributes(getAttributesRequest, data);
		if (ret == null)
		{
			GetAttributesResponse resp = (GetAttributesResponse)ctxt.proceed();
			data = new CachedAttributeData(resp);
			synchronized(_attrCache)
			{
				_attrCache.put(name, data);
			}
			
			ret = findAttributes(getAttributesRequest, data);
		}
		
		if (ret == null)
			return new GetAttributesResponse(new MessageElement[0]);;
		
		return new GetAttributesResponse(ret.toArray(new MessageElement[0]));
	}

	@PipelineProcessor(portType = GeniiCommon.class)
	public GetAttributesDocumentResponse getAttributesDocument(InvocationContext ctxt,
			Object getAttributesDocumentRequest) throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		WSName name = new WSName(target);
		
		if (!name.isValidWSName())
		{
			// we can't cache if it doesn't have a valid EPI
			return (GetAttributesDocumentResponse)ctxt.proceed();
		}
		
		CachedAttributeData data;
		synchronized(_attrCache)
		{
			data = _attrCache.get(name);
		}
		
		if (data == null || !data.isFull())
		{
			GetAttributesDocumentResponse resp = (GetAttributesDocumentResponse)ctxt.proceed();
			data = new CachedAttributeData(resp);
			synchronized(_attrCache)
			{
				_attrCache.put(name, data);
			}
		}
		
		return new GetAttributesDocumentResponse(data.getAll());
	}
	
	@PipelineProcessor(portType = GeniiCommon.class)
	public SetAttributesResponse setAttributes(InvocationContext ctxt,
			SetAttributes setAttributesRequest) throws Throwable
	{
		EndpointReferenceType target = ctxt.getTarget();
		WSName name = new WSName(target);
		
		if (!name.isValidWSName())
		{
			// we can't cache if it doesn't have a valid EPI
			return (SetAttributesResponse)ctxt.proceed();
		}
		
		synchronized(_attrCache)
		{
			_attrCache.remove(name);
		}
		
		return (SetAttributesResponse)ctxt.proceed();
	}

	@PipelineProcessor(portType = GeniiCommon.class)
	public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(InvocationContext ctxt,
			QName[] getMultipleResourcePropertiesRequest) throws Throwable
	{
		_logger.warn("Caching or WSRF-RP is not supported yet.");
		return (GetMultipleResourcePropertiesResponse)ctxt.proceed();
	}

	@PipelineProcessor(portType = GeniiCommon.class)
	public GetResourcePropertyResponse getResourceProperty(InvocationContext ctxt,
			QName getResourcePropertyRequest) throws Throwable
	{
		_logger.warn("Caching or WSRF-RP is not supported yet.");
		return (GetResourcePropertyResponse)ctxt.proceed();
	}

	@PipelineProcessor(portType = RNSPortType.class)
	public ListResponse list(InvocationContext ctxt,
		List listRequest) throws Throwable
	{
		// We're going to let the list proceed, and then see if any meta data came back with it.
		ListResponse resp = (ListResponse)ctxt.proceed();
		
		QName xferMechs = new QName(ByteIOConstants.RANDOM_BYTEIO_NS,
			ByteIOConstants.XFER_MECHS_ATTR_NAME);
		QName size = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.SIZE_ATTR_NAME);
		
		for (EntryType entry : resp.getEntryList())
		{
			WSName name = new WSName(entry.getEntry_reference());
			if (name.isValidWSName())
			{
				MessageElement []any = entry.get_any();
				if (any != null)
				{
					ArrayList<MessageElement> cachedAttrs = new ArrayList<MessageElement>();
					for (MessageElement elem : any)
					{
						QName elemName = elem.getQName();
						if (elemName.equals(xferMechs) || elemName.equals(size))
						{
							cachedAttrs.add(elem);
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
		
		return resp;
	}
}
