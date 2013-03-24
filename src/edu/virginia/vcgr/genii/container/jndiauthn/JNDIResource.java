package edu.virginia.vcgr.genii.container.jndiauthn;

import org.apache.axis.types.URI;
import java.sql.SQLException;
import java.util.*;

import javax.xml.namespace.QName;

import org.ggf.rns.RNSEntryExistsFaultType;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.rns.RNSDBResource;
import edu.virginia.vcgr.genii.security.SecurityConstants;

public class JNDIResource extends RNSDBResource implements IJNDIResource
{

	// transient properties for the resource
	transient protected HashMap<String, Object> _properties = new HashMap<String, Object>();

	public JNDIResource(ResourceKey parentKey, DatabaseConnectionPool connectionPool) throws SQLException
	{

		super(parentKey, connectionPool);
	}

	public boolean isIdpResource()
	{
		if (_resourceKey == null) {
			return false;
		}
		if (_resourceKey.contains("IDP")) {
			return true;
		}
		return false;
	}

	public String getIdpName() throws ResourceException
	{

		if (!isIdpResource()) {
			throw new ResourceException("Not an IDP resource");
		}

		return _resourceKey.substring(_resourceKey.lastIndexOf(':') + 1);
	}

	public StsType getStsType() throws ResourceException
	{
		if (isServiceResource()) {
			throw new ResourceException("Not a STS or IDP resource");
		}

		if (isIdpResource()) {
			// IDP for a specific identity
			String type = _resourceKey.substring(0, _resourceKey.indexOf(':'));
			return StsType.valueOf(type.toUpperCase());
		}

		// STS for a JNDI directory resource

		return StsType.valueOf((String) getProperty(SecurityConstants.NEW_JNDI_STS_TYPE_QNAME.getLocalPart()));

	}

	public URI createChildIdpEpi(String childName) throws URI.MalformedURIException, ResourceException
	{

		if (isServiceResource() || isIdpResource()) {
			throw new ResourceException("Not a STS resource");
		}

		String type = (String) getProperty(SecurityConstants.NEW_JNDI_STS_TYPE_QNAME.getLocalPart());

		String epiStr = type + ":" + _resourceKey + ":IDP:" + childName;

		return new URI(epiStr);
	}

	@Override
	public void load(String resourceKey) throws ResourceUnknownFaultType, ResourceException
	{
		_resourceKey = resourceKey;
		if (!isIdpResource()) {
			super.load(resourceKey);
			return;
		}
	}

	public void initialize(HashMap<QName, Object> constructionParams) throws ResourceException
	{

		Boolean isIdpResource = (Boolean) constructionParams.get(IJNDIResource.IS_IDP_RESOURCE_CONSTRUCTION_PARAM);
		Boolean isServiceResource = (Boolean) constructionParams.get(IResource.IS_SERVICE_CONSTRUCTION_PARAM);

		if (isIdpResource != null && isIdpResource.booleanValue()) {

			_resourceKey = ((URI) constructionParams.get(ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM)).toString();
		} else if (isServiceResource == null || !isServiceResource.booleanValue()) {

			super.initialize(constructionParams);

			setProperty(SecurityConstants.NEW_JNDI_STS_NAME_QNAME.getLocalPart(),
				constructionParams.get(SecurityConstants.NEW_JNDI_STS_NAME_QNAME));
			setProperty(SecurityConstants.NEW_JNDI_STS_TYPE_QNAME.getLocalPart(),
				constructionParams.get(SecurityConstants.NEW_JNDI_STS_TYPE_QNAME));
			setProperty(SecurityConstants.NEW_JNDI_STS_HOST_QNAME.getLocalPart(),
				constructionParams.get(SecurityConstants.NEW_JNDI_STS_HOST_QNAME));
			setProperty(SecurityConstants.NEW_JNDI_NISDOMAIN_QNAME.getLocalPart(),
				constructionParams.get(SecurityConstants.NEW_JNDI_NISDOMAIN_QNAME));

		} else {

			super.initialize(constructionParams);
		}
	}

	/**
	 * Retrieve the value of a stored property.
	 * 
	 * @param propertyName
	 *            The name of the property to retrieve.
	 * @return The value of the named property.
	 * @throws ResourceException
	 *             If anything goes wrong.
	 */
	public Object getProperty(String propertyName) throws ResourceException
	{

		if (!isIdpResource()) {
			return super.getProperty(propertyName);
		}

		if (propertyName.equals(IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME)) {
			// calculate the epi
			try {
				return new URI(_resourceKey);
			} catch (URI.MalformedURIException e) {
				throw new ResourceException(e.getMessage(), e);
			}
		}

		return _properties.get(propertyName);
	}

