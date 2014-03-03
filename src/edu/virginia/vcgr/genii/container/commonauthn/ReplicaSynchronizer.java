package edu.virginia.vcgr.genii.container.commonauthn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.container.rns.GeniiDirSyncRunner;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;

/*
 * The behavior of a IDP resource is determined by its various resource properties. In addition, it
 * has the same capabilities that are available in an RNS resource. Therefore the replica
 * synchronization class for RNS, Genii- DirSyncRunner, can perform a bulk of the tasks warranted by
 * a IDP replication. The remaining places where we need to deal with IDP specific properties such
 * as specialized resource-create operation and resource properties synchronization are controlled
 * by this extension of the GeniiDirSyncRunner class.
 */
public class ReplicaSynchronizer extends GeniiDirSyncRunner {

	private static Log _logger = LogFactory.getLog(ReplicaSynchronizer.class);

	private STSResourcePropertiesRetriever propertyRetriever;

	public ReplicaSynchronizer(STSResourcePropertiesRetriever propertyRetriever) {
		this.propertyRetriever = propertyRetriever;
	}

	/*
	 * This is the method for populating some construction properties that are
	 * used to distinguish between the creation of a replica resource and a
	 * primary resource. Specific STS port-type classes use these properties to
	 * bypass or augment steps of resource creation procedure.
	 */
	@Override
	public Collection<MessageElement> getDefaultAttributes(
			EndpointReferenceType primaryEPR) {
		List<MessageElement> attributes = new ArrayList<MessageElement>();
		attributes.add(new MessageElement(
				STSConfigurationProperties.CERTIFICATE_OWNER_EPR, primaryEPR));
		attributes.add(new MessageElement(
				STSConfigurationProperties.REPLICA_STS_CONSTRUCTION_PARAM,
				"TRUE"));
		attributes
				.add(new MessageElement(
						STSConfigurationProperties.LINK_TO_SERVICE_DIR_CONSTRUCTION_PARAM,
						"FALSE"));
		return attributes;
	}

	/*
	 * This is a hook that is used by individual STS port-type classes to
	 * populate necessary resource properties after a replica resource has been
	 * created.
	 */
	@Override
	protected void retrieveAndStoreResourcePropertiesFromPrimary(
			GeniiCommon proxyToPrimary, IRNSResource resource) {
		try {
			propertyRetriever.retrieveAndStoreResourceProperties(
					proxyToPrimary, resource);
		} catch (Exception ex) {
			_logger.error(
					"Exception while trying to retrieve IDP specific resource properties",
					ex);
		}
	}

	public interface STSResourcePropertiesRetriever {
		public void retrieveAndStoreResourceProperties(
				GeniiCommon proxyToPrimary, IRNSResource resource)
				throws Exception;
	}
}
