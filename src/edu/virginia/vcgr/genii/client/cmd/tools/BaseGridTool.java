package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cmd.ITool;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSMultiLookupResultException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rns.filters.FilterFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;

public abstract class BaseGridTool implements ITool
{
	private OptionSetter _setter = null;

	private String _description;
	private String _usage;
	private String _manPage = null;

	private boolean _isHidden;
	private ToolCategory _category = ToolCategory.MISC;

	private boolean _useGui = true;
	protected List<String> _arguments = new ArrayList<String>();

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

	protected void addManPage(FileResource manPage)
	{
		_manPage = readResource(manPage);
	}

	private BaseGridTool(String description, String usage, boolean isHidden)
	{
		_description = description;
		_usage = usage;
		_isHidden = isHidden;
	}

	protected BaseGridTool(FileResource descriptionResource, FileResource usageResource, boolean isHidden, ToolCategory cat)
	{
		this(readResource(descriptionResource), readResource(usageResource), isHidden);
		_category = cat;
	}

	protected BaseGridTool(FileResource descriptionResource, FileResource usageResource, boolean isHidden)
	{
		this(readResource(descriptionResource), readResource(usageResource), isHidden);
	}

	protected boolean useGui()
	{
		return _useGui;
	}

	protected void overrideCategory(ToolCategory cat)
	{
		_category = cat;
	}

	protected PrintWriter stdout;
	protected PrintWriter stderr;
	protected BufferedReader stdin;

	public final int run(Writer out, Writer err, Reader in) throws Throwable
	{
		try {
			stdout = (out instanceof PrintWriter) ? (PrintWriter) out : new PrintWriter(out, true);
			stderr = (err instanceof PrintWriter) ? (PrintWriter) err : new PrintWriter(err, true);
			stdin = (in instanceof BufferedReader) ? (BufferedReader) in : new BufferedReader(in);
			try {
				verify();
				return runCommand();
			} catch (InvalidToolUsageException itue) {
				stderr.print(itue.getLocalizedMessage());
				stderr.print(usage());
				stderr.println();
				return -1;
			}
		} catch (UserCancelException uce) {
			return 0;
		} finally {
			stdout = null;
			stderr = null;
			stdin = null;
		}
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

	protected abstract void verify() throws ToolException;

	protected abstract int runCommand() throws Throwable;

	static private String readResource(FileResource resource)
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

	private void handleLongOptionFlag(Class<? extends ITool> toolClass, String optionFlag) throws ToolException
	{
		int index = optionFlag.indexOf('=');
		if (index > 0) {
			handleOption(toolClass, optionFlag.substring(0, index), optionFlag.substring(index + 1));
		} else
			handleFlag(toolClass, optionFlag);
	}

	private void handleShortOptionFlag(Class<? extends ITool> toolClass, String optionFlags) throws ToolException
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