package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.security.credentials.*;
import edu.virginia.vcgr.genii.security.credentials.assertions.*;
import edu.virginia.vcgr.genii.security.credentials.identity.*;

public class LogoutTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dlogout";
	static final private String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/ulogout";
	static final private String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/logout";
	
	protected String _pattern = null;
	protected boolean _all = false;

	public LogoutTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false,
				ToolCategory.SECURITY);
		addManPage(new FileResource(_MANPAGE));
	}

	@Option({"pattern"})
	public void setPattern(String pattern) {
		_pattern = pattern;
	}
	
	@Option({"all"})
	public void setAll() {
		_all = true;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		ICallingContext callContext = ContextManager.getCurrentContext(false);

		if ((callContext != null) && (_all)) {
			TransientCredentials.globalLogout(callContext);
			callContext.setActiveKeyAndCertMaterial(null);
			ContextManager.storeCurrentContext(callContext);
		} else if (_pattern != null) {
			int flags = 0;
			Pattern p = Pattern.compile("^.*" + Pattern.quote(_pattern)
					+ ".*$", flags);

			int numMatched = 0;
			ArrayList <GIICredential> credentials = 
				TransientCredentials.getTransientCredentials(callContext)._credentials;
			Iterator<GIICredential> itr = credentials.iterator();
			while (itr.hasNext()) {
				GIICredential cred = itr.next();
				String toMatch = null;
				if (cred instanceof Identity) {
					toMatch = cred.toString();
				} else if (cred instanceof SignedAssertion) {
					toMatch = ((SignedAssertion) cred).getAttribute().toString();
				}

				Matcher matcher = p.matcher(toMatch);
				if (matcher.matches()) {
					itr.remove();
					numMatched++;
					TransientCredentials._logger.debug("Removing credential from current calling context credentials.");
				}
			}

			if (numMatched == 0) {
				throw new IOException("No credentials matched the pattern \""
						+ _pattern + "\".");
			} 
			ContextManager.storeCurrentContext(callContext);
		} else {
			while (true) {
				ArrayList <GIICredential> credentials = 
					TransientCredentials.getTransientCredentials(callContext)._credentials;
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
						stderr.println("Selection index must be between 0 and " +
							(credentials.size() - 1));
					}
					credentials.remove(which);
					TransientCredentials._logger.debug("Removing credential from current calling context credentials.");
					ContextManager.storeCurrentContext(callContext);
				} catch (Throwable t)	{
					stderr.println("Error getting login selection:  " + 
						t.getLocalizedMessage());
					break;
				}
			}
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