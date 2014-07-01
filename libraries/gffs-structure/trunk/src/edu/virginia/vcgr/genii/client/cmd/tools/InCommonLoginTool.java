package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.dialog.ComboBoxDialog;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.DialogFactory;
import edu.virginia.vcgr.genii.client.dialog.DialogProvider;
import edu.virginia.vcgr.genii.client.dialog.InputDialog;
import edu.virginia.vcgr.genii.client.dialog.MenuItem;
import edu.virginia.vcgr.genii.client.dialog.SimpleMenuItem;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.incommon.CILogonClient;
import edu.virginia.vcgr.genii.client.incommon.CILogonParameters;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

public class InCommonLoginTool extends BaseLoginTool
{
	private static final String STANDARD_PREFIX = "/users/incommon.org/";

	private static final String _DESCRIPTION = "config/tooldocs/description/diclogin";
	private static final String _USAGE = "config/tooldocs/usage/uiclogin";
	private static final String _MANPAGE = "config/tooldocs/man/iclogin";

	static private Log _logger = LogFactory.getLog(InCommonLoginTool.class);

	private boolean _silent = false;
	private boolean _verbose = false;

	private int _lifetime = 24;

	private String _idpUrl = null;
	private String _CSRFileName = null;
	private String _CSRKeyFileName = null;
	private CILogonParameters _params;

	@Option({ "csr", "c" })
	public void setCSR(String csrFile)
	{
		_CSRFileName = csrFile;
	}

	@Option({ "key", "k" })
	public void setKey(String keyFile)
	{
		_CSRKeyFileName = keyFile;
	}

	@Option({ "idp", "i" })
	public void setIdpUrl(String url)
	{
		_idpUrl = url;
	}

	@Option({ "silent", "s" })
	public void setSilent(boolean silent)
	{
		_silent = true;
	}

	@Option({ "verbose", "v" })
	public void setVerbose(boolean verbose)
	{
		_verbose = true;
	}

	@Option({ "lifetime", "l" })
	public void setLifetime(int lifetime)
	{
		_lifetime = lifetime;
	}

