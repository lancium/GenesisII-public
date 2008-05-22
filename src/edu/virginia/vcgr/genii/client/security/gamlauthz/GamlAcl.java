package edu.virginia.vcgr.genii.client.security.gamlauthz;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.*;

public class GamlAcl implements Serializable
{

	static final long serialVersionUID = 0L;

	public boolean requireEncryption = false;
	public ArrayList<Identity> readAcl = new ArrayList<Identity>();
	public ArrayList<Identity> writeAcl = new ArrayList<Identity>();
	public ArrayList<Identity> executeAcl = new ArrayList<Identity>();

	public GamlAcl()
	{
	}

	static public ArrayList<Identity> decodeIdentityList(
			GamlIdentityListType identityList) throws AuthZSecurityException
	{

		ArrayList<Identity> retval = new ArrayList<Identity>();
		if (identityList == null)
		{
			return retval;
		}

		try
		{

			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			for (IdentityType identityType : identityList.getIdentity())
			{

				if (identityType == null)
				{
					retval.add(null);
				}
				else if (identityType instanceof X509IdentityType)
				{

					X509IdentityType idt = (X509IdentityType) identityType;
					int numCerts = idt.getCertificateChain().getCount();
					X509Certificate[] certChain = new X509Certificate[numCerts];
					for (int i = 0; i < numCerts; i++)
					{
						InputStream inputStream =
								new ByteArrayInputStream(idt
										.getCertificateChain()
										.getCertificate(i));
						certChain[i] =
								(X509Certificate) cf
										.generateCertificate(inputStream);
					}
					retval.add(new X509Identity(certChain));

				}
				else if (identityType instanceof UTIdentityType)
				{

					UTIdentityType idt = (UTIdentityType) identityType;
					String name = idt.getName();
					String token = idt.getToken();
					retval.add(new UsernamePasswordIdentity(name, token));
				}
			}

		}
		catch (Exception e)
		{
			throw new AuthZSecurityException(
					"Unable to load GAML AuthZ config: " + e.getMessage(), e);
		}

		return retval;

	}

	static public GamlIdentityListType encodeIdentityList(
			ArrayList<Identity> acl) throws AuthZSecurityException
	{

		try
		{
			IdentityType[] idtList = new IdentityType[acl.size()];

			for (int i = 0; i < acl.size(); i++)
			{
				if (acl.get(i) == null)
				{
					idtList[i] = null;
				}
				else if (acl.get(i) instanceof X509Identity)
				{
					X509Identity idt = (X509Identity) acl.get(i);
					X509Certificate[] certList =
							idt.getAssertingIdentityCertChain();
					byte[][] certListBytes = new byte[certList.length][];
					for (int j = 0; j < certList.length; j++)
					{
						certListBytes[j] = certList[j].getEncoded();
					}
					idtList[i] =
							new X509IdentityType(new CertificateChainType(
									certList.length, certListBytes));

				}
				else if (acl.get(i) instanceof UsernamePasswordIdentity)
				{
					UsernamePasswordIdentity idt =
							(UsernamePasswordIdentity) acl.get(i);
					idtList[i] =
							new UTIdentityType(idt.getUserName(), idt
									.getPassword());
				}
			}

			return new GamlIdentityListType(idtList.length, idtList);
		}
		catch (GeneralSecurityException e)
		{
			throw new AuthZSecurityException(
					"Unable to load GAML AuthZ config: " + e.getMessage(), e);
		}
	}

	static public GamlAcl decodeAcl(AuthZConfig config)
			throws AuthZSecurityException
	{

		try
		{
			MessageElement aclMel = null;
			MessageElement[] anys = config.get_any();
			if (anys == null)
			{
				return null;
			}
			for (MessageElement mel : anys)
			{

				if (mel.getQName().equals(
						GamlAclType.getTypeDesc().getXmlType()))
				{

					if (aclMel != null)
					{
						// we already found one
						throw new AuthZSecurityException(
								"Invalid GAML AuthZ config");
					}

					aclMel = mel;
				}
			}

			if (aclMel == null)
			{
				throw new AuthZSecurityException("Invalid GAML AuthZ config");
			}

			GamlAclType gacl =
					(GamlAclType) aclMel.getObjectValue(GamlAclType.class);

			GamlAcl retval = new GamlAcl();
			if (gacl.getRequireEncryption() != null)
			{
				retval.requireEncryption =
						gacl.getRequireEncryption().booleanValue();
			}

			if (gacl.getReadAcl() != null)
			{
				retval.readAcl = decodeIdentityList(gacl.getReadAcl());
			}
			if (gacl.getWriteAcl() != null)
			{
				retval.writeAcl = decodeIdentityList(gacl.getWriteAcl());
			}
			if (gacl.getExecuteAcl() != null)
			{
				retval.executeAcl = decodeIdentityList(gacl.getExecuteAcl());
			}

			return retval;

		}
		catch (Exception e)
		{
			throw new AuthZSecurityException(
					"Unable to load GAML AuthZ config: " + e.getMessage(), e);
		}
	}

	static public AuthZConfig encodeAcl(GamlAcl acl)
			throws AuthZSecurityException
	{

		GamlAclType gacl = new GamlAclType();
		gacl.setRequireEncryption(new Boolean(acl.requireEncryption));
		gacl.setReadAcl(encodeIdentityList(acl.readAcl));
		gacl.setWriteAcl(encodeIdentityList(acl.writeAcl));
		gacl.setExecuteAcl(encodeIdentityList(acl.executeAcl));

		MessageElement mel[] =
				{ new MessageElement(GamlAclType.getTypeDesc().getXmlType(),
						gacl) };
		return new AuthZConfig(mel);
	}
}
