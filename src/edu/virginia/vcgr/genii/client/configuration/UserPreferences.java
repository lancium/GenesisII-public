package edu.virginia.vcgr.genii.client.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.macro.MacroResolver;
import org.morgan.util.macro.MacroUtils;
import org.morgan.util.macro.MapMacroResolver;
import org.morgan.util.macro.PrefixMapMacroResolver;
import org.morgan.util.macro.PropertiesMacroResolver;

import edu.virginia.vcgr.genii.client.cmd.IExceptionHandler;
import edu.virginia.vcgr.genii.client.cmd.SimpleExceptionHandler;
import edu.virginia.vcgr.genii.client.context.GridUserEnvironment;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class UserPreferences
{
	static private Log _logger = LogFactory.getLog(UserPreferences.class);
	
	static final private String PREFER_GUI_PREFERENCE_KEY =
		"prefer-gui";
	static final private String EXCEPTION_HANDLER_PREFERENCE_KEY =
		"exception-handler";
	static final private String SHELL_PROMPT_TEMPLATE_KEY =
		"shell-prompt-template";
	
	static final private boolean DEFAULT_PREFER_GUI = true;
	static final private IExceptionHandler DEFAULT_EXCEPTION_HANDLER =
		new SimpleExceptionHandler();
	static final private String DEFAULT_SHELL_PROMPT_TEMPLATE = "${net.name}:\\$>";
	
	static private class GridEnvironmentMacroResolver implements MacroResolver
	{
		@Override
		public String lookup(String key)
		{
			Map<String, String> env = 
				GridUserEnvironment.getGridUserEnvironment();
			return env.get(key);
		}	
	}
	
	static private MacroResolver createShellPromptMacroResolver()
	{
		PrefixMapMacroResolver prefixResolver = new PrefixMapMacroResolver("grid");
		
		Map<String, Object> dynamicGridResolution =
			new HashMap<String, Object>();
		dynamicGridResolution.put("pwd", new Object(){
			@Override
			public String toString()
			{
				return RNSPath.getCurrent().toString();
			}
		});
		dynamicGridResolution.put("abbreviated.pwd", new Object(){
			@Override
			public String toString()
			{
				return RNSPath.getCurrent().getName();
			}
		});
		dynamicGridResolution.put("deployment", new Object() {
			@Override
			public String toString()
			{
				return new DeploymentName().toString();
			}
		});
		dynamicGridResolution.put("net.name", new Object() {
			@Override
			public String toString()
			{
				String property = System.getProperty(
					"edu.virginia.vcgr.genii.net-name");
				if (property != null)
					return property;
				
				return "Genesis II";
			}
		});
		
		prefixResolver.addResolver("grid",
			new MapMacroResolver(dynamicGridResolution));
		prefixResolver.addResolver("env", 
			new MapMacroResolver(System.getenv()));
		prefixResolver.addResolver("java",
			new PropertiesMacroResolver(System.getProperties()));
		prefixResolver.addResolver("gridenv",
			new GridEnvironmentMacroResolver());
		
		return prefixResolver;
	}
	
	static private class ShellPromptImpl implements ShellPrompt
	{
		private MacroUtils _macros;
		private String _template;
		
		private ShellPromptImpl(
			String template)
		{
			_macros = new MacroUtils(createShellPromptMacroResolver());
			_template = template;
		}
		
		synchronized private void template(String template)
		{
			_template = template;
		}
		
		@Override
		final public String toString()
		{
			String template;
			
			synchronized(this)
			{
				template = _template;
			}
			
			return _macros.toString(template);
		}
		
		final public String describe()
		{
			return _template;
		}
	}
	
	private boolean _preferGUI;
	private IExceptionHandler _exceptionHandler;
	private ShellPromptImpl _shellPrompt;
	
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
		_shellPrompt = new ShellPromptImpl(prefs.get(
			SHELL_PROMPT_TEMPLATE_KEY, DEFAULT_SHELL_PROMPT_TEMPLATE));
		
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
	
	public ShellPrompt shellPrompt()
	{
		return _shellPrompt;
	}
	
	public void shellPromptTemplate(String template) throws BackingStoreException
	{
		_shellPrompt.template(template);
		
		Preferences prefs = Preferences.userNodeForPackage(
			UserPreferences.class);
		
		prefs.put(SHELL_PROMPT_TEMPLATE_KEY, _shellPrompt._template);
		prefs.flush();
	}
	
	static private UserPreferences _prefs = new UserPreferences();
	
	static public UserPreferences preferences()
	{
		return _prefs;
	}
}