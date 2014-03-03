package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.context.ContextType;

/*
 * Command to automate logging in to XSEDE infrastructure this is the equivalent of the following
 * multistep login: myproxyLogin passwordLogin IDPLogin logout (username-password token)
 */
public class XSEDELoginTool extends BaseLoginTool {

	static private final String _DESCRIPTION = "config/tooldocs/description/dlogin";
	static private final String _USAGE_RESOURCE = "config/tooldocs/usage/ulogin";
	static final private String _MANPAGE = "config/tooldocs/man/login";

	protected XSEDELoginTool(String description, String usage, boolean isHidden) {
		super(description, usage, isHidden);
		overrideCategory(ToolCategory.SECURITY);
	}

	public XSEDELoginTool() {
		super(_DESCRIPTION, _USAGE_RESOURCE, false);
		overrideCategory(ToolCategory.SECURITY);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected void verify() throws ToolException {
	}

	@Override
	protected int runCommand() throws Throwable {

		// get the local identity's key material (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext();
		if (callContext == null) {
			callContext = new CallingContextImpl(new ContextType());
		}

		// Make sure we have username/password set if they were not passed in
		aquireUsername();
		aquirePassword();

		// Do a myproxy login (will replace tool identity)
		MyProxyLoginTool mpTool = new MyProxyLoginTool();
		BaseLoginTool.copyCreds(this, mpTool);
		mpTool.run(stdout, stderr, stdin);

		// Do a normal login
		LoginTool lTool = new LoginTool();
		// trying to use the myproxy creds! which is what we need!
		BaseLoginTool.copyCreds(mpTool, lTool);
		lTool.run(stdout, stderr, stdin);

		return 0;
	}

}
