package edu.virginia.vcgr.genii.ui.prefs;

import java.util.prefs.Preferences;

public abstract class AbstractUIPreferenceSet implements UIPreferenceSet
{
	private String _preferenceSetName;

	protected abstract Preferences preferenceNode(Preferences uiPreferencesRoot);

	protected abstract void loadImpl(Preferences prefNode);

	protected abstract void storeImpl(Preferences prefNode);

	protected AbstractUIPreferenceSet(String preferenceSetName)
	{
		_preferenceSetName = preferenceSetName;
	}

	@Override
	final public String preferenceSetName()
	{
		return _preferenceSetName;
	}

	@Override
	final public void load(Preferences uiPreferencesRoot)
	{
		loadImpl(preferenceNode(uiPreferencesRoot));
	}

	@Override
	final public void store(Preferences uiPreferencesRoot)
	{
		storeImpl(preferenceNode(uiPreferencesRoot));
	}
}