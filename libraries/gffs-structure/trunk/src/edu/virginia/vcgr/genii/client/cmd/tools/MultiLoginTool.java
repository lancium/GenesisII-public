package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.algorithm.math.SimpleRandomizer;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
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

public class MultiLoginTool extends LoginTool
{
	static private Log _logger = LogFactory.getLog(MultiLoginTool.class);

//	private static final String USER_NAME_TERMINATOR = "@";
//	private static final String DOMAIN_NAME_SEPARATOR = "\\.";
//	private static final String PATH_COMPONENT_SEPARATOR = "/";

	static private final String _DESCRIPTION = "config/tooldocs/description/dlogin";
	static private final String _USAGE_RESOURCE = "config/tooldocs/usage/ulogin";
//	static final private String _MANPAGE = "config/tooldocs/man/login";

//	private static Properties p = null;
	static final String SEARCH_PATH = "SEARCH_PATH";

	// ugly hard-coded strings for this currently simple tester.
	ArrayList<StringPair> usersToLogin = new ArrayList<StringPair>(Arrays.asList(
			new StringPair("fred", "FOOP"),
			new StringPair("xsedetest-user1", "eep1"),
			new StringPair("xsedetest-user2", "eep2"),
			new StringPair("xsedetest-user3", "eep3") ));
	
	ArrayList<Runnable> _ourThreads = new ArrayList<Runnable>();
	
	protected MultiLoginTool(String description, String usage, boolean isHidden)
	{
		super(description, usage, isHidden);
//		overrideCategory(ToolCategory.SECURITY);
	}

	public MultiLoginTool()
	{
		super(_DESCRIPTION, _USAGE_RESOURCE, true);
//		overrideCategory(ToolCategory.SECURITY);
//		addManPage(new LoadFileResource(_MANPAGE));
	}

	
	class StringPair extends Pair<String, String>
	{
		private static final long serialVersionUID = 1L;

		StringPair(String a, String b) {
			super(a, b);
		}
	}
	
	public class SimpleWorker implements Runnable 
	{
		Thread _worker = null;
		Writer _out = null;
		Writer _err = null;
		ICallingContext _ctxt = null;
		
		SimpleWorker(byte[] contextFlat, PrintWriter out, PrintWriter err) {
			_logger.debug("simpleworker: construction");

			_out = out;
			_err = err;
			
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(contextFlat);
				ObjectInputStream ois = new ObjectInputStream(bais);
				ICallingContext ctxt = (ICallingContext) ois.readObject();
				
				_ctxt = ctxt;  // hang onto for sanity check.
				
//				KeyAndCertMaterial kacm = ctxt.getActiveKeyAndCertMaterial();				
//				out.println("before thread start, active key info is:\n" + kacm.toString());
				
				_worker = new Thread(this);		
				_logger.debug("simpleworker: thread launch");
				_worker.start();
			} catch (Exception e) {
				_logger.error("exception during resolver setup", e);
			}
			
		}

