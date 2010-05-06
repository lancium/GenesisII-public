package edu.virginia.vcgr.genii.client.naming;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;

import java.net.URL;
import java.sql.Blob;
import javax.sql.rowset.serial.SerialBlob;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.container.ContainerConstants;
import edu.virginia.vcgr.genii.client.ogsa.OGSARP;
import edu.virginia.vcgr.genii.client.ogsa.OGSAWSRFBPConstants;
import edu.virginia.vcgr.genii.client.resource.AttributedURITypeSmart;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.ser.BlobLimits;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.client.security.*;

import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.*;
import org.w3.www.ns.ws_policy.*;

public class EPRUtils
{
	static private Log _logger = LogFactory.getLog(EPRUtils.class);

	static public final String GENII_CONTAINER_ID_PARAMETER =
		"genii-container-id";
	static private final Pattern GENII_CONTAINER_ID_PATTERN = 
		Pattern.compile(String.format("%s=([-a-fA-F0-9]+)",
			Pattern.quote(GENII_CONTAINER_ID_PARAMETER)));
	
	static public final String GENII_SHORT_PARAMETER_NAME =
		"genii-short-parameter";
	static private final Pattern GENII_SHORT_PARAMTER_PATTERN =
		Pattern.compile(String.format("%s=([-a-fA-F0-9:]+)",
			Pattern.quote(GENII_SHORT_PARAMETER_NAME)));
	
	/**
	 * Generates a temporary EPR to (hopefully) obtain the full (incl.
	 * porttypes, certs, etc.) EPR from the service's attributes. The full one
	 * is returned if available, otherwise the temporary one is returned.
	 * 
	 * Some checking of URIs may be required to ensure that a man-in-the-middle
	 * attack has not occured
	 * 
	 * @param serviceURL
	 * @return The EPR for the service indicated.
	 */
	static public EndpointReferenceType makeEPR(String serviceURL,
			boolean retrieveFromObject)
	{

		EndpointReferenceType epr =
				new EndpointReferenceType(
						new AttributedURITypeSmart(serviceURL), null, null,
						null);

		try
		{
			new URL(serviceURL);
		}
		catch (MalformedURLException mue)
		{
			// Not a URL
			retrieveFromObject = false;
		}

		if (!retrieveFromObject)
		{
			return epr;
		}

		try
		{
			OGSARP rp =
					(OGSARP) ResourcePropertyManager.createRPInterface(epr,
							OGSARP.class);
			return rp.getResourceEndpoint();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			return epr;
		}
	}

	/**
	 * Generates a temporary EPR to (hopefully) obtain the full (incl.
	 * porttypes, certs, etc.) EPR from the service's attributes. The full one
	 * is returned if available, otherwise the temporary one is returned.
	 * 
	 * Some checking of URIs may be required to ensure that a man-in-the-middle
	 * attack has not occured
	 * 
	 * @param serviceURL
	 * @return The EPR for the service indicated.
	 */
	static public EndpointReferenceType makeEPR(String serviceURL)
	{
		return makeEPR(serviceURL, true);
	}

	static public URI extractEndpointIdentifier(EndpointReferenceType epr)
	{
		MetadataType mdt = epr.getMetadata();
		if (mdt == null)
			return null;

		MessageElement[] any = mdt.get_any();
		if (any == null || any.length == 0)
			return null;

		for (MessageElement element : any)
		{
			if (element.getQName().equals(WSName.ENDPOINT_IDENTIFIER_QNAME))
			{
				String s = element.getValue().toString();
				try
				{
					return new URI(s);
				}
				catch (URI.MalformedURIException e)
				{
					_logger.warn("Found EPR with WSName \"" + s
							+ "\" which isn't a URI.");
					return null;
				}
			}
		}

		return null;
	}

	static public X509Certificate[] extractCertChain(EndpointReferenceType epr)
			throws GeneralSecurityException
	{

		MetadataType mdt = epr.getMetadata();
		if (mdt == null)
			return null;
		MessageElement[] elements = mdt.get_any();
		if (elements == null || elements.length == 0)
			return null;

		for (MessageElement element : elements)
		{
			if (element.getQName()
					.equals(
							new QName(GenesisIIConstants.OGSA_BSP_NS,
									"EndpointKeyInfo")))
			{

				// original style
				element =
						element.getChildElement(new QName(
								org.apache.ws.security.WSConstants.WSSE11_NS,
								"SecurityTokenReference"));
				if (element != null)
				{
					return WSSecurityUtils
							.getChainFromPkiPathSecTokenRef(element);
				}

			}
			else if (element.getQName().equals(
					new QName(org.apache.ws.security.WSConstants.WSSE11_NS,
							"SecurityTokenReference")))
			{

				// Secure Addressing style
				return WSSecurityUtils.getChainFromPkiPathSecTokenRef(element);
			}
		}

		return null;
	}

