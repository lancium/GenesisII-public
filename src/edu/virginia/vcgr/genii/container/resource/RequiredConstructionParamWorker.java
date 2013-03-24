package edu.virginia.vcgr.genii.container.resource;

import org.apache.axis.types.URI;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.io.IOException;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.security.authz.providers.*;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;

public class RequiredConstructionParamWorker
{
	static public void setRequiredConstructionParameters(IResource resource, HashMap<QName, Object> consParms)
		throws ResourceException
	{
		URI epi = (URI) consParms.get(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM);
		if (epi == null) {
			throw new MissingConstructionParamException(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM);
		}
		resource.setProperty(IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME, epi);

		X509Certificate[] certChain = (X509Certificate[]) consParms.get(IResource.CERTIFICATE_CHAIN_CONSTRUCTION_PARAM);
		if (certChain != null) {
			resource.setProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME, certChain);
		}

		try {
			// perform any authz initialization of the resource for the authz
			// handler specified
			IAuthZProvider handler = AuthZProviders.getProvider(resource.getParentResourceKey().getServiceName());
			ICallingContext context = null;
			try {
				context = ContextManager.getExistingContext();
			} catch (Throwable t) {
				// No current context
			}

			X509Certificate[] serviceCertChain = (X509Certificate[]) consParms
				.get(IResource.SERVICE_CERTIFICATE_CHAIN_CONSTRUCTION_PARAM);

			handler.setDefaultAccess(context, resource, serviceCertChain);

		} catch (IOException e) {
			throw new ResourceException("Could not initialize AuthZ for resource: " + e.getMessage(), e);
		}
	}
}
