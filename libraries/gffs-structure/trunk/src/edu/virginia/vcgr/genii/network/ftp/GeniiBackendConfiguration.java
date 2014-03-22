package edu.virginia.vcgr.genii.network.ftp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.LoginTool;
import edu.virginia.vcgr.genii.client.cmd.tools.LogoutTool;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.context.InMemorySerializedContextResolver;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class GeniiBackendConfiguration implements Cloneable
{
	static private Log _logger = LogFactory.getLog(GeniiBackendConfiguration.class);

	private ICallingContext _callingContext;
	private RNSPath _root;

	private GeniiBackendConfiguration(ICallingContext callingContext, RNSPath root) throws IOException
	{
		_callingContext = callingContext;
		_root = root;

		// This always gets called in a new thread, so...
		ContextManager.setResolver(new InMemorySerializedContextResolver());
		ContextManager.storeCurrentContext(_callingContext);
	}

	public GeniiBackendConfiguration(BufferedReader stdin, PrintWriter stdout, PrintWriter stderr,
		ICallingContext callingContext) throws RNSException, IOException, ReloadShellException, ToolException
	{
		IContextResolver oldResolver = ContextManager.getResolver();
		try {
			IContextResolver newResolver = new InMemorySerializedContextResolver();
			ContextManager.setResolver(newResolver);

			newResolver.store(callingContext);
			_callingContext = newResolver.load();

			_root = _callingContext.getCurrentPath().getRoot();
			_callingContext.setCurrentPath(_root);

			LogoutTool logout = new LogoutTool();
			logout.setAll();
			int retVal = logout.run(stdout, stderr, stdin);
			if (retVal != 0) {
				String msg = "failure calling logout tool: return value=" + retVal;
				_logger.error(msg);
				throw new AuthZSecurityException(msg);
			}

			// Assume normal user/pass -> idp login
			LoginTool login = new LoginTool();
			retVal = login.run(stdout, stderr, stdin);
			if (retVal != 0) {
				String msg = "failure calling login tool: return value=" + retVal;
				_logger.error(msg);
				throw new AuthZSecurityException(msg);
			}
			_callingContext = newResolver.load();
		} catch (FileNotFoundException e) {
			throw new IOException(e.getLocalizedMessage(), e);
		} catch (Throwable e) {
			// print nothing since BaseGridTool already did.
			throw new AuthZSecurityException(e.getLocalizedMessage(), e);
		} finally {
			ContextManager.setResolver(oldResolver);
		}
	}

	public GeniiBackendConfiguration(BufferedReader stdin, PrintWriter stdout, PrintWriter stderr) throws RNSException,
		FileNotFoundException, IOException, ReloadShellException, ToolException
	{
		this(stdin, stdout, stderr, ContextManager.getExistingContext());
	}

	public void setSandboxPath(String sandboxPath) throws RNSException
	{
		_root = _root.lookup(sandboxPath, RNSPathQueryFlags.MUST_EXIST);
		_callingContext.setCurrentPath(_root);
	}

	public RNSPath getRoot()
	{
		return _root;
	}

	public ICallingContext getCallingContext()
	{
		return _callingContext;
	}

	public Object clone()
	{
		try {
			return new GeniiBackendConfiguration(_callingContext.deriveNewContext(), _root);
		} catch (IOException ioe) {
			throw new RuntimeException("Unexpected internal exception.", ioe);
		}
	}
}
