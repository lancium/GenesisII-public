package edu.virginia.vcgr.genii.gjt.prefs;

public interface ToolPreferenceListener {
	public void preferenceChanged(ToolPreferences preferences,
			ToolPreference preference, Object newValue);
}