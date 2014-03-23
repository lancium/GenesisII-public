package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.context.ContextType;

/*
 * Command to automate logging in to XSEDE infrastructure this is the equivalent of the following
 * multistep login: myproxyLogin passwordLogin IDPLogin logout (username-password token)
 */
public class XSEDELoginTool extends BaseLoginTool
{
	static private Log _logger = LogFactory.getLog(XSEDELoginTool.class);

	static private final String _DESCRIPTION = "config/tooldocs/description/dlogin";
	static private final String _USAGE_RESOURCE = "config/tooldocs/usage/ulogin";
	static final private String _MANPAGE = "config/tooldocs/man/login";

	protected XSEDELoginTool(String description, String usage, boolean isHidden)
	{
		super(description, usage, isHidden);
		overrideCategory(ToolCategory.SECURITY);
	}

	public XSEDELoginTool()
	{
		super(_DESCRIPTION, _USAGE_RESOURCE, false);
		overrideCategory(ToolCategory.SECURITY);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected void verify() throws ToolException
	{
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

		// Make sure we have username/password set if they were not passed in
		aquireUsername();
		aquirePassword();

		// Do a myproxy login (will replace tool identity)
		MyProxyLoginTool mpTool = new MyProxyLoginTool();
		BaseLoginTool.copyCreds(this, mpTool);
		int retVal = mpTool.run(stdout, stderr, stdin);
		if (retVal != 0) {
			String msg = "failure during login script: return value=" + retVal;
			_logger.error(msg);
			return retVal;
		}

		// Do a normal login
		LoginTool lTool = new LoginTool();
		// trying to use the myproxy creds! which is what we need!
		BaseLoginTool.copyCreds(mpTool, lTool);
		retVal = lTool.run(stdout, stderr, stdin);
		if (retVal != 0) {
			String msg = "failure during login script: return value=" + retVal;
			_logger.error(msg);
			return retVal;
		}

		return 0;
	}

}
