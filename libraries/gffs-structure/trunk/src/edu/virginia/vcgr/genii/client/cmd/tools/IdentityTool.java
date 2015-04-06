package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.List;

import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.PreferredIdentity;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;

public class IdentityTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/didentity";
	static final private String _USAGE = "config/tooldocs/usage/uidentity";
	static final private String _MANPAGE = "config/tooldocs/man/identity";

	// true if they want to list their preferred identity.
	boolean _showIdentity = false;
	// true if the preferred id should be used regardless of whether currently in credentials.
	boolean _fixated = false;
	// true if we are setting the preferred id.
	boolean _settingId = false;
	// the exactly matching credential's DN to look for in credential wallet.
	String _exactString = null;
	// a pattern to try to find in DNs in the credential wallet.
	String _patternString = null;
	// if true, then we are resetting the preferred identity.
	boolean _resetting = false;
	// if true, then show all the identities that are available in the wallet.
	boolean _listAllIdentities = false;
	// if true, then the current setting should be resolved to test it out.
	boolean _resolveIdentity = false;

	public IdentityTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Option({ "show" })
	public void setShowIdentity()
	{
		_showIdentity = true;
	}

	@Option({ "listAll" })
	public void setListAll()
	{
		_listAllIdentities = true;

	}

	@Option({ "fixate" })
	public void setFixate()
	{
		_fixated = true;
	}

	@Option({ "reset" })
	public void setReset()
	{
		_resetting = true;
	}

	@Option({ "set" })
	public void setSet()
	{
		_settingId = true;
	}

	@Option({ "exact" })
	public void setExactMatch(String exo)
	{
		_exactString = exo;
	}

	@Option({ "pattern" })
	public void setPatternMatch(String pat)
	{
		_patternString = pat;
	}

	@Option({ "resolve" })
	public void setResolve()
	{
		_resolveIdentity = true;
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
	{
		ICallingContext context = ContextManager.getCurrentContext();

		/*
		 * the scheme here is actually logical, despite it seeming like a big bag of code. we will handle any of the three major command line
		 * flags, even if all are provided. but we interpret this in a particular order that makes sense to us; we do a reset first, if it was
		 * requested. then we do a setting operation if it was requested. then we do the listing operation. by doing things in that order,
		 * even someone who passes in all the flags will get something reasonable out of the tool, as long as their pattern/exact string
		 * parameters make sense (in conjunction with the fixate flag).
		 */

		// if reset, then reset.
		if (_resetting) {
			PreferredIdentity.removeFromContext(context);
			stdout.println("The preferred identity has been reset.");
		}

		// if set, then set...
		if (_settingId) {
			// get the ball of credentials we'll be looking at.
			List<NuCredential> clump = PreferredIdentity.gatherCredentials(context);

			// if exact string is set, just use that.
			String identityToUse = _exactString;
			if (identityToUse == null) {
				// if pattern is not set, complain and bomb out.
				if (_patternString == null) {
					throw new ToolException("This tool requires either an exact string or a pattern parameter.");
				}
				// if pattern match fails, bomb out. if pattern match succeeds, set the exact
				// string.
				identityToUse = PreferredIdentity.resolveIdentityPatternInCredentials(_patternString, clump);
				if (identityToUse == null) {
					throw new ToolException("The pattern provided did not match any user or connection credentials.");
				}
			}
			// the identityToUse is proven to not be null by here.
			PreferredIdentity toStore = new PreferredIdentity(identityToUse, _fixated);
			if (_fixated) {
				// if fixate is true, just go with it.
				PreferredIdentity.setInContext(context, toStore);
			} else {
				/*
				 * if fixate is false, check resulting exact string is listed in credentials. we know this might be checking the string twice,
				 * since the pattern could have been used to find the string, but we are not worried about a simple string compare done in
				 * human scale of time. we definitely care if a non-fixated identity was not actually in the credentials though.
				 */
				identityToUse = PreferredIdentity.resolveIdentityPatternInCredentials(identityToUse, clump);
				if (identityToUse == null) {
					throw new ToolException("The exact string provided did not match a user or connection credential.");
				}
				// if decided good, save the new preferred identity in context.
				PreferredIdentity.setInContext(context, toStore);
			}
			// let them know that things went well, and also show the new id.
			stdout.println("The preferred identity has been set.");
			_showIdentity = true;
		}

		// if showing id, then show current.
		if (_showIdentity) {
			PreferredIdentity curr = PreferredIdentity.getFromContext(context);
			if (curr == null) {
				stdout.println("show: There is currently no preferred identity for this user.");
			} else {
				stdout.println("The current preferred identity is: " + curr.toString());
				if (curr.getFixateIdentity()) {
					stdout.println("** This identity is fixated by user request. **");
				}
			}
		}

		if (_resolveIdentity) {
			PreferredIdentity curr = PreferredIdentity.getFromContext(context);
			if (curr == null) {
				stdout.println("resolve: There is currently no preferred identity for this user.");
			} else {
				List<NuCredential> clump = PreferredIdentity.gatherCredentials(context);
				String resolved = PreferredIdentity.resolveIdentityPatternInCredentials(curr.getIdentityString(), clump);
				if (resolved.equals(curr.getIdentityString())) {
					stdout.println("The preferred identity resolves properly to an existing credential.");
				} else {
					stdout.println("The preferred identity cannot currently be resolved to an existing credential.");
				}
			}

		}

		// if listing all, show anything the user has currently.
		if (_listAllIdentities) {
			// get the full set of credentials the user possesses.
			List<NuCredential> clump = PreferredIdentity.gatherCredentials(context);
			if (clump.isEmpty()) {
				stdout.println("There are currently no credentials available to list; not logged in.");
			} else {
				stdout.println("User and Connection Identities in Wallet:");
			}
			stdout.println(PreferredIdentity.printUserAndConnCredentials(clump));
		}

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if ((_exactString != null) && (_patternString != null)) {
			throw new ToolException("This tool cannot accept both an exact string and a pattern.");
		}
		if (!_resetting && !_settingId && !_showIdentity && !_listAllIdentities && !_resolveIdentity) {
			// we don't want to goof if they provided _some_ info but forgot the main tag...
			if ((_patternString != null) || (_exactString != null)) {
				throw new ToolException("This tool cannot accept DN strings without a --set operation.");
			}
			// if they didn't pick an action, then we just show the current identity.
			_showIdentity = true;
		}
	}
}