	public InCommonLoginTool()
	{
		super(_DESCRIPTION, _USAGE, false);
		overrideCategory(ToolCategory.SECURITY);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected void verify() throws ToolException
	{
		_params = getParams(_username, _password, _idpUrl, _CSRFileName, _CSRKeyFileName);

		if (_params == null) {
			throw new InvalidToolUsageException(usage());
		}
		_params.verbose = _verbose;
		_params.silent = _silent;
		_params.lifetime = _lifetime;
	}

	@Override
	protected int runCommand()
	{
		ICallingContext callContext = null;
		X509Certificate cert = null;
		TransientCredentials transientCredentials = null;
		String targetPath = null;
		try {
			// get the local identity's key material (or create one if necessary)
			callContext = ContextManager.getCurrentContext();
			if (callContext == null) {
				callContext = new CallingContextImpl(new ContextType());
			}

			// Call CILogon for an InCommon certificate
			cert = callCILogon(_params);

			if (cert == null) {
				stdout.println("Didn't get a cert back, bailing out");
				return -1;
			}

			// Set the InCommon cert as the new session id
			KeyAndCertMaterial clientKeyMaterial = new KeyAndCertMaterial(new X509Certificate[] { cert }, _params.key);
			callContext.setActiveKeyAndCertMaterial(clientKeyMaterial);
			ContextManager.storeCurrentContext(callContext);

			// TODO Needs to be adjusted after permanent policy location is determined.
			targetPath = STANDARD_PREFIX + _username;

			// we're going to use the WS-TRUST token-issue operation to log in to a security tokens
			// service.
			RNSPath authnPath = callContext.getCurrentPath().lookup(targetPath, RNSPathQueryFlags.MUST_EXIST);
			EndpointReferenceType epr = authnPath.getEndpoint();

			// log in to the target STS using the InCommon cert as the base credential
			ArrayList<NuCredential> creds =
				IDPLoginTool.doIdpLogin(epr, _credentialValidMillis, clientKeyMaterial._clientCertChain);
			if (creds != null) {
				// insert the target credential into the calling context
				transientCredentials = TransientCredentials.getTransientCredentials(callContext);
				transientCredentials.addAll(creds);
			}

			return 0;

		} catch (AuthZSecurityException e) {
			_logger.error("Can't set client session cert. See stack trace for details.", e);
			stderr.println("Login failed, see logs for additional details.");
		} catch (RNSPathDoesNotExistException e) {
			_logger.error("Target STS path " + targetPath + " doesn't exist. See stack trace for details.", e);
			stderr.println("Login failed, see logs for additional details.");
		} catch (RNSPathAlreadyExistsException e) {
			_logger.error("We should never get this exception... What did you do!?! See stack trace for details.", e);
			stderr.println("Login failed, see logs for additional details.");
		} catch (IOException e) {
			_logger.error("Couldn't access calling context. See stack trace for details.", e);
			stderr.println("Login failed, see logs for additional details.");
		} catch (Throwable e) {
			_logger.error("Failed IDPLogin to " + targetPath + " or other general exception. See stack trace for details.", e);
			stderr.println("Login failed, see logs for additional details.");
		} finally {
			// save the current state of the calling context before quitting
			if (callContext != null) {
				try {
					ContextManager.storeCurrentContext(callContext);
				} catch (IOException e) {
					_logger.error("Couldn't access calling context. See stack trace for details.", e);
					stderr.println("Login failed, see logs for additional details.");
				}
			}
		}
		return -1;
	}

	private X509Certificate callCILogon(CILogonParameters params)
	{
		CILogonClient client = new CILogonClient(params);
		try {

			String result = client.call();

			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(IOUtils.toInputStream(result));
			return cert;

		} catch (IOException e) {
			_logger.error("Couldn't complete InCommon call. See stack trace for details.", e);
			stderr.println("Login failed, see logs for additional details.");
		} catch (URISyntaxException e) {
			_logger.error("Couldn't complete InCommon call. See stack trace for details.", e);
			stderr.println("Login failed, see logs for additional details.");
		} catch (CertificateException e) {
			_logger.error("Couldn't convert resulting certificate. See stack trace for details.", e);
			stderr.println("Login failed, see logs for additional details.");
		}
		return null;
	}

	private CILogonParameters getParams(String username, String password, String idpUrl, String csrFileName,
		String csrKeyFileName) throws ToolException
	{
		if (username == null || password == null || idpUrl == null || csrFileName == null || csrKeyFileName == null) {
			return promptForParams();
		} else {
			CILogonParameters params =
				new CILogonParameters(idpUrl, username, password, readFile(csrFileName), parseKeyFile(csrKeyFileName)
					.getPrivate(), stdout, stderr);
			return params;
		}
	}

	private CILogonParameters promptForParams() throws ToolException
	{
		DialogProvider provider = DialogFactory.getProvider(stdout, stderr, stdin, useGui());

		if (_idpUrl == null) {
			// TODO get these from server instead of hard code
			// list hosted at: https://cilogon.org/include/ecpidps.txt
			try {
				ComboBoxDialog idpDialog =
					provider.createSingleListSelectionDialog("IDP Choice", "Please choose IDP", new SimpleMenuItem(
						"ProtectNetwork", "https://idp.protectnetwork.org/protectnetwork-idp/profile/SAML2/SOAP/ECP"),
						new SimpleMenuItem("ProtectNetwork",
							"https://idp.protectnetwork.org/protectnetwork-idp/profile/SAML2/SOAP/ECP"), new SimpleMenuItem(
							"LIGO Scientific Collaboration", "https://login.ligo.org/idp/profile/SAML2/SOAP/ECP"),
						new SimpleMenuItem("LTER Network", "https://shib.lternet.edu/idp/profile/SAML2/SOAP/ECP"),
						new SimpleMenuItem("University of Chicago",
							"https://shibboleth2.uchicago.edu/idp/profile/SAML2/SOAP/ECP"), new SimpleMenuItem(
							"University of Illinois at Urbana-Champaign",
							"https://shibboleth.illinois.edu/idp/profile/SAML2/SOAP/ECP"), new SimpleMenuItem(
							"University of Washington", "https://idp.u.washington.edu/idp/profile/SAML2/SOAP/ECP"),
						new SimpleMenuItem("University of Wisconsin-Madison",
							"https://login.wisc.edu/idp/profile/SAML2/SOAP/ECP"));
				idpDialog.showDialog();

				MenuItem response = idpDialog.getSelectedItem();
				_idpUrl = (String) response.getContent();
			} catch (DialogException e) {
				e.printStackTrace(stderr);
				_idpUrl = "https://idp.protectnetwork.org/protectnetwork-idp/profile/SAML2/SOAP/ECP";
				stdout.println("Defaulting to ProtectNetwork");
			} catch (UserCancelException e) {
				e.printStackTrace(stderr);
				return null;
			}
		}

		try {
			aquireUsername();
			aquirePassword();

		} catch (DialogException e) {
			_logger.error("Username dialog failed for some reason", e);
			throw new ToolException("Username dialog failed for some reason", e);
		} catch (UserCancelException e) {
			_logger.error("User cancelled the username request, bailing out", e);
			throw new ToolException("Cannot continue without username, bailing out", e);
		} catch (ToolException e) {
			_logger.error("Password dialog failed for some reason", e);
			throw new ToolException("Password dialog failed for some reason", e);
		}

		String csr = "";
		KeyPair key = null;

		if (_CSRKeyFileName == null) {
			// See if they want to use an existing keypair file
			promptForKeyFile();
		}

		if (_CSRKeyFileName != null && !_CSRKeyFileName.equals("")) {
			// if they provided a file name, parse it
			key = parseKeyFile(_CSRKeyFileName);
			if (_CSRFileName != null || promptForCSRFile()) {
				// since they had a keypair, maybe they have a csr too
				// read it in
				csr = readFile(_CSRFileName);
			}
		} else {
			// they didn't give us a file, so make a new keypair
			key = generateKeyPair();
		}
		if (csr == null || csr.equals("")) {
			// they didn't have a csr file, or we didn't ask because we generated the keypair
			csr = generateCSR(key);
		}

		// now that everything is populated, ship it to the caller
		return new CILogonParameters(_idpUrl, _username, _password, csr, key.getPrivate(), stdout, stderr);
	}

	private String generateCSR(KeyPair keyPair) throws ToolException
	{
		String subject = "CN=ignore";
		PKCS10CertificationRequest csr = null;
		try {
			AsymmetricKeyParameter privateKey = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
			AlgorithmIdentifier signatureAlgorithm = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1WITHRSA");
			AlgorithmIdentifier digestAlgorithm = new DefaultDigestAlgorithmIdentifierFinder().find("SHA-1");
			ContentSigner signer = new BcRSAContentSignerBuilder(signatureAlgorithm, digestAlgorithm).build(privateKey);

			PKCS10CertificationRequestBuilder csrBuilder =
				new JcaPKCS10CertificationRequestBuilder(new X500Name(subject), keyPair.getPublic());
			ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
			extensionsGenerator.addExtension(X509Extension.basicConstraints, true, new BasicConstraints(true));
			extensionsGenerator.addExtension(X509Extension.keyUsage, true,
				new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
			csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extensionsGenerator.generate());
			csr = csrBuilder.build(signer);
		} catch (IOException e) {
			_logger.error("Problem generating CSR, see stack trace", e);
			throw new ToolException("Couldn't generate a new CSR for the request", e);
		} catch (OperatorCreationException e) {
			_logger.error("Problem generating CSR, see stack trace", e);
			throw new ToolException("Couldn't generate a new CSR for the request", e);
		}

		try {
			StringWriter sw = new StringWriter();
			PEMWriter writer = new PEMWriter(sw);
			writer.writeObject(csr);
			writer.flush();
			writer.close();
			return sw.toString();
		} catch (IOException e) {
			throw new ToolException("Couldn't convert CSR to pem format", e);
		}
	}

	private String readFile(String fileName) throws ToolException
	{
		String line = null;
		String ret = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				ret += line + "\n";
			}
			br.close();
			return ret;
		} catch (IOException e) {
			throw new ToolException("Couldn't read contents of file " + fileName, e);
		}
	}

	private KeyPair generateKeyPair()
	{
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			// Should never happen, with the algorithm hard-coded here
			e.printStackTrace(stderr);
			_logger.error("Unexpected exception in " + this.getClass(), e);
			return null;
		}
		keyGen.initialize(2048, new SecureRandom());
		KeyPair keyPair = keyGen.generateKeyPair();
		return keyPair;
	}

	private void promptForKeyFile() throws ToolException
	{
		DialogProvider provider = DialogFactory.getProvider(stdout, stderr, stdin, useGui());

		InputDialog keyFileDialog;
		try {
			keyFileDialog =
				provider.createInputDialog("KeyFile", "Please enter keypair filename, or leave blank to generate one.");
			keyFileDialog.showDialog();
			_CSRKeyFileName = keyFileDialog.getAnswer();

		} catch (DialogException e) {
			_logger.error("Key File dialog threw an exception, see stack trace for details", e);
			throw new ToolException("Unexpected failure from Key file dialog, bailing out", e);
		} catch (UserCancelException e) {
			// Not a failure, just generate a new pair later
		}
	}

	private boolean promptForCSRFile() throws ToolException
	{
		DialogProvider provider = DialogFactory.getProvider(stdout, stderr, stdin, useGui());

		InputDialog csrFileDialog;
		try {
			csrFileDialog =
				provider.createInputDialog("CSR File", "Please enter CSR filename, or leave blank to generate one.");
			csrFileDialog.showDialog();
			_CSRFileName = csrFileDialog.getAnswer();
			return (_CSRFileName != null && !_CSRFileName.equals(""));

		} catch (DialogException e) {
			_logger.error("Unexpected failure in CSR file dialog", e);
			throw new ToolException("Unexpected failure in CSR file dialog", e);
		} catch (UserCancelException e) {
			// not a failure, we'll just generate one later
		}

		return false;
	}

	private KeyPair parseKeyFile(String keyFile) throws ToolException
	{
		KeyPair key = null;

		try {
			PEMParser parser = new PEMParser(new FileReader(keyFile));
			PEMKeyPair keyPair = (PEMKeyPair) parser.readObject();
			parser.close();

			key = new JcaPEMKeyConverter().setProvider("BC").getKeyPair(keyPair);
		} catch (IOException e) {
			throw new ToolException("Failed to parse specified key file", e);
		}

		return key;
	}
}
