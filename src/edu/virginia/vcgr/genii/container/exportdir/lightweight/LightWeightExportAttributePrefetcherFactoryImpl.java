package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.attrs.AttributePreFetcher;
import edu.virginia.vcgr.genii.container.byteio.export.ExportedRandomByteIOForkAttributePrefetcher;
import edu.virginia.vcgr.genii.container.common.AttributesPreFetcherFactory;
import edu.virginia.vcgr.genii.container.common.DefaultGenesisIIAttributesPreFetcher;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.RandomByteIOResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;

public class LightWeightExportAttributePrefetcherFactoryImpl implements AttributesPreFetcherFactory
{

	static private Log _logger = LogFactory.getLog(LightWeightExportAttributePrefetcherFactoryImpl.class);

	@Override
	public AttributePreFetcher getPreFetcher(EndpointReferenceType epr, ResourceKey rKey, ResourceForkService service)
		throws Throwable
	{
		ResourceFork fork = null;

		if ((fork = getMyRandomByteIOFork(epr, rKey, service)) != null) {
			if (fork instanceof RandomByteIOResourceFork) {
				return new ExportedRandomByteIOForkAttributePrefetcher(rKey, fork.getForkPath());
			}

		}

		if (Container.onThisServer(epr)) {
			return new DefaultGenesisIIAttributesPreFetcher<IResource>(epr);
		}

		return null;
	}

	private ResourceFork getMyRandomByteIOFork(EndpointReferenceType target, ResourceKey rKey, ResourceForkService service)
	{
		try {
			AddressingParameters ap = new AddressingParameters(target.getReferenceParameters());

			ResourceForkInformation rfi = (ResourceForkInformation) ap.getResourceForkInformation();

			if (rfi != null) {
				String targetKey = ap.getResourceKey();
				String myKey = rKey.getResourceKey();
				if (targetKey != null && myKey != null && (targetKey.equals(myKey))) {
					ResourceFork fork = rfi.instantiateFork(service);
					if (fork instanceof RandomByteIOResourceFork)
						return fork;
				}
			}
		} catch (Throwable cause) {
			// If anything goes wrong, we simply don't fill in the attributes.
			_logger.warn("Unable to fill in the attributes for an export resource fork.", cause);
		}

		return null;
	}

}
