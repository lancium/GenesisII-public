package edu.virginia.vcgr.genii.gjt.prefs;

public enum ToolPreference {
	PopupForWarnings(Boolean.TRUE),
	ParameterSweepPopupLimit(new Integer(1000)),
	LimitOperatingSystemChoices(Boolean.TRUE),
	LimitProcessorArchitectures(Boolean.TRUE);

	private Object _defaultValue;

	private ToolPreference(Object defaultValue)
	{
		if (defaultValue == null)
			throw new IllegalArgumentException("The default value for a preference cannot be null.");

		_defaultValue = defaultValue;
	}

	final public Object defaultValue()
	{
		return _defaultValue;
	}
}