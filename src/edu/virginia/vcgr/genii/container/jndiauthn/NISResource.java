package edu.virginia.vcgr.genii.container.jndiauthn;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.*;
import java.security.GeneralSecurityException;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.ReferenceParametersType;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.container.common.notification.SubscriptionInformation;
import edu.virginia.vcgr.genii.container.configuration.ServiceDescription;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class NISResource extends BasicDBResource implements INISResource {

	// used for pulling out the credential duration
	static private final QName _SERVICES_QNAME = new QName(
			GenesisIIConstants.GENESISII_NS, "services");
	static protected Long _resourceCertificateLifetime = null;

	// used for encoding the end certificate into the resource properties
	static private final String _RESOURCE_CERT_NAME = "resource-cert";
	static private QName _RESOURCE_CERT_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, _RESOURCE_CERT_NAME);

	// transient properties for the resource 
	transient protected HashMap<String, Object> _properties = new HashMap<String, Object>();

	
	public NISResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool,
			IResourceKeyTranslater translater)
		throws SQLException
	{
		super(parentKey, connectionPool, translater);
	}
	
	public void load(ReferenceParametersType refParams) throws ResourceUnknownFaultType, ResourceException
	{
		_resourceKey = (String) _translater.unwrap(refParams);
		if ((_resourceKey == null) || (isServiceResource())) {
			super.load(refParams);
			return;
		}

		for (MessageElement element : refParams.get_any()) {
			if (element.getQName().equals(_RESOURCE_CERT_QNAME)) {
				element = element.getChildElement(
						new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "SecurityTokenReference"));
				if (element != null) {
					try {
						X509Certificate[] callersNotionOfMe = 
							SecurityUtils.getChainFromPkiPathSecTokenRef(element);
						setProperty(CERTIFICATE_CHAIN_PROPERTY_NAME, callersNotionOfMe);
					} catch (GeneralSecurityException e) {
						throw new ResourceException(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	public void initialize(HashMap<QName, Object> constructionParams)
		throws ResourceException {

		Boolean isService = (Boolean) constructionParams.get(
			IResource.IS_SERVICE_CONSTRUCTION_PARAM);
		if (isService != null && isService.booleanValue()) {
			super.initialize(constructionParams);
		} else {
			_resourceKey = ((URI) constructionParams.get(ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM)).toString();
		}
	}	
	
	@SuppressWarnings("unchecked")
	protected long getResourceCertLifetime() throws ResourceException, ConfigurationException {
		synchronized(this.getClass()) {
		
			if (_resourceCertificateLifetime != null) {
				return _resourceCertificateLifetime.longValue();
			}
	
			XMLConfiguration conf = 
				ConfigurationManager.getCurrentConfiguration().getContainerConfiguration();
			ArrayList<Object> sections;
			sections = conf.retrieveSections(_SERVICES_QNAME);
			for (Object obj : sections) {
				HashMap<String, ServiceDescription> services =
					(HashMap<String, ServiceDescription>)obj;
				ServiceDescription desc = services.get(this.getParentResourceKey().getServiceName());
				if (desc != null)
				{
					_resourceCertificateLifetime = new Long(desc.getResourceCertificateLifetime());
					break;
				}
			}
			return _resourceCertificateLifetime.longValue();
		}
	}
	

	/**
	 * Retrieve the value of a stored property.
	 * 
	 * @param propertyName The name of the property to retrieve.
	 * @return The value of the named property.
	 * @throws ResourceException If anything goes wrong.
	 */
	public Object getProperty(String propertyName) 
		throws ResourceException {

		if (isServiceResource()) {
			return super.getProperty(propertyName);
		}
		
		if (propertyName.equals(IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME)) {
			// calculate the epi
			try {
				return new URI(_resourceKey);
			} catch (URISyntaxException e) {
				throw new ResourceException(e.getMessage(), e);
			}
		}

		
		return _properties.get(propertyName);
	}
	
	/**
	 * Set the value of a stored property.
	 * 
	 * @param propertyName The name of the value to set.
	 * @param value The value for the property (which must be java.io.Serializable).
	 * @throws ResourceException If anything goes wrong.
	 */
	public void setProperty(String propertyName, Object value) 
		throws ResourceException {

		if (isServiceResource()) {
			super.setProperty(propertyName, value);
			return;
		}
		
		_properties.put(propertyName, value);
	}
	
	/**
	 * Destroy all state associated with this resource.
	 * 
	 * @throws ResourceException If anything goes wrong.
	 */
	public void destroy() throws ResourceException {

		if (isServiceResource()) {
			super.destroy();
			return;
		}
		
		throw new ResourceException("Cannot destroy transparent resources");
	}
	
	public Collection<SubscriptionInformation> matchSubscriptions(
			String topicExpression) throws ResourceException {
		
		if (isServiceResource()) {
			return super.matchSubscriptions(topicExpression);
		}
		
		throw new ResourceException("Cannot match subscriptions on transparent resources");
	}
	
	
	/**
	 * Retrieve the WS-Addressing ReferenceParameters that match this resource.
	 * 
	 * @return The Addressing information for WS-Addressing.
	 * @throws ResourceException If anything goes wrong.
	 */
	public ReferenceParametersType getResourceParameters()
		throws ResourceException {

		ReferenceParametersType refParams = super.getResourceParameters(); 

		// add the end-certificate as a new reference parameter

		X509Certificate[] certChain = 
			(X509Certificate[]) getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME); 
		
		if (certChain != null && (refParams != null)) { 
			ArrayList<MessageElement> mels = 
				new ArrayList<MessageElement>(Arrays.asList(refParams.get_any()));

			try {
				MessageElement certRef = new MessageElement(
						_RESOURCE_CERT_QNAME);
				certRef.addChild(SecurityUtils.makePkiPathSecTokenRef(certChain)); 
				mels.add(certRef);
			} catch (SOAPException e) {
				throw new ResourceException(e.getMessage(), e);
			} catch (GeneralSecurityException e) {
				throw new ResourceException(e.getMessage(), e);
			}
			
			refParams.set_any(mels.toArray(new MessageElement[0]));
		}
		
		return refParams;
	}
	

	
};
