package edu.virginia.vcgr.genii.client.configuration;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.IExceptionHandler;
import edu.virginia.vcgr.genii.client.cmd.SimpleExceptionHandler;

public class UserPreferences
{
	static private Log _logger = LogFactory.getLog(UserPreferences.class);
	
	static final private String PREFER_GUI_PREFERENCE_KEY =
		"prefer-gui";
	static final private String EXCEPTION_HANDLER_PREFERENCE_KEY =
		"exception-handler";
	
	static final private boolean DEFAULT_PREFER_GUI = true;
	static final private IExceptionHandler DEFAULT_EXCEPTION_HANDLER =
		new SimpleExceptionHandler();
	
	private boolean _preferGUI;
	private IExceptionHandler _exceptionHandler;
	
	private UserPreferences()
	{
		String exceptionHandlerClassName;
		
		Preferences prefs = Preferences.userNodeForPackage(
			UserPreferences.class);
		
		_preferGUI = prefs.getBoolean(
			PREFER_GUI_PREFERENCE_KEY, DEFAULT_PREFER_GUI);
		exceptionHandlerClassName = prefs.get(
			EXCEPTION_HANDLER_PREFERENCE_KEY,
			DEFAULT_EXCEPTION_HANDLER.getClass().getName());
		
		_exceptionHandler = createExceptionHandler(exceptionHandlerClassName);
	}
	
	@SuppressWarnings("unchecked")
	static private IExceptionHandler createExceptionHandler(String className)
	{
		try
		{
			ClassLoader loader = 
				Thread.currentThread().getContextClassLoader();
			Class<? extends IExceptionHandler> cl = 
				(Class<? extends IExceptionHandler>)loader.loadClass(
					className);
			return cl.getConstructor().newInstance();
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to load exception handler -- using default.",
				cause);
			return DEFAULT_EXCEPTION_HANDLER;
		}
	}
	
	public IExceptionHandler getExceptionHandler()
	{
		return _exceptionHandler;
	}
	
	public void setExceptionHandler(IExceptionHandler handler)
		throws BackingStoreException
	{
		_exceptionHandler = handler;
		
		Preferences prefs = Preferences.userNodeForPackage(
				UserPreferences.class);
			
		prefs.put(
			EXCEPTION_HANDLER_PREFERENCE_KEY,
			_exceptionHandler.getClass().getName());
		prefs.flush();
	}
	
	public boolean preferGUI()
	{
		return _preferGUI;
	}
	
	public void preferGUI(boolean preferGUI)
		throws BackingStoreException
	{
		_preferGUI = preferGUI;
		
		Preferences prefs = Preferences.userNodeForPackage(
				UserPreferences.class);
			
		prefs.putBoolean(PREFER_GUI_PREFERENCE_KEY, _preferGUI);
		prefs.flush();
	}
	
	static private UserPreferences _prefs = new UserPreferences();
	
	static public UserPreferences preferences()
	{
		return _prefs;
	}
}