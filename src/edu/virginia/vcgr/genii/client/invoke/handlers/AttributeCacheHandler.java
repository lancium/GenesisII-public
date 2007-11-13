package edu.virginia.vcgr.genii.client.invoke.handlers;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.RNSPortType;
import org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;

import edu.virginia.vcgr.genii.client.invoke.InvocationContext;
import edu.virginia.vcgr.genii.client.invoke.PipelineProcessor;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rattrs.GetAttributesDocumentResponse;
import edu.virginia.vcgr.genii.common.rattrs.GetAttributesResponse;
import edu.virginia.vcgr.genii.common.rattrs.SetAttributes;
import edu.virginia.vcgr.genii.common.rattrs.SetAttributesResponse;

public class AttributeCacheHandler
{
	static private Log _logger = LogFactory.getLog(AttributeCacheHandler.class);
	
	@PipelineProcessor(portType = GeniiCommon.class)
	public GetAttributesResponse getAttributes(InvocationContext ctxt,
		QName[] getAttributesRequest) throws Throwable
	{
		// TODO Auto-generated method stub
		return (GetAttributesResponse)ctxt.proceed();
	}

	@PipelineProcessor(portType = GeniiCommon.class)
	public GetAttributesDocumentResponse getAttributesDocument(InvocationContext ctxt,
			Object getAttributesDocumentRequest) throws Throwable
	{
		// TODO Auto-generated method stub
		return (GetAttributesDocumentResponse)ctxt.proceed();
	}
	
	@PipelineProcessor(portType = GeniiCommon.class)
	public SetAttributesResponse setAttributes(InvocationContext ctxt,
			SetAttributes setAttributesRequest) throws Throwable
	{
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return (ListResponse)ctxt.proceed();
	}
}