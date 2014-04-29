package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.NamespaceDefinitions;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gui.HelpLinkConfiguration;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.utils.PathUtils;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

public class LoginTool extends BaseLoginTool
{
	static private Log _logger = LogFactory.getLog(LoginTool.class);

	private static final String USER_NAME_TERMINATOR = "@";
	private static final String DOMAIN_NAME_SEPARATOR = "\\.";
	private static final String PATH_COMPONENT_SEPARATOR = "/";

	static private final String _DESCRIPTION = "config/tooldocs/description/dlogin";
	static private final String _USAGE_RESOURCE = "config/tooldocs/usage/ulogin";
	static final private String _MANPAGE = "config/tooldocs/man/login";

	private static Properties p = null;
	static final String SEARCH_PATH = "SEARCH_PATH";

	protected LoginTool(String description, String usage, boolean isHidden)
	{
		super(description, usage, isHidden);
		overrideCategory(ToolCategory.SECURITY);
	}

	public LoginTool()
	{
		super(_DESCRIPTION, _USAGE_RESOURCE, false);
		overrideCategory(ToolCategory.SECURITY);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	private static Collection<String> getDefaultIDPPaths(String username)
	{
		LinkedList<String> idpList = new LinkedList<String>();

		NamespaceDefinitions nsd = Installation.getDeployment(new DeploymentName()).namespace();

		String constructedPath = constructPathFromLoginName(null, username);
		if (constructedPath != null) {
			idpList.add(nsd.getUsersDirectory() + "/" + constructedPath);
			// idpList.add(nsd.getUsersDirectory() + "/demo/" + constructedPath);
		}

		// Checks this lists of idp paths, in order,
		// if one is not passed on the command line
		idpList.add(nsd.getUsersDirectory() + "/" + username);
		// idpList.add(nsd.getUsersDirectory() + "/demo/" + username);

		// Let's first load the LoginSearchPath properties file
		if (p == null) {
			p = new Properties();
			InputStream in =
				HelpLinkConfiguration.class.getClassLoader().getResourceAsStream("config/LoginSearchPath.properties");
			try {
				p.load(in);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				StreamUtils.close(in);
			}
		}
		// Then get the list of paths to search
		String r = p.getProperty(SEARCH_PATH);
		if (r == null) {
			throw new RuntimeException("Could not find config/LoginSearchPath.properties " + SEARCH_PATH);
		}
		// ASG: 2014-02-11, Now grab the substrings and add them to the list
		StringTokenizer tokenCollector = new StringTokenizer(r, ":");
		while (tokenCollector.hasMoreTokens()) {
			String pathname = tokenCollector.nextToken();
			idpList.add(pathname + "/" + username);
		}

		return idpList;
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException, InvalidToolUsageException,
		ClassNotFoundException, DialogException
	{

		// get the local identity's key material (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext();
		if (callContext == null) {
			callContext = new CallingContextImpl(new ContextType());
		}

		aquireUsername();

		// Determine IDP path
		if (numArguments() == 1) {
			// If Specified
			_authnUri = getArgument(0);
			URI authnSource;
			try {
				authnSource = PathUtils.pathToURI(_authnUri);
			} catch (URISyntaxException e) {
				throw new ToolException("failure to convert path: " + e.getLocalizedMessage(), e);
			}

			if (!callContext.getCurrentPath().lookup(authnSource.getSchemeSpecificPart()).exists())
				throw new ToolException("Invalid IDP path specified: " + authnSource.getSchemeSpecificPart());

		} else {
			// Check default paths
			_authnUri = null;

			for (String authURI : getDefaultIDPPaths(_username)) {
				URI authnSource;
				try {
					authnSource = PathUtils.pathToURI(authURI);
				} catch (URISyntaxException e) {
					throw new ToolException("failure to convert path: " + e.getLocalizedMessage(), e);
				}
				if (callContext.getCurrentPath().lookup(authnSource.getSchemeSpecificPart()).exists()) {
					_authnUri = authURI;
					break;
				}
			}

			if (_authnUri == null)
				throw new ToolException(
					"Could not authenticate to login service, ensure your username is correct or manually specify IDP path");
		}

		// Do password Login
		UsernamePasswordIdentity utCredential = new PasswordLoginTool().doPasswordLogin(_username, _password);

		if (utCredential != null) {

			TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callContext);
			transientCredentials.add(utCredential);

			ContextManager.storeCurrentContext(callContext);

			// we're going to use the WS-TRUST token-issue operation
			// to log in to a security tokens service
			URI authnSource;
			try {
				authnSource = PathUtils.pathToURI(_authnUri);
			} catch (URISyntaxException e) {
				throw new ToolException("failure to convert path: " + e.getLocalizedMessage(), e);
			}
			KeyAndCertMaterial clientKeyMaterial =
				ClientUtils.checkAndRenewCredentials(callContext, BaseGridTool.credsValidUntil(), new SecurityUpdateResults());

			RNSPath authnPath =
				callContext.getCurrentPath().lookup(authnSource.getSchemeSpecificPart(), RNSPathQueryFlags.MUST_EXIST);
			EndpointReferenceType epr = authnPath.getEndpoint();

			try {

				// Do IDP login
				ArrayList<NuCredential> creds =
					IDPLoginTool.doIdpLogin(epr, _credentialValidMillis, clientKeyMaterial._clientCertChain);

				if (creds == null) {
					return 0;
				} else {
					for (NuCredential q : creds) {
						_logger.info("login cred: " + q);
					}
				}

				// insert the assertion into the calling context's transient creds
				transientCredentials.addAll(creds);

			} finally {

				if (utCredential != null) {
					// the UT credential was used only to log into the IDP, remove it
					transientCredentials.remove(utCredential);
					_logger.debug("Removing temporary username-token credential from current calling context credentials.");
				}
			}

		}

		ContextManager.storeCurrentContext(callContext);
		// jumpToUserHomeIfExists(_username);
		{
			// Assumption is that user idp's are off /user and homes off /home
			String userHome = _authnUri.replaceFirst("users", "home");
			try {
				CdTool.chdir(userHome);
			} catch (Throwable e) {
			}
		}

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if (numArgs > 1)
			throw new InvalidToolUsageException("This tool takes at most one argument.");
		
		if ((_username == null) || (_username.length() == 0))
			throw new InvalidToolUsageException("The username cannot be blank.");

		if (_durationString != null) {
			try {
				_credentialValidMillis = (long) new Duration(_durationString).as(DurationUnits.Milliseconds);
			} catch (IllegalArgumentException pe) {
				throw new ToolException("Invalid duration string given.", pe);
			}
		}

	}

	/*
	 * public static void jumpToUserHomeIfExists(String loginName) {
	 * 
	 * if (loginName == null) return;
	 * 
	 * List<String> candidateHomeDirs = new ArrayList<String>(); String constructedPathToHome =
	 * constructPathFromLoginName(null, loginName);
	 * 
	 * NamespaceDefinitions nsd = Installation.getDeployment(new DeploymentName()).namespace();
	 * 
	 * if (constructedPathToHome != null) { candidateHomeDirs.add(nsd.getHomesDirectory() + "/" +
	 * constructedPathToHome); candidateHomeDirs.add(nsd.getHomesDirectory() + "/demo/" +
	 * constructedPathToHome); } candidateHomeDirs.add(nsd.getHomesDirectory() + "/" + loginName);
	 * candidateHomeDirs.add(nsd.getHomesDirectory() + "/demo/" + loginName);
	 * candidateHomeDirs.add("rns:/");
	 * 
	 * for (String userHome : candidateHomeDirs) { try { CdTool.chdir(userHome); break; } catch
	 * (Throwable e) { } } }
	 */

	public static String constructPathFromLoginName(String pathPrefix, String loginName)
	{

		if (loginName == null)
			return null;

		try {
			if (loginName.contains(USER_NAME_TERMINATOR)) {

				String[] parts = loginName.split(USER_NAME_TERMINATOR);
				if (parts.length != 2)
					return null;
				String user = parts[0];
				String domain = parts[1];
				String[] domainParts = domain.split(DOMAIN_NAME_SEPARATOR);

				StringBuilder buffer = new StringBuilder();
				if (pathPrefix != null) {
					buffer.append(pathPrefix);
					if (!pathPrefix.endsWith(PATH_COMPONENT_SEPARATOR)) {
						buffer.append(PATH_COMPONENT_SEPARATOR);
					}
				}
				int domainLength = domainParts.length;
				for (int i = domainLength - 1; i >= 0; i--) {
					buffer.append(domainParts[i]).append(PATH_COMPONENT_SEPARATOR);
				}
				buffer.append(user);

				return buffer.toString();
			} else
				return null;

		} catch (Throwable e) {
			return null;
		}
	}
}