	static public MessageLevelSecurityRequirements extractMinMessageSecurity(
			EndpointReferenceType epr) throws GeneralSecurityException
	{

		MetadataType mdt = epr.getMetadata();
		if (mdt == null)
			return null;
		MessageElement[] elements = mdt.get_any();
		if (elements == null || elements.length == 0)
			return null;

		RequiredMessageSecurityType minSec = null;
		for (MessageElement element : elements)
		{

			if (element.getQName().equals(
					new QName(PolicyAttachment.getTypeDesc().getXmlType()
							.getNamespaceURI(), "PolicyAttachment")))
			{

				// OGSA Secure Addressing compliant security requirements
				try
				{

					PolicyAttachment policyAttachment =
							(PolicyAttachment) element
									.getObjectValue(PolicyAttachment.class);

					MessageLevelSecurityRequirements retval = new MessageLevelSecurityRequirements();

					// TODO: assume it applies to everything. We will want to
					// get
					// more specific at some point of SecAddr takes off
					Policy metaPolicy = policyAttachment.getPolicy();
					MessageElement[] policyElements = metaPolicy.get_any();
					if (policyElements == null || policyElements.length == 0)
					{
						return null;
					}

					for (MessageElement attachmentElement : policyElements)
					{
						if (attachmentElement.getQName().equals(
								new QName(PolicyReference.getTypeDesc()
										.getXmlType().getNamespaceURI(),
										"PolicyReference")))
						{

							PolicyReference policyReference =
									(PolicyReference) attachmentElement
											.getObjectValue(PolicyReference.class);
							if (policyReference.getURI().equals(
									new org.apache.axis.types.URI(
											SecurityConstants.MUTUAL_X509_URI)))
							{
								retval =
										retval
												.computeUnion(new MessageLevelSecurityRequirements(
														MessageLevelSecurityRequirements.SIGN));
							}

						}
						else if (attachmentElement.getQName().equals(
								new QName(SePartsType.getTypeDesc()
										.getXmlType().getNamespaceURI(),
										"EncryptedParts")))
						{

							retval = retval.computeUnion(new MessageLevelSecurityRequirements(
									MessageLevelSecurityRequirements.ENCRYPT));
						}
					}

					return retval;

				}
				catch (Exception e)
				{
					e.printStackTrace(System.err);
					return null;
				}
			}
		}

		if (minSec == null)
		{
			return null;
		}

		RequiredMessageSecurityTypeMin minType = minSec.getMin();
		if (minType == null)
		{
			return null;
		}

		return new MessageLevelSecurityRequirements(minType.getValue());
	}

	static private Pattern _SERVICE_NAME_PATTERN =
			Pattern.compile("^.*/axis/services/([^/]+)$");

	static public String extractServiceName(URI uri)
		throws AxisFault
	{
		Matcher matcher = _SERVICE_NAME_PATTERN.matcher(
			uri.getPath());
		if (!matcher.matches())
			throw new AxisFault(String.format(
				"Can't locate target service \"%s\".", uri));
		return matcher.group(1);
	}
	
	static public String extractServiceName(EndpointReferenceType epr)
			throws AxisFault
	{
		return extractServiceName(epr.getAddress().get_value());
	}

	static public String extractServiceName(String url)
		throws AxisFault, MalformedURIException
	{
		return extractServiceName(new URI(url));
	}

	static public byte[] toBytes(EndpointReferenceType epr)
			throws ResourceException
	{
		ByteArrayOutputStream baos = null;

		try
		{
			baos = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(baos);
			ObjectSerializer.serialize(writer, epr, new QName(
					GenesisIIConstants.GENESISII_NS, "endpoint"));
			writer.flush();
			writer.close();

			return baos.toByteArray();
		}
		catch (IOException ioe)
		{
			throw new ResourceException(ioe.toString(), ioe);
		}
		finally
		{
			StreamUtils.close(baos);
		}

	}

	static public EndpointReferenceType fromBytes(byte[] data)
			throws ResourceException
	{
		ByteArrayInputStream bais = null;

		try
		{
			bais = new ByteArrayInputStream(data);
			return fromInputStream(bais);
		}
		finally
		{
			StreamUtils.close(bais);
		}
	}

	static public EndpointReferenceType fromInputStream(InputStream in)
			throws ResourceException
	{
		return (EndpointReferenceType) ObjectDeserializer.deserialize(
				new InputSource(in), EndpointReferenceType.class);
	}

	static public Blob toBlob(
		EndpointReferenceType epr, String tableName, String columnName)
			throws ResourceException
	{
		long maxLength = BlobLimits.limits().getLimit(tableName, columnName);
		
		if (epr == null)
			return null;

		try
		{
			Blob blob = new SerialBlob(toBytes(epr));
			_logger.debug(String.format(
				"Created a blob of length %d bytes for %s.%s which has a " +
				"max length of %d bytes.",
				blob.length(), tableName, columnName, maxLength));
			if (blob.length() > maxLength)
			{
				_logger.error(String.format(
					"Error:  Blob was created with %d bytes for %s.%s, " +
					"but the maximum length for that column is %d bytes.", 
					blob.length(), tableName, columnName, maxLength));
			}
			
			return blob;
		}
		catch (Throwable t)
		{
			throw new ResourceException("Could not serialze epr to BLOB", t);
		}
	}

