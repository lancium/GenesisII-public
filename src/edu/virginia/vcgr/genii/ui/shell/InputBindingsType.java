package edu.virginia.vcgr.genii.ui.shell;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.ui.shell.emacs.EmacsInputBindings;
import edu.virginia.vcgr.genii.ui.shell.vi.ViInputBindings;

public enum InputBindingsType {
	DEFAULT("default", DefaultInputBindings.class), EMACS("emacs", EmacsInputBindings.class), VI("vi", ViInputBindings.class);

	private String _humanName;
	private Class<? extends InputBindings> _bindingsClass;

	private InputBindingsType(String humanName, Class<? extends InputBindings> bindingsClass)
	{
		_humanName = humanName;
		_bindingsClass = bindingsClass;
	}

	@Override
	final public String toString()
	{
		return _humanName;
	}

	final public InputBindings createBindings()
	{
		try {
			return _bindingsClass.newInstance();
		} catch (InstantiationException e) {
			throw new ConfigurationException("Unable to create input bindings.", e);
		} catch (IllegalAccessException e) {
			throw new ConfigurationException("Unable to create input bindings.", e);
		}
	}

	static public InputBindingsType defaultBindings()
	{
		return DEFAULT;
	}
}