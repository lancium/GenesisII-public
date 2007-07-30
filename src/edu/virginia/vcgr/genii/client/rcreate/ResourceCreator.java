package edu.virginia.vcgr.genii.client.rcreate;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;

public class ResourceCreator
{
	static private Log _logger = LogFactory.getLog(ResourceCreator.class);
	
	static public EndpointReferenceType createNewResource(
		EndpointReferenceType serviceEPR, MessageElement []creationParameters,
		ResourceCreationContext creationContext)
		throws CreationException
	{
		if (creationContext == null)
			creationContext = new ResourceCreationContext();
		
		try
		{
			GeniiCommon common = ClientUtils.createProxy(
				GeniiCommon.class, serviceEPR);
			
			VcgrCreate vcgrCreateRequest = new VcgrCreate(creationParameters);
			return common.vcgrCreate(vcgrCreateRequest).getEndpoint();
		}
		catch (Exception e)
		{
			throw new CreationException(e.getLocalizedMessage(), e);
		}
	}
	
	static public EndpointReferenceType createNewResource(
		String serviceName, MessageElement []creationParameters,
		ResourceCreationContext creationContext)
			throws CreationException
	{
		if (creationContext == null)
			creationContext = new ResourceCreationContext();
		
		String fullPath = creationContext.getDefaultContainerPath() +
			"/" + creationContext.getServiceRelativePath() +
			"/" + serviceName;
		
		try
		{
			RNSPath path = RNSPath.getCurrent().lookup(
				fullPath, RNSPathQueryFlags.MUST_EXIST);
			return createNewResource(
				path.getEndpoint(), creationParameters, creationContext);
		}
		catch (CreationException ce)
		{
			throw ce;
		}
		catch (Exception e)
		{
			throw new CreationException(e.getLocalizedMessage(), e);
		}
	}
	
	static public void terminate(EndpointReferenceType target)
	{
		try
		{
			GeniiCommon common = ClientUtils.createProxy(
				GeniiCommon.class, target);
			common.immediateTerminate(null);
		}
		catch (Throwable t)
		{
			_logger.warn(t.getLocalizedMessage(), t);
		}
	}
}