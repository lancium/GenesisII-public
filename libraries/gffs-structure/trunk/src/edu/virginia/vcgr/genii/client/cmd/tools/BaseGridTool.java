package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cmd.ITool;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSMultiLookupResultException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rns.filters.FilterFactory;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.PermissionDeniedException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.security.SecurityConstants;

public abstract class BaseGridTool implements ITool
{
	static private Log _logger = LogFactory.getLog(BaseGridTool.class);

	protected OptionSetter _setter = null;

	private String _description;
	private String _usage;
	private String _manPage = null;

	private boolean _isHidden;
	private ToolCategory _category = ToolCategory.MISC;

	private boolean _useGui = true;
	protected List<String> _arguments = new ArrayList<String>();

	// tool credential validity; used when recreating a TLS certificate for the tool.
	protected static long _credentialValidMillis = SecurityConstants.defaultCredentialExpirationMillis;

	/*
	 * tracks the last command's exit value. this is managed per-program instance. zero implies
	 * success of last operation; any other value implies a failure.
	 */
	private static int _lastExit = 0;

	protected PrintWriter stdout;
	protected PrintWriter stderr;
	protected BufferedReader stdin;

	private BaseGridTool(String description, String usage, boolean isHidden)
	{
		_description = description;
		_usage = usage;
		_isHidden = isHidden;
	}

	protected BaseGridTool(LoadFileResource descriptionResource, LoadFileResource usageResource, boolean isHidden,
		ToolCategory cat)
	{
		this(readResource(descriptionResource), readResource(usageResource), isHidden);
		_category = cat;
	}

	protected BaseGridTool(LoadFileResource descriptionResource, LoadFileResource usageResource, boolean isHidden)
	{
		this(readResource(descriptionResource), readResource(usageResource), isHidden);
	}

	protected abstract void verify() throws ToolException;

	// hmmm: ugly, but true. the set returned here should be reduced.
	protected abstract int runCommand() throws ReloadShellException /* necessary */, ToolException /* necessary */,
		UserCancelException /* necessary */, RNSException /* necessary */, GenesisIISecurityException,
		IOException/* necessary */, ResourcePropertyException, CreationException, ClassNotFoundException/* necessary */,
		DialogException;

	protected boolean useGui()
	{
		return _useGui;
	}

	protected void overrideCategory(ToolCategory cat)
	{
		_category = cat;
	}

