package edu.virginia.vcgr.genii.container.container.forks;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.axis.description.JavaServiceDesc;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.ReadOnlyRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

public class ServicesRNSFork extends ReadOnlyRNSResourceFork
{
	private String shortenedURL() throws ResourceException
	{
		EndpointReferenceType myEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().getProperty(
			WorkingContext.EPR_PROPERTY_NAME);
		String ret = myEPR.getAddress().get_value().toString();
		int last = ret.lastIndexOf('/');
		if (last <= 0)
			throw new ResourceException(String.format("Couldn't parse target container URL \"%s\".", ret));
		return ret.substring(0, last + 1);
	}

	private PortType[] findImplementedPortTypes(Class<?> jClass) throws ResourceException
	{
		try {
			Constructor<?> cons = jClass.getConstructor();
			Object obj = cons.newInstance();
			GenesisIIBase base = (GenesisIIBase) obj;
			return base.getImplementedPortTypes(null);
		} catch (ResourceException re) {
			throw re;
		} catch (Throwable cause) {
			throw new ResourceException("Unable to get target services' port types.", cause);
		}
	}

	public ServicesRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR, String entryName) throws IOException
	{
		Collection<InternalEntry> ret = new LinkedList<InternalEntry>();

		for (JavaServiceDesc desc : Container.getInstalledServices()) {
			String serviceName = desc.getName();
			if (entryName == null || entryName.equals(serviceName)) {
				ResourceKey targetKey = ResourceManager.getServiceResource(serviceName);

				EndpointReferenceType targetEPR = ResourceManager.createEPR(
					targetKey,
					String.format("%s%s?%s=%s", shortenedURL(), serviceName, EPRUtils.GENII_CONTAINER_ID_PARAMETER,
						Container.getContainerID()), findImplementedPortTypes(desc.getImplClass()), serviceName);
				ret.add(new InternalEntry(serviceName, targetEPR));
			}
		}

		return ret;
	}
}