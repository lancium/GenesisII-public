package edu.virginia.vcgr.genii.client.security;

import java.io.IOException;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;

public class CertGeneratorUtils
{
	static public final long _DEFAULT_VALIDITY = (1000L*60*60*24*(365*12+4));
	static public QName CERT_GENERATOR_DEFAULT_VALIDITY_CONSTRUCTION_PARAMETER =
		new QName(GenesisIIConstants.GENESISII_NS, "cert-generator-default-validity");
	static public QName CERT_GENERATOR_ISSUER_CHAIN_CONSTRUCTION_PARAMETER =
		new QName(GenesisIIConstants.GENESISII_NS, "cert-generator-issuer-chain");
	static public QName CERT_GENERATOR_ISSUER_PRIVATE_KEY_CONSTRUCTION_PARAMETER =
		new QName(GenesisIIConstants.GENESISII_NS, "cert-generator-issuer-private-key");

	static public void insertCertGeneratorParameters(
		HashMap<QName, MessageElement> parameters,
		Long defaultValidity,
		X509Certificate [] issuerChain,
		PrivateKey issuerPrivateKey)
		throws IOException
	{
		parameters.put(CERT_GENERATOR_DEFAULT_VALIDITY_CONSTRUCTION_PARAMETER,
			new MessageElement(
				CERT_GENERATOR_DEFAULT_VALIDITY_CONSTRUCTION_PARAMETER,
				defaultValidity.toString()));
		parameters.put(CERT_GENERATOR_ISSUER_CHAIN_CONSTRUCTION_PARAMETER,
				new MessageElement(
				CERT_GENERATOR_ISSUER_CHAIN_CONSTRUCTION_PARAMETER, DBSerializer.serialize(issuerChain)));
		parameters.put(CERT_GENERATOR_ISSUER_PRIVATE_KEY_CONSTRUCTION_PARAMETER,
				new MessageElement(
					CERT_GENERATOR_ISSUER_PRIVATE_KEY_CONSTRUCTION_PARAMETER,
					DBSerializer.serialize(issuerPrivateKey)));
	}
	
	static public MessageElement[] createCreationProperties(
		X509Certificate [] issuerChain,
		PrivateKey issuerPrivateKey,
		Long defaultValidity)
		throws IOException
	{
		MessageElement []any = new MessageElement[3];
		any[0] = new MessageElement(CERT_GENERATOR_ISSUER_CHAIN_CONSTRUCTION_PARAMETER, DBSerializer.serialize(issuerChain));
		any[1] = new MessageElement(CERT_GENERATOR_ISSUER_PRIVATE_KEY_CONSTRUCTION_PARAMETER, DBSerializer.serialize(issuerPrivateKey));
		if (defaultValidity == null)
			defaultValidity = new Long(_DEFAULT_VALIDITY);
		any[2] = new MessageElement(CERT_GENERATOR_DEFAULT_VALIDITY_CONSTRUCTION_PARAMETER, defaultValidity.toString());
		
		return any;
	}
	
	static public Long getDefaultValidity(
			HashMap<QName, MessageElement> parameters)
		throws IOException, Exception
	{
		MessageElement elem = parameters.get(CERT_GENERATOR_DEFAULT_VALIDITY_CONSTRUCTION_PARAMETER);
		return (Long) (elem.getObjectValue(Long.class));
	}

	static public X509Certificate [] getIssuerChain(
		HashMap<QName, MessageElement> parameters)
		throws IOException, Exception
	{
		MessageElement elem = parameters.get(CERT_GENERATOR_ISSUER_CHAIN_CONSTRUCTION_PARAMETER);
		return (X509Certificate []) (elem.getObjectValue(X509Certificate[].class));
	}

	static public PrivateKey getIssuerPrivateKey(
		HashMap<QName, MessageElement> parameters)
		throws IOException, Exception
	{
		MessageElement elem = parameters.get(CERT_GENERATOR_ISSUER_PRIVATE_KEY_CONSTRUCTION_PARAMETER);
		return (PrivateKey) (elem.getObjectValue(PrivateKey.class));
	}
}