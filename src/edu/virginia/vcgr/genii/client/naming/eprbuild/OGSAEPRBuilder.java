package edu.virginia.vcgr.genii.client.naming.eprbuild;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.axis.message.MessageElement;
import org.morgan.util.configuration.ConfigurationException;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.security.WSSecurityUtils;

public class OGSAEPRBuilder extends WSNamingEPRBuilder
{
	private X509Certificate []_certificateChain = null;
	private Collection<MessageElement> _policies = 
		new LinkedList<MessageElement>();
	private boolean _requireEncryption = false;
	private boolean _includeServerTls = false;
	private boolean _requireSigning = false;
	
	private Collection<MessageElement> policies()
	{
		Collection<MessageElement> ret = new LinkedList<MessageElement>(
			_policies);
		
		if (_requireEncryption)
			ret.add(SecurityPolicy.requireEncryptionPolicy());
		
		if (_includeServerTls)
			ret.add(SecurityPolicy.includeServerTls());
		
		if (_requireSigning)
			ret.addAll(SecurityPolicy.requireMessageSigning());
		
		return ret;
	}
	
	public OGSAEPRBuilder(URI address)
	{
		super(address);
	}
	
	final public void certificateChain(X509Certificate []certificateChain)
	{
		_certificateChain = certificateChain;
	}
	
	final public X509Certificate[] certificateChain()
	{
		return _certificateChain;
	}
	
	final public void addUsernamePasswordTokenPolicy(boolean isOptional)
	{
		_policies.add(SecurityPolicy.usernamePasswordPolicy(isOptional));
	}
	
	final public void requireEncryption(boolean requireEncryption)
	{
		_requireEncryption = requireEncryption;
	}
	
	final public boolean requireEncryption()
	{
		return _requireEncryption;
	}
	
	final public void includeServerTls(boolean includeServerTls)
	{
		_includeServerTls = includeServerTls;
	}
	
	final public boolean includeServerTls()
	{
		return _includeServerTls;
	}
	
	final public void requireMessageSigning(boolean requireSigning)
	{
		_requireSigning = requireSigning;
	}
	
	final public boolean requireMessageSigning()
	{
		return _requireSigning;
	}
	
	@Override
	public Collection<Element> metadata()
	{
		Collection<Element> ret = new LinkedList<Element>(super.metadata());
		
		/* Add certificate chain */
		if (_certificateChain != null)
		{
			try
			{
				ret.add(WSSecurityUtils.makePkiPathSecTokenRef(
					_certificateChain, "RecipientMessageIdentity"));
			}
			catch (GeneralSecurityException gse)
			{
				throw new ConfigurationException(
					"Unable to generate certificate chain metadata!", gse);
			}
		}
		
		/* Add policy information */
		Collection<MessageElement> policies = policies();
		if (!policies.isEmpty())
			ret.add(SecurityPolicy.constructMetaPolicy(policies));
		
		return ret;
	}
}
