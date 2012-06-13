package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rp.DefaultSingleResourcePropertyTranslator;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;

@XmlRootElement(namespace = GenesisIIConstants.GENESISII_NS, name = "AuthZConfigUpdateNotification")
public class AuthZConfigUpdateNotification extends NotificationMessageContents {

	private static final long serialVersionUID = 0L;

	public AuthZConfigUpdateNotification() {}
	
	public AuthZConfigUpdateNotification(AuthZConfig authZConfig) 
			throws ResourceUnknownFaultType, ResourceException {
		useIndirectPublishers = true;
		indirectPublishersRetrieveQuery = "SELECT DISTINCT(resourceid) FROM entries WHERE endpoint_id = ?";
		MessageElement element = new MessageElement(GenesisIIBaseRP.AUTHZ_CONFIG_QNAME, authZConfig);
		setAdditionalAttributes(new MessageElement[] {element}); 
	}
	
	@XmlTransient
	public AuthZConfig getNewConfig() throws ResourcePropertyException {
		MessageElement[] additionalAttributes = getAdditionalAttributes();
		for (MessageElement attribute : additionalAttributes) {
			QName qName = attribute.getQName();
			if (qName.equals(GenesisIIBaseRP.AUTHZ_CONFIG_QNAME)) {
				SingleResourcePropertyTranslator authZTranslator = 
					new DefaultSingleResourcePropertyTranslator();
				return authZTranslator.deserialize(AuthZConfig.class, attribute);
			}
		}
		throw new RuntimeException("Could not find authZConfig " +
				"attribute in notification message.");
	}
	
	@Override
	public boolean isIgnoreBlockedIndirectPublisher(long blockingTime) {
		return false;
	}
}