	/**
	 * Set the value of a stored property.
	 * 
	 * @param propertyName
	 *            The name of the value to set.
	 * @param value
	 *            The value for the property (which must be java.io.Serializable).
	 * @throws ResourceException
	 *             If anything goes wrong.
	 */
	public void setProperty(String propertyName, Object value) throws ResourceException
	{

		if (!isIdpResource()) {
			super.setProperty(propertyName, value);
			return;
		}

		_properties.put(propertyName, value);
	}

	/**
	 * Destroy all state associated with this resource.
	 * 
	 * @throws ResourceException
	 *             If anything goes wrong.
	 */
	public void destroy() throws ResourceException
	{

		if (!isIdpResource()) {
			super.destroy();
			return;
		}

		throw new ResourceException("Cannot destroy transparent resources");
	}

	/*
	 * What does this do? public Collection<SubscriptionInformation> matchSubscriptions( String
	 * topicExpression) throws ResourceException {
	 * 
	 * if (!isIdpResource()) { return super.matchSubscriptions(topicExpression); }
	 * 
	 * throw new ResourceException( "Cannot match subscriptions on transparent resources"); }
	 */

	/*
	 * Mark Morgan -- This functionis no longer contained here. We'll have to revisit this issue
	 * again. public ReferenceParametersType getResourceParameters() throws ResourceException {
	 * 
	 * ReferenceParametersType refParams = super.getResourceParameters();
	 * 
	 * if (!isIdpResource()) { return refParams; }
	 * 
	 * ArrayList<MessageElement> mels = new ArrayList<MessageElement>(Arrays
	 * .asList(refParams.get_any()));
	 * 
	 * // add the end-certificate as a new reference parameter X509Certificate[] certChain =
	 * (X509Certificate[]) getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME); if (certChain !=
	 * null && (refParams != null)) {
	 * 
	 * try { MessageElement certRef = new MessageElement(_RESOURCE_CERT_QNAME);
	 * certRef.addChild(WSSecurityUtils .makePkiPathSecTokenRef(certChain)); mels.add(certRef); }
	 * catch (SOAPException e) { throw new ResourceException(e.getMessage(), e); } catch
	 * (GeneralSecurityException e) { throw new ResourceException(e.getMessage(), e); } }
	 * 
	 * // add all of our creation params try { ResourceKey stsKey =
	 * ResourceManager.getCurrentResource(); IResource stsResource = stsKey.dereference();
	 * 
	 * MessageElement stsParm = null;
	 * 
	 * stsParm = new MessageElement( SecurityConstants.NEW_JNDI_STS_HOST_QNAME, stsResource
	 * .getProperty(SecurityConstants.NEW_JNDI_STS_HOST_QNAME .getLocalPart())); mels.add(stsParm);
	 * stsParm = new MessageElement( SecurityConstants.NEW_JNDI_NISDOMAIN_QNAME, stsResource
	 * .getProperty(SecurityConstants.NEW_JNDI_NISDOMAIN_QNAME .getLocalPart())); mels.add(stsParm);
	 * 
	 * } catch (ResourceUnknownFaultType e) { throw new ResourceException(e.getMessage(), e); }
	 * 
	 * refParams.set_any(mels.toArray(new MessageElement[0]));
	 * 
	 * return refParams; }
	 */

	public void addEntry(InternalEntry entry) throws ResourceException, RNSEntryExistsFaultType
	{

		if (isServiceResource()) {
			super.addEntry(entry);
			return;
		}

		throw new ResourceException("Resource is not a JNDIAuthnPortType resource");
	}

	public Collection<String> listEntries(String name) throws ResourceException
	{

		if (isServiceResource()) {
			return super.listEntries(name);
		}

		throw new ResourceException("Resource is not a JNDIAuthnPortType resource");
	}

	public Collection<InternalEntry> retrieveEntries(String entryName) throws ResourceException
	{

		if (isServiceResource()) {
			return super.retrieveEntries(entryName);
		}

		throw new ResourceException("Resource is not a JNDIAuthnPortType resource");
	}

	public Collection<String> removeEntries(String entryName) throws ResourceException
	{

		if (isServiceResource()) {
			return super.removeEntries(entryName);
		}

		throw new ResourceException("Resource is not a JNDIAuthnPortType resource");
	}

};