	static protected RNSPath lookup(RNSPath parent, GeniiPath path) throws InvalidToolUsageException
	{
		if (path.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException(String.format("%s is not a grid path!", path));

		return parent.lookup(path.path());
	}

	static protected RNSPath lookup(GeniiPath path) throws InvalidToolUsageException
	{
		return lookup(RNSPath.getCurrent(), path);
	}

	static protected RNSPath lookup(RNSPath parent, GeniiPath path, RNSPathQueryFlags queryFlags)
		throws InvalidToolUsageException, RNSPathDoesNotExistException, RNSPathAlreadyExistsException
	{
		if (path.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException(String.format("%s is not a grid path!", path));

		return parent.lookup(path.path(), queryFlags);
	}

	static protected RNSPath lookup(GeniiPath path, RNSPathQueryFlags queryFlags) throws InvalidToolUsageException,
		RNSPathDoesNotExistException, RNSPathAlreadyExistsException
	{
		return lookup(RNSPath.getCurrent(), path, queryFlags);
	}

	protected void addManPage(LoadFileResource manPage)
	{
		_manPage = readResource(manPage);
	}

	public int getLastExit()
	{
		return _lastExit;
	}

	public void setLastExit(int newExitValue)
	{
		_lastExit = newExitValue;
	}

	@Override
	public final int run(Writer out, Writer err, Reader in) throws ReloadShellException, ToolException
	{
		try {
			stdout = (out instanceof PrintWriter) ? (PrintWriter) out : new PrintWriter(out, true);
			stderr = (err instanceof PrintWriter) ? (PrintWriter) err : new PrintWriter(err, true);
			stdin = (in instanceof BufferedReader) ? (BufferedReader) in : new BufferedReader(in);
			// check the parameters first.
			verify();
			// set the last exit value based on actual return value.
			_lastExit = runCommand();
		} catch (ClassNotFoundException e) {
			String msg = "application error, missing class: " + e.getLocalizedMessage();
			_logger.error(msg, e);
			// stderr.print(msg);
			// stderr.println();
			_lastExit = 1; // set last exit to a failure value.
			throw new ToolException(e.getLocalizedMessage(), e);
		} catch (DialogException e) {
			String msg = "dialog reported problem: " + e.getLocalizedMessage();
			_logger.error(msg, e);
			// stderr.print(msg);
			// stderr.println();
			_lastExit = 1; // set last exit to a failure value.
			throw new ToolException(e.getLocalizedMessage(), e);
		} catch (InvalidToolUsageException e) {
			String msg = "invalid tool usage: " + e.getLocalizedMessage();
			_logger.warn(msg);
			stderr.print(msg);
			stderr.print(usage());
			stderr.println();
			_lastExit = 1; // set last exit to a failure value.
			// this gets handled here, without forwarding the exception.
		} catch (CreationException e) {
			String msg = "a creation exception occurred during the operation: " + e.getLocalizedMessage();
			_logger.error(msg, e);
			// stderr.print(msg);
			// stderr.println();
			_lastExit = 1; // set last exit to a failure value.
			throw new ToolException(e.getLocalizedMessage(), e);
		} catch (ResourcePropertyException e) {
			String msg = "a resource property exception occurred: " + e.getLocalizedMessage();
			_logger.error(msg, e);
			// stderr.print(msg);
			// stderr.println();
			_lastExit = 1; // set last exit to a failure value.
			throw new ToolException(e.getLocalizedMessage(), e);
		} catch (UserCancelException uce) {
			// user cancellation is considered a success.
			_logger.info("user cancelled operation: " + uce.getLocalizedMessage());
			_lastExit = 0;
		} catch (RNSPathAlreadyExistsException e) {
			String msg = "path already exists: " + e.getLocalizedMessage();
			_logger.info(msg);
			// stderr.print(msg);
			// stderr.println();
			_lastExit = 1; // set last exit to a failure value.
			throw new ToolException(e.getLocalizedMessage(), e);
		} catch (RNSPathDoesNotExistException e) {
			String msg = "path does not exist: " + e.getLocalizedMessage();
			_logger.info(msg);
			// stderr.print(msg);
			// stderr.println();
			_lastExit = 1; // set last exit to a failure value.
			throw new ToolException(e.getLocalizedMessage(), e);
		} catch (PermissionDeniedException e) {
			String operation = PermissionDeniedException.extractMethodName(e.getMessage());
			String failedAsset = PermissionDeniedException.extractAssetDenied(e.getMessage());
			String msg;
			if ((operation != null) && (failedAsset != null))
				msg =
					e.getLocalizedMessage() + "; permission denied on \"" + failedAsset + "\" (in method \"" + operation
						+ "\")";
			else
				msg = "an odd permission denied exception occurred: " + e.getLocalizedMessage();
			_logger.info(msg);
			// stderr.print(msg);
			// stderr.println();
			_lastExit = 1; // set last exit to a failure value.
			throw new ToolException(e.getLocalizedMessage(), e);
		} catch (AuthZSecurityException e) {
			String msg = "an authorization error occurred: " + e.getLocalizedMessage();
			_logger.error(msg);
			// stderr.print(msg);
			// stderr.println();
			_lastExit = 1; // set last exit to a failure value.
			throw new ToolException(e.getLocalizedMessage(), e);
		} catch (RNSException e) {
			String msg = "an RNS exception occurred: " + e.getLocalizedMessage();
			_logger.error(msg);
			// stderr.print(msg);
			// stderr.println();
			_lastExit = 1; // set last exit to a failure value.
			throw new ToolException(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			String msg = "an I/O exception occurred: " + e.getLocalizedMessage();
			_logger.error(msg);
			// stderr.print(msg);
			// stderr.print(usage());
			// stderr.println();
			_lastExit = 1; // set last exit to a failure value.
			throw new ToolException(e.getLocalizedMessage(), e);
		} catch (ReloadShellException e) {
			// expected issue from certain commands, and we do need to forward this out.
			throw e;
		} finally {
			stdout = null;
			stderr = null;
			stdin = null;
		}
		return _lastExit;
	}

	protected List<String> getArguments()
	{
		return _arguments;
	}

	protected int numArguments()
	{
		return _arguments.size();
	}

	protected String getArgument(int argNumber)
	{
		if (argNumber >= _arguments.size())
			return null;

		return _arguments.get(argNumber);
	}

	@Option({ "no-gui" })
	public void setNo_gui()
	{
		_useGui = false;
	}

	/**
	 * returns the duration in milliseconds of a newly created self-signed TLS credential lifetime.
	 */
	static public long getValidMillis()
	{
		return _credentialValidMillis;
	}

	/**
	 * returns a Date object representing when our self-signed credentials should expire, if they
	 * are not created yet.
	 */
	static public Date credsValidUntil()
	{
		return new Date(System.currentTimeMillis() + _credentialValidMillis + 10000);
	}

	public void addArgument(String argument) throws ToolException
	{
		if (_setter == null)
			_setter = new OptionSetter(this);
		Class<? extends ITool> toolClass = getClass();
		if (argument.startsWith("--"))
			handleLongOptionFlag(toolClass, argument.substring(2));
		else if (argument.startsWith("-"))
			handleShortOptionFlag(toolClass, argument.substring(1));
		else
			_arguments.add(argument);
	}

	public String description()
	{
		return _description;
	}

	public boolean isHidden()
	{
		return _isHidden;
	}

	public String usage()
	{
		return "\nUsage:\n" + _usage;
	}

	static private String readResource(LoadFileResource resource)
	{
		if (resource == null) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		BufferedReader reader = null;
		String line;
		try {
			reader = new BufferedReader(new InputStreamReader(resource.open()));

			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append('\n');
			}

			return builder.toString();
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to read resource \"" + resource.toString() + "\".", ioe);
		} catch (Exception e) {
			throw new RuntimeException("Unable to read resource \"" + resource.toString() + "\".", e);
		} finally {
			StreamUtils.close(reader);
		}
	}

	protected void handleLongOptionFlag(Class<? extends ITool> toolClass, String optionFlag) throws ToolException
	{
		int index = optionFlag.indexOf('=');
		if (index > 0) {
			handleOption(toolClass, optionFlag.substring(0, index), optionFlag.substring(index + 1));
		} else
			handleFlag(toolClass, optionFlag);
	}

	protected void handleShortOptionFlag(Class<? extends ITool> toolClass, String optionFlags) throws ToolException
	{
		int len = optionFlags.length();
		for (int lcv = 0; lcv < len; lcv++) {
			char c = optionFlags.charAt(lcv);
			if (lcv + 1 >= len)
				handleFlag(toolClass, Character.toString(c));
			else {
				char next = optionFlags.charAt(lcv + 1);
				if (next != '=')
					handleFlag(toolClass, Character.toString(c));
				else
					handleOption(toolClass, Character.toString(c), optionFlags.substring(lcv + 2));
			}
		}
	}

	private void handleOption(Class<? extends ITool> toolClass, String option, String value) throws ToolException
	{
		_setter.set(option, value);
	}

	private void handleFlag(Class<? extends ITool> toolClass, String flag) throws ToolException
	{
		_setter.set(flag);
	}

	protected RNSPath expandSingleton(String pathExpression) throws InvalidToolUsageException
	{
		return expandSingleton(null, pathExpression);
	}

	protected RNSPath expandSingleton(RNSPath current, String pathExpression) throws InvalidToolUsageException
	{
		return expandSingleton(current, pathExpression, null);
	}

	protected RNSPath expandSingleton(RNSPath current, String pathExpression, FilterFactory factoryType)
		throws InvalidToolUsageException
	{
		if (current == null)
			current = RNSPath.getCurrent();

		try {
			return current.expandSingleton(pathExpression, factoryType);
		} catch (RNSMultiLookupResultException rmlre) {
			throw new InvalidToolUsageException("Path expanded to too many entries.");
		}
	}

	public ToolCategory getCategory()
	{
		return _category;
	}

	public String getManPage()
	{
		if (_manPage == null)
			return null;
		else
			return _manPage;
	}
}