	static public EndpointReferenceType fromBlob(Blob blob)
			throws ResourceException

	{
		InputStream in = null;

		if (blob == null)
			return null;

		try
		{
			in = blob.getBinaryStream();
			return fromInputStream(in);
		}
		catch (Throwable t)
		{
			throw new ResourceException("Could not deserialize EPR from BLOB",
					t);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}

	static public PortType[] getImplementedPortTypes(EndpointReferenceType epr)
	{
		MetadataType mdt = epr.getMetadata();
		if (mdt == null)
			return null;
		MessageElement[] any = mdt.get_any();
		if (any == null || any.length == 0)
			return null;

		for (MessageElement element : any)
		{
			if (element.getQName().equals(
					OGSAWSRFBPConstants.WS_RESOURCE_INTERFACES_ATTR_QNAME))
			{
				String s = element.getValue().toString();
				return PortType.translate(s).toArray(new PortType[0]);
			}
		}

		return null;
	}
	
	static public GUID getGeniiContainerID(EndpointReferenceType epr)
	{
		AttributedURIType uriType = epr.getAddress();
		if (uriType != null)
		{
			URI uri = uriType.get_value();
			if (uri != null)
			{
				String query = uri.getQueryString();
				if (query != null)
				{
					Matcher matcher = GENII_CONTAINER_ID_PATTERN.matcher(
						query);
					if (matcher.matches())
						return GUID.fromString(matcher.group(1));
				}
			}
		}
		
		return null;
	}
	
	static public String getEPIShortParameter(EndpointReferenceType epr)
	{
		AttributedURIType uriType = epr.getAddress();
		if (uriType != null)
		{
			URI uri = uriType.get_value();
			if (uri != null)
			{
				String query = uri.getQueryString();
				if (query != null)
				{
					Matcher matcher = GENII_SHORT_PARAMTER_PATTERN.matcher(
						query);
					if (matcher.matches())
						return matcher.group(1);
				}
			}
		}
		
		return null;
	}

	static public EndpointReferenceType readEPR(String filename)
			throws IOException
	{
		FileInputStream fin = null;

		try
		{
			fin = new FileInputStream(filename);
			return (EndpointReferenceType) ObjectDeserializer.deserialize(
					new InputSource(fin), EndpointReferenceType.class);
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}

	static public boolean isUnboundEPR(EndpointReferenceType epr)
	{
		return ((epr.getAddress() == null)
				|| (epr.getAddress().get_value() == null) || (epr.getAddress()
				.get_value().toString().equals(WSName.UNBOUND_ADDRESS)));
	}

	static public EndpointReferenceType makeUnboundEPR(EndpointReferenceType epr)
	{
		return new EndpointReferenceType(
				new org.ws.addressing.AttributedURIType(WSName.UNBOUND_ADDRESS),
				epr.getReferenceParameters(), epr.getMetadata(), epr.get_any());
	}

	static public boolean isCommunicable(EndpointReferenceType epr)
	{
		AttributedURIType auri = epr.getAddress();
		org.apache.axis.types.URI uri = auri.get_value();

		try
		{
			new URL(uri.toString());
			return true;
		}
		catch (MalformedURLException mue)
		{
			return false;
		}
	}

	final static public void serializeEPR(ObjectOutputStream oos,
			EndpointReferenceType epr) throws IOException
	{
		if (epr == null)
		{
			oos.writeInt(-1);
		}
		else
		{
			byte[] data = toBytes(epr);
			oos.writeInt(data.length);
			oos.write(data);
		}
	}

	final static public EndpointReferenceType deserializeEPR(
			ObjectInputStream ois) throws IOException
	{
		int length = ois.readInt();
		if (length < 0)
		{
			return null;
		}
		else
		{
			byte[] data = new byte[length];
			ois.readFully(data);
			return fromBytes(data);
		}
	}
	
	static public EndpointReferenceType packEPR(EndpointReferenceType epr) 
		throws ResourceException
	{
		return fromBytes(toBytes(epr));
	}
	
	static public GUID extractContainerID(EndpointReferenceType epr)
	{
		if (epr == null)
			return null;
		
		MetadataType md = epr.getMetadata();
		if (md == null)
			return null;
		
		MessageElement []any = md.get_any();
		if (any == null)
			return null;
		
		for (MessageElement element : any)
		{
			QName name = element.getQName();
			if (name.equals(ContainerConstants.CONTAINER_ID_METADATA_ELEMENT))
			{
				return GUID.fromString(element.getValue());
			}
		}
		
		return null;
	}
}
