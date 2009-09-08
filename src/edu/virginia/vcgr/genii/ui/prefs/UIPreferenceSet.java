package edu.virginia.vcgr.genii.ui.prefs;

import java.util.prefs.Preferences;

import javax.swing.JPanel;

public interface UIPreferenceSet
{
	public String preferenceSetName();
	
	public void load(Preferences uiPreferencesRoot);
	public void store(Preferences uiPreferencesRoot);
	
	public JPanel createEditor();
	public void load(JPanel editor);
}