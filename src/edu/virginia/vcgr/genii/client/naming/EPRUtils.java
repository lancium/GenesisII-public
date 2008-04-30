package edu.virginia.vcgr.genii.client.naming;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import org.apache.axis.types.URI;
import java.net.URL;
import java.sql.Blob;
import javax.sql.rowset.serial.SerialBlob;
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
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.ogsa.OGSARP;
import edu.virginia.vcgr.genii.client.ogsa.OGSAWSRFBPConstants;
import edu.virginia.vcgr.genii.client.resource.AttributedURITypeSmart;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
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
			OGSARP rp = (OGSARP)ResourcePropertyManager.createRPInterface(
				epr, OGSARP.class);
			return rp.getResourceEndpoint();
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
				catch (URI.MalformedURIException e)
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
					return WSSecurityUtils.getChainFromPkiPathSecTokenRef(element);
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
		return (EndpointReferenceType)ObjectDeserializer.deserialize(
			new InputSource(in),
			EndpointReferenceType.class);
	}

	static public Blob toBlob(EndpointReferenceType epr)
		throws ResourceException
	{
		if (epr == null)
			return null;
		
		try 
		{
			return new SerialBlob(toBytes(epr));
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
		catch(Throwable t)
		{
			throw new ResourceException("Could not deserialize EPR from BLOB", t);
		}
		finally
		{
			StreamUtils.close(in);
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
				OGSAWSRFBPConstants.WS_RESOURCE_INTERFACES_ATTR_QNAME))
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
	
	static public boolean isUnboundEPR(EndpointReferenceType epr) {
		return ((epr.getAddress() == null) ||
				(epr.getAddress().get_value() == null) ||
				(epr.getAddress().get_value().toString().equals(WSName.UNBOUND_ADDRESS)));
	}
	
	static public EndpointReferenceType makeUnboundEPR(EndpointReferenceType epr)
	{
		return new EndpointReferenceType(
				new org.ws.addressing.AttributedURIType(WSName.UNBOUND_ADDRESS),
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
