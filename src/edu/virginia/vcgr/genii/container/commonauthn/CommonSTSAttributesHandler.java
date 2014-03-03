package edu.virginia.vcgr.genii.container.commonauthn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.codec.binary.Base64;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.GeniiDirPolicy;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.axis.ServerWSDoAllReceiver;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;

/*
 * This attribute handler class is particularly useful for exchanging replication related attributes
 * that are necessary for IDP resources. It makes some sensitive information such as IDP
 * certificates and private keys available for inspection. So additional security checking is
 * imposed to attribute getter methods to ensure that the system is not giving away such information
 * to any arbitrary entity leading to a security compromise.
 * 
 * However, there are some attributes such as transfer mechanisms and replication policy that are
 * not protected information. We keep them in this class too to have a common place for attribute
 * retrievals.
 */
public class CommonSTSAttributesHandler extends AbstractAttributeHandler {

	public CommonSTSAttributesHandler(AttributePackage pkg)
			throws NoSuchMethodException {
		super(pkg);
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException {

		addHandler(new QName(ByteIOConstants.RANDOM_BYTEIO_NS,
				ByteIOConstants.XFER_MECHS_ATTR_NAME), "getTransferMechsAttr");
		addHandler(SecurityConstants.NEW_IDP_NAME_QNAME, "getIDPNameAttr");
		addHandler(SecurityConstants.IDP_STORED_CREDENTIAL_QNAME,
				"getDelegatedCredentials");
		addHandler(SecurityConstants.CERTIFICATE_CHAIN_QNAME,
				"getResourceCertificate");
		addHandler(SecurityConstants.STORED_CALLING_CONTEXT_QNAME,
				"getStoredCallingContext");
		addHandler(SecurityConstants.IDP_PRIVATE_KEY_QNAME, "getPrivateKey");
		addHandler(GeniiDirPolicy.REPLICATION_POLICY_QNAME,
				"getReplicationPolicyAttr", "setReplicationPolicyAttr");
	}

	public Collection<MessageElement> getTransferMechsAttr() {

		ArrayList<MessageElement> ret = new ArrayList<MessageElement>();
		ret.add(new MessageElement(new QName(ByteIOConstants.RANDOM_BYTEIO_NS,
				ByteIOConstants.XFER_MECHS_ATTR_NAME),
				ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		ret.add(new MessageElement(new QName(ByteIOConstants.RANDOM_BYTEIO_NS,
				ByteIOConstants.XFER_MECHS_ATTR_NAME),
				ByteIOConstants.TRANSFER_TYPE_DIME_URI));
		ret.add(new MessageElement(new QName(ByteIOConstants.RANDOM_BYTEIO_NS,
				ByteIOConstants.XFER_MECHS_ATTR_NAME),
				ByteIOConstants.TRANSFER_TYPE_MTOM_URI));
		return ret;
	}

	public MessageElement getIDPNameAttr() throws IOException {
		IResource resource = getResourceAfterAccessChecking("IDP Name");
		String idpName = (String) resource
				.getProperty(SecurityConstants.NEW_IDP_NAME_QNAME
						.getLocalPart());
		return new MessageElement(SecurityConstants.NEW_IDP_NAME_QNAME, idpName);
	}

	public MessageElement getDelegatedCredentials() throws IOException {
		IResource resource = getResourceAfterAccessChecking("Delegated Credentials");
		NuCredential credential = (NuCredential) resource
				.getProperty(SecurityConstants.IDP_STORED_CREDENTIAL_QNAME
						.getLocalPart());
		return new MessageElement(
				SecurityConstants.IDP_STORED_CREDENTIAL_QNAME,
				serializeObjectToString(credential));
	}

	public MessageElement getStoredCallingContext() throws IOException {
		IResource resource = getResourceAfterAccessChecking("Stored Calling Context");
		ICallingContext callingContext = (ICallingContext) resource
				.getProperty(IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME);
		return new MessageElement(
				SecurityConstants.STORED_CALLING_CONTEXT_QNAME,
				callingContext.getSerialized());
	}

	public MessageElement getResourceCertificate() throws Throwable {
		IResource resource = getResourceAfterAccessChecking("Resource Certificate");
		Certificate[] certificate = (Certificate[]) resource
				.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
		return new MessageElement(SecurityConstants.CERTIFICATE_CHAIN_QNAME,
				serializeObjectToString(certificate));
	}

	public MessageElement getPrivateKey() throws IOException {
		IResource resource = getResourceAfterAccessChecking("Private Key");
		PrivateKey privateKey = (PrivateKey) resource
				.getProperty(IResource.PRIVATE_KEY_PROPERTY_NAME);
		return new MessageElement(SecurityConstants.IDP_PRIVATE_KEY_QNAME,
				serializeObjectToString(privateKey));
	}

	public MessageElement getReplicationPolicyAttr()
			throws ResourceUnknownFaultType, ResourceException {
		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = (IResource) rKey.dereference();
		String value = (String) resource
				.getProperty(GeniiDirPolicy.REPLICATION_POLICY_QNAME
						.getLocalPart());
		if (value == null)
			return null;
		return new MessageElement(GeniiDirPolicy.REPLICATION_POLICY_QNAME,
				value);
	}

	public void setReplicationPolicyAttr(MessageElement element)
			throws ResourceUnknownFaultType, ResourceException {
		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = (IResource) rKey.dereference();
		String value = element.getValue();
		if (resource.isServiceResource())
			return;
		resource.setProperty(
				GeniiDirPolicy.REPLICATION_POLICY_QNAME.getLocalPart(), value);
	}

	public static Object deserializeObjectFromString(String data)
			throws IOException, ClassNotFoundException {
		byte[] objectString = Base64.decodeBase64(data);
		ByteArrayInputStream biStream = new ByteArrayInputStream(objectString);
		ObjectInputStream oiStream = new ObjectInputStream(biStream);
		Object object = oiStream.readObject();
		oiStream.close();
		return object;
	}

	/*
	 * Some of the attributes such as certificate and private key are compound
	 * objects that cannot be directly transmitted over the wire. So we have a
	 * custom serializer method that convert the attributes to suitable form
	 * (base 64 encoded strings).
	 */
	private String serializeObjectToString(Serializable object)
			throws IOException {
		ByteArrayOutputStream boutStream = new ByteArrayOutputStream();
		ObjectOutputStream ooutStream = new ObjectOutputStream(boutStream);
		ooutStream.writeObject(object);
		ooutStream.close();
		byte[] encodedBytes = Base64.encodeBase64(boutStream.toByteArray());
		return new String(encodedBytes);
	}

	private IResource getResourceAfterAccessChecking(String attributeName)
			throws ResourceUnknownFaultType, ResourceException, IOException {
		ResourceKey resourceKey = ResourceManager.getCurrentResource();
		IResource resource = resourceKey.dereference();
		if (!ServerWSDoAllReceiver.checkAccess(resource, RWXCategory.WRITE)) {
			String message = "Unauthorized access to the sensitive IDP attribute: "
					+ attributeName;
			throw new SecurityException(message);
		}
		return resource;
	}

}
