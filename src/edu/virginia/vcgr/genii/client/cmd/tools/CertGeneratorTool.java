package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.SocketException;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;

import java.util.Date;
import java.util.Enumeration;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;
import org.bouncycastle.asn1.x509.X509Name;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.client.security.x509.CertGeneratorUtils;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;
import edu.virginia.vcgr.genii.certGenerator.CertificateChainType;
import edu.virginia.vcgr.genii.certGenerator.X509NameType;
import edu.virginia.vcgr.genii.certGenerator.PublicKeyType;
import edu.virginia.vcgr.genii.certGenerator.CertGeneratorPortType;
import edu.virginia.vcgr.genii.certGenerator.GenerateX509V3CertificateChainRequestType;
import edu.virginia.vcgr.genii.certGenerator.GenerateX509V3CertificateChainResponseType;

public class CertGeneratorTool extends BaseGridTool 
{
	static private final String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dcert-generator";
	static private final String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/ucert-generator";

	static private Log _logger = LogFactory.getLog(CertGeneratorTool.class);
	
	static private final String _LOCAL_MACHINE_NAME_CN = "LOCAL_MACHINE_NAME";
	static private final String _LOCAL_MACHINE_IP_CN = "LOCAL_MACHINE_IP";

	private boolean _create_generator = false;
	private boolean _gen_cert = false;
	private boolean _url = false;
	
	private String _ks_path; 
	private String _ks_password; 
	private String _entry_password; 
	private String _ks_alias; 
	private Long _default_validity = new Long(1000L * 60 * 60 * 24 * (365 * 12 + 3));
	private String _path_for_cert_generator_factory = null;
	private String _path_for_cert_generator = null;
	private String _cn = _LOCAL_MACHINE_NAME_CN;
	private String _c = "US";
	private String _st = "Virginia";
	private String _l = null;
	private String _o = null;
	private String _ou = null;
	private String _email= null;
	private Integer _keySize = null;

