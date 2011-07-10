package edu.virginia.vcgr.genii.client.cmd;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import edu.virginia.vcgr.genii.client.cmd.tools.ToolCategory;

public class ToolDescription
{
	private String _toolName;
	private String _description = null;
	private String _manPage = null;
	private Boolean _hidden = null;
	private String _usage = null;
	private Class<? extends ITool> _toolClass;
	private ITool _toolInstance = null;
	private ToolCategory _category = null;

	public ToolDescription(Class<? extends ITool> toolClass,
			String toolName)
	{
		_toolClass = toolClass;
		_toolName = toolName;

	}

	public Class<? extends ITool> getToolClass()
	{
		return _toolClass;
	}

	public String getToolName()
	{
		return _toolName;
	}

	synchronized public ToolCategory getCategory()
	throws ToolException
	{
		if (_category == null)
			_toolInstance = getToolInstance();

		_category = _toolInstance.getCategory();
		return _category;
	}
	synchronized public String getToolDescription()
	throws ToolException
	{
		if (_description == null)
			_toolInstance = getToolInstance();

		_description = _toolInstance.description();
		return _description;
	}

	synchronized public String getUsage()
	throws ToolException
	{
		if (_usage == null)
			_toolInstance = getToolInstance();

		_usage = _toolInstance.usage();
		return _usage;
	}

	synchronized public String getManPage()
	throws ToolException
	{
		if (_manPage == null)
			_toolInstance = getToolInstance();

		_manPage = _toolInstance.getManPage();
		return _manPage;
	}

	synchronized public boolean isHidden()
	throws ToolException
	{
		if (_hidden == null)
			_toolInstance = getToolInstance();

		_hidden = new Boolean(_toolInstance.isHidden()); 
		return _hidden.booleanValue();
	}

	synchronized public ITool getToolInstance()
	throws ToolException
	{
		try
		{
			if (_toolInstance == null)
			{
				Constructor<? extends ITool> constructor =
					_toolClass.getConstructor(new Class[0]);
				return constructor.newInstance(new Object[0]);
			} else
				return _toolInstance;
		}
		catch (NoSuchMethodException nsme)
		{
			throw new ToolException(
					"Unable to find appropriate no-arg constructor for " +
					"tool class \"" + _toolClass + "\".", nsme);
		}
		catch (IllegalAccessException iae)
		{
			throw new ToolException(
					"No-arg constructor for tool class \"" +
					_toolClass + "\" does not appear to be public.", iae);
		}
		catch (InvocationTargetException ite)
		{
			throw new ToolException("No-arg constructor for tool class \"" +
					_toolClass + "\" threw exception.", ite.getCause());

		}
		catch (InstantiationException ie)
		{
			throw new ToolException("Unknown error constructing tool class \"" +
					_toolClass + "\".", ie);
		}
		finally
		{
			_toolInstance = null;
		}
	}
}