		@Override
		public void run() {

			_logger.debug("simpleworker: establish context");
			try {
				MemoryBasedContextResolver ourResolver = new MemoryBasedContextResolver(_ctxt);			
				ContextManager.setResolver(ourResolver);
			} catch (Exception e) {
				_logger.error("failed to setup thread's context", e);
				return;
			}
			
			while (true) {
				
				// sleep from 1 to 10 seconds before acting.
				try {
					Thread.sleep(100 * SimpleRandomizer.randomInteger(10, 100));
				} catch (InterruptedException e) {
					_logger.info("simpleworker: sleep got interrupted, leaving loop.");
					break;
				}
				
				_logger.debug("simpleworker: run loop entry");				

				try {
					StringWriter baseWriter = new StringWriter();
					PrintWriter buff = new PrintWriter(baseWriter);
					
					buff.println(_worker.getId() + " vvvvvvvvvvvvvvvvvvvvvvv");
					
					ICallingContext ctxt = ContextManager.getCurrentContext();
					
//					_logger.debug("here is current context dump:");
//					_logger.debug(ctxt.dumpContext());
										
					
//					KeyAndCertMaterial kacm = ctxt.getActiveKeyAndCertMaterial();				
//					stdout.println("inside thread, active key info is:\n" + (kacm == null? "NULL!  aieeee": kacm.toString()));					

//					// testing this; we are not seeing any issues with the call context.
//					KeyAndCertMaterial kacm2 = ClientUtils.checkAndRenewCredentials(ctxt, BaseGridTool.credsValidUntil(), new SecurityUpdateResults());
//					stdout.println("inside thread, after verify, active key info is:\n" + (kacm2 == null? "NULL!  aieeee": kacm2.toString()));
					
					// code seen in the axis based context resolver when current context is stored.  trying here.
					//nope, can't get to resource manager. ResourceManager.getCurrentResource().dereference().setProperty(IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME, ctxt);
					//instead, trying hail mary pass which we suspect won't help...
					//indeed doesn't help -- ContextManager.storeCurrentContext(ctxt);				
					
					// do some simple actions...
					WhoamiTool who = new WhoamiTool();
					who.displayCredentials(ctxt, false, buff, buff);

					buff.println("current directory:");
					buff.println(RNSPath.getCurrent().pwd());					
					
					LsTool lister = new LsTool();
					lister.establishStandardIO(buff, buff, null);
					lister.runCommand();
					
					// drop our context again if needed.
					//haven't seen a need yet.
					
					buff.println(_worker.getId() + " ^^^^^^^^^^^^^^^^^");
					// gather all the intended output in a batch to show at the same time.
					System.out.print(baseWriter.toString());
					
					_logger.debug("simpleworker: run loop exit");
					
				} catch (Exception e) {
					_logger.error("failed to do main operation in SimpleWorker", e);
				}

//				 // snooze 10 seconds before next action.
//				try {
//					Thread.sleep(1000 * 10);
//				} catch (InterruptedException e) {
//					_logger.info("simpleworker: sleep got interrupted, leaving loop.");
//					break;
//				}

			}
		}
		
		
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException, CreationException, InvalidToolUsageException, ClassNotFoundException, DialogException
	{
		stdout.println("logging out any existing credentials before test");
		{ // new scope to hide variables.
			ICallingContext callContext = ContextManager.getCurrentContext();
			ClientUtils.invalidateCredentials(callContext);
			ContextManager.storeCurrentContext(callContext);
			stdout.println("our current identity before logins is now:");
			// grab context again.
			callContext = ContextManager.getCurrentContext();
			WhoamiTool who = new WhoamiTool();
			who.displayCredentials(callContext, true, stdout, stderr);
		}
				
		for (StringPair userpass : usersToLogin) {
			stdout.println("logging in user: " + userpass.first());

			// get the local identity's key material (or create one if necessary)
			ICallingContext realCallingContext = ContextManager.getCurrentOrMakeNewContext();

			//aquireUsername();  // replace with our info.
			_username = userpass.first();
			_password = userpass.second();

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
				ICallingContext newContext = realCallingContext;

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

			stdout.println("presumably we now have a good login for: " + userpass.first());
			
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
			
			stdout.println("have changed directory for user: " + userpass.first());

			// now special sauce; save off this context into a thread local variable for a new thread we create
			// to get some work done.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(realCallingContext);
			oos.flush();
			SimpleWorker w = new SimpleWorker(baos.toByteArray(), stdout, stderr);
			_ourThreads.add(w);
			
			stdout.println("worker thread spun up for user: " + userpass.first());
		
			// logout now for this main thread.  the blank pattern will reap all credentials, but leave TLS.
			LogoutTool.logoutByPattern(realCallingContext, "");			
		}
		
		stdout.println("Now snoozing at main thread to keep workers alive.");
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				stdout.println("Bailing out of snoozing due to exception: " + e.getMessage());
				break;
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
//	public static String constructPathFromLoginName(String pathPrefix, String loginName)
//	{
//		if (loginName == null)
//			return null;
//
//		try {
//			if (loginName.contains(USER_NAME_TERMINATOR)) {
//
//				String[] parts = loginName.split(USER_NAME_TERMINATOR);
//				if (parts.length != 2)
//					return null;
//				String user = parts[0];
//				String domain = parts[1];
//				String[] domainParts = domain.split(DOMAIN_NAME_SEPARATOR);
//
//				StringBuilder buffer = new StringBuilder();
//				if (pathPrefix != null) {
//					buffer.append(pathPrefix);
//					if (!pathPrefix.endsWith(PATH_COMPONENT_SEPARATOR)) {
//						buffer.append(PATH_COMPONENT_SEPARATOR);
//					}
//				}
//				int domainLength = domainParts.length;
//				for (int i = domainLength - 1; i >= 0; i--) {
//					buffer.append(domainParts[i]).append(PATH_COMPONENT_SEPARATOR);
//				}
//				buffer.append(user);
//
//				return buffer.toString();
//			} else
//				return null;
//
//		} catch (Throwable e) {
//			return null;
//		}
//	}
}
