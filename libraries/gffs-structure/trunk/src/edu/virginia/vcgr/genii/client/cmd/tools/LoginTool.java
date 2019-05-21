package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.NamespaceDefinitions;
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
import edu.virginia.vcgr.genii.client.security.PreferredIdentity;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.utils.PathUtils;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
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

	/**
	 * login will checks this list of idp paths, in order, if one is not passed on the command line
	 */
	protected static Collection<String> getDefaultIDPPaths(String username)
	{
		ArrayList<String> idpList = new ArrayList<String>();

		NamespaceDefinitions nsd = Installation.getDeployment(new DeploymentName()).namespace();

		String constructedPath = constructPathFromLoginName(null, username);
		if (constructedPath != null) {
			// add the default path in front of the weird email hierarchy name we got back.
			idpList.add(nsd.getUsersDirectory() + "/" + constructedPath);
		}

		// if we're in xsede.org namespace, we need to add the globus auth path in as first try.
		if (nsd.getUsersDirectory().equals(GenesisIIConstants.DEFAULT_XSEDE_USERS_PATH)) {
			idpList.add(0, GenesisIIConstants.DEFAULT_GLOBUSAUTH_USERS_PATH + "/" + username);
			if (_logger.isTraceEnabled())
				_logger.debug("found xsede user path is default, so trying globus first, with this path: " + idpList.get(0));
		}

		// add in the attempt to login based on the default users storage path.
		idpList.add(nsd.getUsersDirectory() + "/" + username);

		// Let's first load the LoginSearchPath properties file
		if (p == null) {
			p = new Properties();
			InputStream in = HelpLinkConfiguration.class.getClassLoader().getResourceAsStream("config/LoginSearchPath.properties");
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
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException, CreationException, InvalidToolUsageException, ClassNotFoundException, DialogException
	{

		// get the local identity's key material (or create one if necessary)
		ICallingContext realCallingContext = ContextManager.getCurrentOrMakeNewContext();
		/*
		 * May 9, 2019 by ASG. Code added to create support for multiple identity sessions, first step, create a nonce to refer to each session.
		 */
		

		// Added 2019-05-17 by ASG
		if (_password==null && _username==null){
			aquireUsername();
		}

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

			if ((realCallingContext.getCurrentPath() == null)
				|| !realCallingContext.getCurrentPath().lookup(authnSource.getSchemeSpecificPart()).exists())
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
				if ((realCallingContext.getCurrentPath() != null)
					&& realCallingContext.getCurrentPath().lookup(authnSource.getSchemeSpecificPart()).exists()) {
					_authnUri = authURI;
					break;
				}
			}

			if (_authnUri == null)
				throw new ToolException(
					"Could not authenticate to login service, ensure your username is correct or manually specify IDP path");
		}

		// we will fill out this list if the login is successful.
		ArrayList<NuCredential> creds = null;

		// temporary context to hold username password junk.
		Closeable assumedContextToken = null;
		// identity filled in during login.
		PreferredIdentity prefId = null;

		try {
			// we will make a clone of our context now to avoid having this credential stick around.
			ICallingContext newContext = realCallingContext.deriveNewContext();

			assumedContextToken = ContextManager.temporarilyAssumeContext(newContext);

			// Do password Login
			UsernamePasswordIdentity utCredential = new PasswordLoginTool().doPasswordLogin(_username, _password);
			if (utCredential != null) {

				TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(newContext);
				transientCredentials.add(utCredential);

				/*
				 * we're going to use the WS-TRUST token-issue operation to log in to a security tokens service
				 */
				URI authnSource;
				try {
					authnSource = PathUtils.pathToURI(_authnUri);
				} catch (URISyntaxException e) {
					throw new ToolException("failure to convert path: " + e.getLocalizedMessage(), e);
				}
				KeyAndCertMaterial clientKeyMaterial =
					ClientUtils.checkAndRenewCredentials(newContext, BaseGridTool.credsValidUntil(), new SecurityUpdateResults());

				if (newContext.getCurrentPath() == null)
					throw new ToolException("Failure to getCurrentPath in context");

				RNSPath authnPath = newContext.getCurrentPath().lookup(authnSource.getSchemeSpecificPart(), RNSPathQueryFlags.MUST_EXIST);
				EndpointReferenceType epr = authnPath.getEndpoint();

				try {

					// Do IDP login
					creds = IDPLoginTool.doIdpLogin(epr, _credentialValidMillis, clientKeyMaterial._clientCertChain);

					// drop any notification brokers or other cached info after credential change.
					CacheManager.resetCachingSystem();

					if (creds == null) {
						return 0;
					} else {
						for (NuCredential q : creds) {
							_logger.info("login cred: " + q);
						}
					}

					// grab the preferred id so we can put it in the real context.
					prefId = PreferredIdentity.getCurrent();
				} finally {
					if (utCredential != null) {
						// the UT credential was used only to log into the IDP, remove it
						transientCredentials.remove(utCredential);
						_logger.debug("Removing temporary username-token credential from current calling context credentials.");
					}
				}
			}
		} finally {
			StreamUtils.close(assumedContextToken);
		}

		// re-acquire our context.
		realCallingContext = ContextManager.getCurrentContext();
		PreferredIdentity.setInContext(realCallingContext, prefId);
		TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(realCallingContext);

		// now add the credentials that we picked up from the login.
		if (creds != null) {
			transientCredentials.addAll(creds);
		}

		ContextManager.storeCurrentContext(realCallingContext);

		// reset caching system again prior to changing directory to make sure nothing old is left.
		CacheManager.resetCachingSystem();

		{
			// special code to not use /home/globus-auth as a home path, since we don't have those.
			String pathToChop = _authnUri;
			if (pathToChop.startsWith(GenesisIIConstants.DEFAULT_GLOBUSAUTH_USERS_PATH + "/")) {
				pathToChop = GenesisIIConstants.DEFAULT_XSEDE_USERS_PATH + "/"
					+ _authnUri.substring(GenesisIIConstants.DEFAULT_GLOBUSAUTH_USERS_PATH.length() + 1);
				if (_logger.isTraceEnabled())
					_logger.debug("amended path for auth after saw globus in there to move back to /users/xsede.org: " + pathToChop);
			}

			// jumps to the user's home directory.
			// Assumption is that user idp's are off /user and homes off /home
			String userHome = pathToChop.replaceFirst("users", "home");
			try {
				CdTool.chdir(userHome);
				SetTool.set_var("HOME", userHome);
			} catch (Throwable e) {
			}
		}

		// reload context since CdTool stored to disk and didn't update our context here.
		realCallingContext = ContextManager.getCurrentContext();
		
		/*
		 * May 9, 2019 by ASG. Code added to create support for multiple identity sessions, first step, create a nonce to refer to each sesstion.
		 */
		if (_create_nonce) {
			String nonce = _username + "-" + (new GUID()).toString();
			// Now put it in the LRU Cache
			if (realCallingContext != null) {
				ContextManager.stash(nonce, realCallingContext);
				stdout.println("#nonce=" + nonce);
			}
			else {
				_logger.error("There was no context to be stored for nonce of: " + nonce);
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

		if (_bogusPassword) {
			if (_password != null) {
				throw new ToolException("Cannot provide a password with noPassword option.");
			}

			/*
			 * don't always use the same string for the bogus password. it's already bogus and should just never be right, but using a simple
			 * constant string seemed wrong.
			 */
			_password = "bogus" + ((new Random()).nextInt(1000000000) + 1) + "-" + ((new Random()).nextInt(1000000000) + 1);
		}

		if (_durationString != null) {
			try {
				_credentialValidMillis = (long) new Duration(_durationString).as(DurationUnits.Milliseconds);
			} catch (IllegalArgumentException pe) {
				throw new ToolException("Invalid duration string given.", pe);
			}
		}

	}

	/**
	 * a special purpose function that builds a reverse ordered path from an email address hierarchy, if we see that in the user name,
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