	public CertGeneratorTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE_RESOURCE), false,
				ToolCategory.SECURITY);
	}
	
	@Option({"create-generator"})
	public void setCreate_generator()
	{
		_create_generator = true;
	}
	
	@Option({"gen-cert"})
	public void setGen_cert()
	{
		_gen_cert = true;
	}
	
	@Option({"keysize"})
	public void setKeysize(String keySizeStr) {
		_keySize = Integer.parseInt(keySizeStr);
	}
	
	@Option({"url"})
	public void setUrl()
	{
		_url = true;
	}
	
	@Option({"default-validity"})
	public void setDefault_validity(String default_validity)
	{
		_default_validity = new Long(default_validity);
	}
	
	@Option({"ks-path"})
	public void setKs_path(String ks_path)
	{
		_ks_path = ks_path;
	}
	
	@Option({"ks-pword"})
	public void setKs_pword(String ks_pword)
	{
		_ks_password = ks_pword;
	}
	
	@Option({"entry-pword"})
	public void setEntry_pword(String entry_pword)
	{
		_entry_password = entry_pword;
	}
	
	@Option({"ks-alias"})
	public void setKs_alias(String ks_alias)
	{
		_ks_alias = ks_alias;
	}
	
	@Option({"cn"})
	public void setCn(String cn)
	{
		_cn = cn;
	}
	
	@Option({"c"})
	public void setC(String c)
	{
		_c = c;
	}
	
	@Option({"st"})
	public void setSt(String st)
	{
		_st = st;
	}
	
	@Option({"l"})
	public void setL(String l)
	{
		_l = l;
	}
	
	@Option({"o"})
	public void setO(String o)
	{
		_o = o;
	}
	
	@Option({"ou"})
	public void setOu(String ou)
	{
		_ou = ou;
	}
	
	@Option({"email"})
	public void setEmail(String email)
	{
		_email = email;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		int numArgs = numArguments();
		if (_create_generator)
		{
			_path_for_cert_generator_factory = getArgument(0);
			if (numArgs > 1)
				_path_for_cert_generator = getArgument(1);
			createGenerator(_path_for_cert_generator_factory, _url, _path_for_cert_generator, _ks_path, _ks_password, _ks_alias, _entry_password, _default_validity);
		}
		
		if (_gen_cert)
		{
			_path_for_cert_generator = getArgument(0);
			
			if (_keySize == null) {
				_keySize = ClientUtils.getClientRsaKeyLength();
			}
			KeyPair newKeyPair = CertTool.generateKeyPair(_keySize);
			X509Certificate [] certChain = createCert(newKeyPair, _path_for_cert_generator, _cn, _c, _st, _l, _o, _ou, _email);
			storeCert(newKeyPair, certChain, _ks_path, _ks_password, _ks_alias, _entry_password);
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();

		// make sure 1 and only one command type specified.
		if ((_gen_cert && _create_generator) || (!_gen_cert && !_create_generator))
			throw new InvalidToolUsageException();
		
		if (_create_generator)
		{
			if (numArgs < 1 || numArgs > 2)
				throw new InvalidToolUsageException();
			if (_ks_path == null || _ks_password == null || _ks_alias == null)
				throw new InvalidToolUsageException();
			return;
		}
		
		if (_gen_cert)
		{
			if (numArgs != 1)
				throw new InvalidToolUsageException();
			if (_ks_path == null || _ks_password == null || _ks_alias == null)
				throw new InvalidToolUsageException();
			return;
		}
	}
	
	public EndpointReferenceType createGenerator(String service, boolean url,
		String optTargetName, String issuerCertKSPath, 
		String issuerCertKSPassword, String issuerCertAlias, 
		String issuerCertEntryPassword, Long defaultValidity) 
		throws IOException, RNSException, CreationException, KeyStoreException,
			GeneralSecurityException, InvalidToolUsageException
	{
		EndpointReferenceType epr;
		PrivateKey issuerPrivateKey = null;

		String keyStoreType = "PKCS12";
		char[] keyStorePassChars = null;
		if (issuerCertKSPassword != null) {
			keyStorePassChars = issuerCertKSPassword.toCharArray();
		}
		
		char[] entryPassChars = null;
		if (issuerCertEntryPassword != null) {
			entryPassChars = issuerCertEntryPassword.toCharArray();
		}

		File ksFile = new File(issuerCertKSPath);
		if (!ksFile.exists())
		{
			throw new CreationException("Key store file " + issuerCertKSPath + " does not exist");
		}
		if (!ksFile.isFile())
		{
			throw new CreationException("Key store path " + issuerCertKSPath + " is not a file");
		}

		KeyStore keyStore = CertTool.openStoreDirectPath(ksFile, keyStoreType, keyStorePassChars);
		
		if (keyStore == null)
		{
			throw new CreationException("Could not open key store at path " + issuerCertKSPath);
		}

		if (!keyStore.containsAlias(issuerCertAlias))
		{
			throw new CreationException("Alias " + issuerCertAlias + " not found in key store " + issuerCertKSPath);
		}

		Enumeration<String> aliases = keyStore.aliases();
		while(aliases.hasMoreElements())
		{
			String nextAlias = aliases.nextElement();
			System.out.println(nextAlias + "\n");
		}

    	// load the signing cert/private key and generate a client cert
	    issuerPrivateKey = (PrivateKey) keyStore.getKey(
	    		issuerCertAlias, 
	    		entryPassChars);
	
	    Certificate[] chain = keyStore.getCertificateChain(issuerCertAlias);
    
	    if (chain == null)
		{
			throw new CreationException("Could not retrieve cert " + issuerCertAlias + " from key store at path " + issuerCertKSPath);
		}
		
		X509Certificate [] issuerCertChain = new X509Certificate[chain.length];
		for (int i = 0; i < chain.length; i++)
			issuerCertChain[i] = (X509Certificate) chain[i];
		
		MessageElement[] createProps = CertGeneratorUtils.createCreationProperties(
				issuerCertChain, issuerPrivateKey, defaultValidity);
		
		if (!url)
		{
			RNSPath path = RNSPath.getCurrent();
			path = path.lookup(service, RNSPathQueryFlags.MUST_EXIST);
			epr = CreateResourceTool.createInstance(path.getEndpoint(), 
				(optTargetName == null) ? null : new GeniiPath(optTargetName),
				createProps);
		}
		else
		{
			epr = CreateResourceTool.createInstance(EPRUtils.makeEPR(service),
				(optTargetName == null) ? null : new GeniiPath(optTargetName),
				createProps);
		}
		return epr;
	}
	
	static public X509Certificate [] createCert(
		KeyPair newKeyPair,
		String generatorPath,
		String cn, 
		String c, 
		String st, 
		String l, 
		String o, 
		String ou, 
		String email) 
		throws IOException, RNSException, CreationException, 
			GeneralSecurityException
	{
		RNSPath path = RNSPath.getCurrent();
		path = path.lookup(generatorPath, RNSPathQueryFlags.MUST_EXIST);
		CertGeneratorPortType certGenerator = ClientUtils.createProxy(CertGeneratorPortType.class, path.getEndpoint());
		GenerateX509V3CertificateChainRequestType request = new GenerateX509V3CertificateChainRequestType();
		X509Name name = generateX509Name(cn, c, st, l, o, ou, email);
		X509NameType nameType = new X509NameType(name.toString());
		request.setX509Name(nameType);
		PublicKey newPublicKey = newKeyPair.getPublic();
		PublicKeyType newPublicKeyType = new PublicKeyType(SecurityUtils.serializePublicKey(newPublicKey));
		request.setPublicKey(newPublicKeyType);
		GenerateX509V3CertificateChainResponseType response = certGenerator.generateX509V3CertificateChain(request);
		CertificateChainType newCertChainType = response.getCertificateChain();
		X509Certificate [] newCertChain = SecurityUtils.decodeCertificateChain(newCertChainType);

		X509Certificate newCert = newCertChain[0];
		newCert.checkValidity(new Date());
		
		return newCertChain;
	}

	static public void storeCert(
		KeyPair keyPair,
		X509Certificate [] certChain,
		String keyStorePath,
		String keyStorePassword,
		String keyAlias,
		String entryPassword)
		throws IOException, GeneralSecurityException
	{
		String keyStoreType = "PKCS12";
			
		char[] keyStorePassChars = null;
		if (keyStorePassword != null) {
			keyStorePassChars = keyStorePassword.toCharArray();
		}
		
		char[] entryPassChars = null;
		if (entryPassword != null) {
			entryPassChars = entryPassword.toCharArray();
		}
		
		KeyStore ks = KeyStore.getInstance(keyStoreType, "BC");
		File outFile = new File(keyStorePath);
		if (outFile.exists()) {
			FileInputStream fis = new FileInputStream(keyStorePath);
			ks.load(fis, keyStorePassChars);
			fis.close();
		} else {
			ks.load(null, keyStorePassChars);
		}
            
		ks.setKeyEntry(keyAlias, keyPair.getPrivate(), entryPassChars, certChain);

		FileOutputStream fos = new FileOutputStream(keyStorePath);
		ks.store(fos, keyStorePassChars);
		fos.close();
	}
	
	
	static public X509Name generateX509Name(String cn, String c, String st, String l, String o, String ou, String email) throws SocketException
	{
		String nameString = "";
		
		if (cn.equals(_LOCAL_MACHINE_NAME_CN))
			cn = determineLocalMachineName();
		else if (cn.equals(_LOCAL_MACHINE_IP_CN))
			cn = determineLocalMachineIP();

		nameString += "CN=" + cn;
		
		if (c != null && c != "")
			nameString += ", C=" + c;
		
		if (st != null && st != "")
			nameString += ", ST=" + st;
		
		if (l != null && l != "")
			nameString += ", L=" + l;
		
		if (o != null && o != "")
			nameString += ", O=" + o;
		
		if (ou != null && ou != "")
			nameString += ", OU=" + ou;
		
		if (email != null && email != "")
			nameString += ", EMAIL=" + email;
		
		return new X509Name(nameString);
	}
	
	static private String determineLocalMachineName() throws SocketException
	{
		return Hostname.getMostGlobal().getCanonicalHostName();		
	}

	static private String determineLocalMachineIP() throws SocketException
	{
		return Hostname.getMostGlobal().getHostAddress();		
	}
}
