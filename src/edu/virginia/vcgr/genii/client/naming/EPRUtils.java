package edu.virginia.vcgr.genii.client.naming;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.PKIPathSecurity;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.AttributedURITypeSmart;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rattrs.GetAttributesResponse;
import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.security.*;

public class EPRUtils
{
	static private Log _logger = LogFactory.getLog(EPRUtils.class);

	/**
	 * Generates a temporary EPR to (hopefully) obtain the 
	 * full (incl. porttypes, certs, etc.) EPR from the service's 
	 * attributes.  The full one is returned if available, otherwise
	 * the temporary one is returned.
	 * 
	 * Some checking of URIs may be required to ensure that a man-in-the-middle
	 * attack has not occured
	 * 
	 * @param serviceURL
	 * @return The EPR for the service indicated.
	 */
	static public EndpointReferenceType makeEPR(String serviceURL, boolean retrieveFromObject) {
		
		EndpointReferenceType epr = new EndpointReferenceType(
				new AttributedURITypeSmart(serviceURL),
				null, null, null);
		
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
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, epr);

			// attempt to get the full EPR from the object itself
			GetAttributesResponse resp = common.getAttributes(
					new QName[] {GenesisIIConstants.RESOURCE_ENDPOINT_ATTR_QNAME});
			MessageElement []elements = resp.get_any();
			if (elements == null || elements.length < 1)
				throw new Exception("Couldn't get EPR for target container.");
			return (EndpointReferenceType)elements[0].getObjectValue(
					EndpointReferenceType.class);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			return epr;
		}		
	}
	
	/**
	 * Generates a temporary EPR to (hopefully) obtain the 
	 * full (incl. porttypes, certs, etc.) EPR from the service's 
	 * attributes.  The full one is returned if available, otherwise
	 * the temporary one is returned.
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
	
	static public URI extractEndpointIdentifier(
			EndpointReferenceType epr)
	{
		MetadataType mdt = epr.getMetadata();
		if (mdt == null)
			return null;
		
		MessageElement []any = mdt.get_any();
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
				catch (URISyntaxException e)
				{
					_logger.warn("Found EPR with WSName \"" +
						s + "\" which isn't a URI.");
					return null;
				}
			}
		}

		return null;
	}
	
	static public X509Certificate[] extractCertChain(EndpointReferenceType epr) throws GeneralSecurityException {

		MetadataType mdt = epr.getMetadata();
		if (mdt == null)
			return null;
		MessageElement []elements = mdt.get_any();
		if (elements == null || elements.length == 0)
			return null;

		for (MessageElement element : elements)
		{
			if (element.getQName().equals(
					new QName(GenesisIIConstants.OGSA_BSP_NS, "EndpointKeyInfo"))) {
				element = element.getChildElement(
						new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "SecurityTokenReference"));
				if (element != null) {
					element = element.getChildElement(
							new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "Embedded"));
					if (element != null) {
						element = element.getChildElement(
								BinarySecurity.TOKEN_BST);
						if (element != null) {
							try {
								PKIPathSecurity bstToken = new PKIPathSecurity(element);
								return bstToken.getX509Certificates(false, 
									new edu.virginia.vcgr.genii.client.comm.axis.security.FlexibleBouncyCrypto());
						    } catch (GenesisIISecurityException e) {
								throw new GeneralSecurityException(e.getMessage(), e);
							} catch (WSSecurityException e) {
								throw new GeneralSecurityException(e.getMessage(), e);
							} catch (IOException e) {
								throw new GeneralSecurityException(e.getMessage(), e);
							} catch (CredentialException e) {
								throw new GeneralSecurityException(e.getMessage(), e);
							}				
						}
					}
				}
			}
		}
		
		return null;		
	}				
	
	static public MessageLevelSecurity extractMinMessageSecurity(EndpointReferenceType epr) throws GeneralSecurityException {

		MetadataType mdt = epr.getMetadata();
		if (mdt == null)
			return null;
		MessageElement []elements = mdt.get_any();
		if (elements == null || elements.length == 0)
			return null;

		RequiredMessageSecurityType minSec = null;
		for (MessageElement element : elements)
		{
			if (element.getQName().equals(
					RequiredMessageSecurityType.getTypeDesc().getXmlType()))
			{
				try {
					minSec = (RequiredMessageSecurityType) element.getObjectValue(RequiredMessageSecurityType.class);
				} catch (Exception e) {
					e.printStackTrace(System.err);
					return null;
				}
				break;
			}
		}

		if (minSec == null) {
			return null;
		}
		
		RequiredMessageSecurityTypeMin minType = minSec.getMin();
		if (minType == null) {
			return null;
		}
		
		return new MessageLevelSecurity(minType.getValue());
	}	
	
	static private Pattern _SERVICE_NAME_PATTERN =
		Pattern.compile("^.*/axis/services/([^/]+)$");
	
	static public String extractServiceName(EndpointReferenceType epr)
		throws AxisFault
	{
		return extractServiceName(epr.getAddress().get_value().toString());
	}
	
	static public String extractServiceName(String url)
		throws AxisFault
	{
		Matcher matcher = _SERVICE_NAME_PATTERN.matcher(url);
		if (!matcher.matches())
			throw new AxisFault("Can't locate target service \""
				+ url + "\".");
		return matcher.group(1);
	}
	
	static public byte[] toBytes(EndpointReferenceType epr)
		throws ResourceException
	{
		ByteArrayOutputStream baos = null;
		
		try
		{
			baos = new ByteArrayOutputStream();
			OutputStreamWriter writer =  new OutputStreamWriter(baos);
			ObjectSerializer.serialize(writer, epr,
				new QName(GenesisIIConstants.GENESISII_NS, "endpoint"));
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
	
	static public EndpointReferenceType fromBytes(byte []data)
		throws ResourceException
	{
		ByteArrayInputStream bais = null;
		
		try
		{
			bais = new ByteArrayInputStream(data);
			return (EndpointReferenceType)ObjectDeserializer.deserialize(
				new InputSource(bais),
				EndpointReferenceType.class);
		}
		finally
		{
			StreamUtils.close(bais);
		}
	}
	
	static public QName[] getImplementedPortTypes(
		EndpointReferenceType epr)
	{
		MetadataType mdt = epr.getMetadata();
		if (mdt == null)
			return null;
		MessageElement []any = mdt.get_any();
		if (any == null || any.length == 0)
			return null;

		ArrayList<QName> tRet = new ArrayList<QName>();
		for (MessageElement element : any)
		{
			if (element.getQName().equals(
				GenesisIIConstants.IMPLEMENTED_PORT_TYPES_ATTR_QNAME))
			{
				String s = element.getValue().toString();
				int firstIndex = s.indexOf('{');
				int secondIndex = s.indexOf('}');
				if (firstIndex >= 0 && firstIndex < secondIndex)
				{
					tRet.add(new QName(
						s.substring(firstIndex + 1,
							secondIndex), s.substring(secondIndex + 1)));
				} else
					tRet.add(new QName(s));
			}
		}
		
		QName []ret = new QName[tRet.size()];
		tRet.toArray(ret);
		return ret;
	}
	
	static public EndpointReferenceType readEPR(String filename)
		throws IOException
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(filename);
			return (EndpointReferenceType)ObjectDeserializer.deserialize(
				new InputSource(fin), EndpointReferenceType.class);
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	static public EndpointReferenceType makeUnboundEPR(EndpointReferenceType epr)
	{
		return new EndpointReferenceType(new org.ws.addressing.AttributedURIType(WSName.UNBOUND_ADDRESS),
				epr.getReferenceParameters(),
				epr.getMetadata(),
				epr.get_any());
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
}
