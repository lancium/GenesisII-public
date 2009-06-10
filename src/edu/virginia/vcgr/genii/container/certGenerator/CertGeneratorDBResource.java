package edu.virginia.vcgr.genii.container.certGenerator;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.HashMap;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.x509.CertGeneratorUtils;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class CertGeneratorDBResource extends BasicDBResource implements ICertGeneratorResource
{
	static public final String _DEFAULT_VALIDITY_PROPERTY_NAME =	"cert-generator-default-validity";
	static public final String _ISSUER_CERT_CHAIN_PROPERTY_NAME =	"cert-generator-issuer-cert-chain";
	static public final String _ISSUER_PRIVATE_KEY_PROPERTY_NAME =	"cert-generator-issuer-private-key";
	
	public CertGeneratorDBResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
	public void setCertificateIssuerInfo(HashMap<QName, Object> creationParameters) 
		throws ResourceException
	{
		setProperty(_DEFAULT_VALIDITY_PROPERTY_NAME,
			creationParameters.get(
				CertGeneratorUtils.CERT_GENERATOR_DEFAULT_VALIDITY_CONSTRUCTION_PARAMETER).toString());
		X509Certificate [] issuerCertChain = (X509Certificate []) creationParameters.get(CertGeneratorUtils.CERT_GENERATOR_ISSUER_CHAIN_CONSTRUCTION_PARAMETER);
		setProperty(_ISSUER_CERT_CHAIN_PROPERTY_NAME, issuerCertChain);
		PrivateKey issuerPrivateKey = (PrivateKey) creationParameters.get(CertGeneratorUtils.CERT_GENERATOR_ISSUER_PRIVATE_KEY_CONSTRUCTION_PARAMETER);
		setProperty(_ISSUER_PRIVATE_KEY_PROPERTY_NAME, issuerPrivateKey);
	}

	public Long getDefaultValidity() 
		throws ResourceException
	{
		return new Long((String) getProperty(_DEFAULT_VALIDITY_PROPERTY_NAME));
	}

	public X509Certificate[] getIssuerChain() 
		throws ResourceException
	{
		return (X509Certificate[]) getProperty(_ISSUER_CERT_CHAIN_PROPERTY_NAME);
	}
	
	public PrivateKey getIssuerPrivateKey() 
		throws ResourceException
	{
		return (PrivateKey) getProperty(_ISSUER_PRIVATE_KEY_PROPERTY_NAME);
	}

	
}