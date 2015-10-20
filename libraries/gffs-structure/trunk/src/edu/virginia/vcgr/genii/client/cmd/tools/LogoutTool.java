package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.PreferredIdentity;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.identity.Identity;

public class LogoutTool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(LogoutTool.class);

	static final private String _DESCRIPTION = "config/tooldocs/description/dlogout";
	static final private String _USAGE = "config/tooldocs/usage/ulogout";
	static final private String _MANPAGE = "config/tooldocs/man/logout";

	protected String _pattern = null;
	protected boolean _all = false;

	public LogoutTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.SECURITY);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Option({ "pattern" })
	public void setPattern(String pattern)
	{
		_pattern = pattern;
	}

	@Option({ "all" })
	public void setAll()
	{
		_all = true;
	}

	/**
	 * checks whether the credential "cred" is equivalent to the preferred identity or not. if it is, then we toss out the current preferred
	 * identity unless it's fixated.
	 */
	public void removePreferredIdentityIfAppropriate(NuCredential cred)
	{
		// remove the preferred identity if this guy matches (and pref id is not fixated).
		if (!PreferredIdentity.fixatedInCurrent()) {
			PreferredIdentity current = PreferredIdentity.getCurrent();

			if (current.matchesIdentity(cred.getOriginalAsserter()[0])) {
				/*
				 * ouch, we have a match to the preferred identity. they just logged out of it, so we need to drop it. if they don't have it
				 * in their credentials, it's worthless to everyone.
				 */
				PreferredIdentity.dropCurrent();
				_logger.debug("dropping preferred identity found same as logged out credential: " + current.getIdentityString());
			}
		}
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
	{
		ICallingContext callContext = ContextManager.getCurrentContext();

		if ((callContext == null) && _all) {
			// nothing to log out.
			return 0;
		}
		if (callContext == null) {
			throw new IOException("There was no calling context to log out of.");
		}
		if (_all) {
			// toss out all credentials, including TLS cert.
			ClientUtils.invalidateCredentials(callContext);
			ContextManager.storeCurrentContext(callContext);
		} else if (_pattern != null) {
			int flags = 0;
			Pattern p = Pattern.compile("^.*" + Pattern.quote(_pattern) + ".*$", flags);

			int numMatched = 0;
			ArrayList<NuCredential> credentials = TransientCredentials.getTransientCredentials(callContext).getCredentials();
			Iterator<NuCredential> itr = credentials.iterator();
			while (itr.hasNext()) {
				NuCredential cred = itr.next();
				String toMatch = null;
				if (cred instanceof Identity) {
					toMatch = cred.toString();
				} else if (cred instanceof TrustCredential) {
					toMatch = ((TrustCredential) cred).getRootIdentity().toString();
				}

				Matcher matcher = p.matcher(toMatch);
				if (matcher.matches()) {
					removePreferredIdentityIfAppropriate(cred);

					itr.remove();
					numMatched++;
					if (_logger.isDebugEnabled()) {
						_logger.debug("Removed credential from current calling context credentials.");
					}
				}
			}

			if (numMatched == 0) {
				throw new IOException("No credentials matched the pattern \"" + _pattern + "\".");
			}
			ContextManager.storeCurrentContext(callContext);
		} else {
			while (true) {
				ArrayList<NuCredential> credentials = TransientCredentials.getTransientCredentials(callContext).getCredentials();
				if (credentials.size() == 0)
					break;
				stdout.println("Please select a credential to logout from:");
				for (int lcv = 0; lcv < credentials.size(); lcv++) {
					stdout.println("\t[" + lcv + "]:  " + credentials.get(lcv));
				}

				stdout.println("\t[x]:  Cancel");
				stdout.print("\nSelection?  ");
				try {
					String answer = stdin.readLine();
					if (answer == null)
						continue;
					if (answer.equalsIgnoreCase("x"))
						break;
					int which = Integer.parseInt(answer);
					if (which >= credentials.size()) {
						stderr.println("Selection index must be between 0 and " + (credentials.size() - 1));
					}

					NuCredential cred = credentials.get(which);
					removePreferredIdentityIfAppropriate(cred);

					credentials.remove(which);
					if (_logger.isDebugEnabled())
						_logger.debug("Removing credential from current calling context credentials.");

					ContextManager.storeCurrentContext(callContext);
				} catch (Throwable t) {
					stderr.println("Error getting login selection:  " + t.getLocalizedMessage());
					break;
				}
			}

			// drop any notification brokers or other cached info after credential change.
			CacheManager.resetCachingSystem();
		}

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 0)
			throw new InvalidToolUsageException();
	}
}