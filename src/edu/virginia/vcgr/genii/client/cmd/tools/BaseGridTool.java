package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cmd.ITool;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSMultiLookupResultException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.filters.FilterFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;

public abstract class BaseGridTool implements ITool
{
	private HashMap<String, Method> _setters = 
		new HashMap<String, Method>();
	
	private String _description;
	private String _usage;
	private boolean _isHidden;
	
	private boolean _useGui = true;
	private List<String> _arguments = new ArrayList<String>();
	
	protected BaseGridTool(String description, String usage, boolean isHidden)
	{
		_description = description;
		_usage = usage;
		_isHidden = isHidden;
	}
	
	protected BaseGridTool(String description, FileResource usageResource,
		boolean isHidden)
	{
		this(description, readResource(usageResource), isHidden);
	}
	
	protected BaseGridTool(FileResource descriptionResource,
		FileResource usageResource, boolean isHidden)
	{
		this(readResource(descriptionResource),
			readResource(usageResource), isHidden);
	}
	
	protected boolean useGui()
	{
		return _useGui;
	}
	
	protected PrintWriter stdout;
	protected PrintWriter stderr;
	protected BufferedReader stdin;
	
	public final int run(Writer out, Writer err, Reader in)
		throws Throwable
	{
		try
		{
			stdout = (out instanceof PrintWriter) ? (PrintWriter)out : new PrintWriter(out, true);
			stderr = (err instanceof PrintWriter) ? (PrintWriter)err : new PrintWriter(err, true);
			stdin = (in instanceof BufferedReader) ? (BufferedReader)in : new BufferedReader(in);
			try
			{
				verify();
				return runCommand();
			}
			catch(InvalidToolUsageException itue)
			{
				stderr.print(itue.getLocalizedMessage());
				stderr.print(usage());
				stderr.println();
				return -1;
			}
		}
		catch (UserCancelException uce)
		{
			return 0;
		}
		finally
		{
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
	
	public void setNo_gui()
	{
		_useGui = false;
	}
	
	public void addArgument(String argument) throws ToolException
	{
		Class<? extends ITool> toolClass = getClass();
		
		if (argument.startsWith("--"))
			handleLongOptionFlag(
				toolClass, argument.substring(2));
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
		return _usage;
	}
	
	protected abstract void verify() throws ToolException;
	
	protected abstract int runCommand() throws Throwable;
	
	static private String readResource(FileResource resource)
	{
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = null;
		String line;
		
		try
		{
			reader = new BufferedReader(new InputStreamReader(
				resource.open()));
		
			while ( (line = reader.readLine()) != null)
			{
				builder.append(line);
				builder.append('\n');
			}
	
			return builder.toString();
		}
		catch (IOException ioe)
		{
			throw new RuntimeException(
				"Unable to read resource \"" + resource.toString() 
				+ "\".", ioe);
		}
		finally
		{
			StreamUtils.close(reader);
		}
	}
	
	private void handleLongOptionFlag(
		Class<? extends ITool> toolClass, String optionFlag)
			throws ToolException
	{
		int index = optionFlag.indexOf('=');
		if (index > 0)
		{
			handleOption(toolClass, 
				optionFlag.substring(0, index),
				optionFlag.substring(index + 1));
		} else
			handleFlag(toolClass, optionFlag);
	}
	
	private void handleShortOptionFlag(
		Class<? extends ITool> toolClass, String optionFlags)
			throws ToolException
	{
		int len = optionFlags.length();
		for (int lcv = 0; lcv < len; lcv++)
		{
			char c = optionFlags.charAt(lcv);
			if (lcv + 1 >= len)
				handleFlag(toolClass, Character.toString(c));
			else
			{
				char next = optionFlags.charAt(lcv+1);
				if (next != '=')
					handleFlag(toolClass, Character.toString(c));
				else
					handleOption(toolClass, Character.toString(c), 
						optionFlags.substring(lcv + 2));
			}
		}
	}
	
	static private String formatMethodPortion(String optionFlag)
	{
		optionFlag = optionFlag.substring(0, 1).toUpperCase() +
			optionFlag.substring(1);
		return optionFlag.replaceAll("-", "_");
	}
	
	private void handleOption(
		Class<? extends ITool> toolClass, String option, String value)
			throws ToolException
	{
		Method method;
		String optionMethod = formatMethodPortion(option);
		
		synchronized(_setters)
		{
			method = _setters.get(option);
			if (method != null && method.getName().startsWith("set"))
			{
				// We have this method in our table which means we've already
				// set this option before.
				throw new ToolException("Invalid Usage.  Option \"" +
					option + "\" can only be given once.");
			}
			
			if (method == null)
			{
				try
				{
					// look for set method
					method = toolClass.getMethod(
						String.format("set%s", optionMethod),
						String.class);
				}
				catch (NoSuchMethodException nsme)
				{
					try
					{
						// look for add method
						method = toolClass.getMethod(
							String.format("add%s", optionMethod),
							String.class);
					}
					catch (NoSuchMethodException nsme2)
					{
						throw new ToolException("Option \"" + option +
							"\" is unrecognized."); 
					}
				}
				
				_setters.put(option, method);
			}
		}
			
		try
		{
			method.invoke(this, new Object[] { value });
		}
		catch (InvocationTargetException ite)
		{
			throw new ToolException(
				"Tool threw an exception while setting option \"" + 
				option + "\".", ite.getCause());
		}
		catch (IllegalAccessException iae)
		{
			throw new ToolException(
				"Tool cannot handle option \"" + option +
				"\" due to protection issue.", iae);
		}
	}
	
	private void handleFlag(
		Class<? extends ITool> toolClass, String flag)
			throws ToolException
	{
		String flagMethod = formatMethodPortion(flag);
		Method method;
		
		synchronized(_setters)
		{
			method = _setters.get(flag);
			if (method == null)
			{
				try
				{
					// look for add method
					method = toolClass.getMethod(
						String.format("set%s", flagMethod));
				}
				catch (NoSuchMethodException nsme2)
				{
					throw new ToolException("Flag \"" + flag +
						"\" is unrecognized."); 
				}
				
				_setters.put(flag, method);
			}
		}
		
		try
		{
			method.invoke(this, new Object[0]);
		}
		catch (InvocationTargetException ite)
		{
			throw new ToolException(
				"Tool threw an exception while setting flag \"" + 
				flag + "\".", ite.getCause());
		}
		catch (IllegalAccessException iae)
		{
			throw new ToolException(
				"Tool cannot handle flag \"" + flag +
				"\" due to protection issue.", iae);
		}
	}
	
	protected RNSPath expandSingleton(String pathExpression)
		throws InvalidToolUsageException
	{
		return expandSingleton(null, pathExpression);
	}
	
	protected RNSPath expandSingleton(RNSPath current, String pathExpression)
		throws InvalidToolUsageException
	{
		return expandSingleton(current, pathExpression, null);
	}
	
	protected RNSPath expandSingleton(
		RNSPath current, String pathExpression, FilterFactory factoryType)
		throws InvalidToolUsageException
	{
		if (current == null)
			current = RNSPath.getCurrent();
		
		try
		{
			return current.expandSingleton(pathExpression, factoryType);
		}
		catch (RNSMultiLookupResultException rmlre)
		{
			throw new InvalidToolUsageException(
				"Path expanded to too many entries.");
		}
	}
}