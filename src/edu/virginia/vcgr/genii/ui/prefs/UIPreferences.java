package edu.virginia.vcgr.genii.ui.prefs;

import java.awt.Window;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import edu.virginia.vcgr.genii.ui.prefs.general.GeneralUIPreferenceSet;
import edu.virginia.vcgr.genii.ui.prefs.history.HistoryUIPreferenceSet;
import edu.virginia.vcgr.genii.ui.prefs.security.SecurityUIPreferenceSet;
import edu.virginia.vcgr.genii.ui.prefs.shell.ShellUIPreferenceSet;
import edu.virginia.vcgr.genii.ui.prefs.xml.XMLUIPreferenceSet;

public class UIPreferences
{
	private Map<Class<? extends UIPreferenceSet>, UIPreferenceSet> _preferenceSets = new LinkedHashMap<Class<? extends UIPreferenceSet>, UIPreferenceSet>();

	static private Preferences uiPreferencesRoot()
	{
		return Preferences.userNodeForPackage(UIPreferences.class);
	}

	public UIPreferences()
	{
		Preferences uiPreferencesRoot = uiPreferencesRoot();

		_preferenceSets.put(GeneralUIPreferenceSet.class, new GeneralUIPreferenceSet());
		_preferenceSets.put(ShellUIPreferenceSet.class, new ShellUIPreferenceSet());
		_preferenceSets.put(SecurityUIPreferenceSet.class, new SecurityUIPreferenceSet());
		_preferenceSets.put(HistoryUIPreferenceSet.class, new HistoryUIPreferenceSet());
		_preferenceSets.put(XMLUIPreferenceSet.class, new XMLUIPreferenceSet());

		for (UIPreferenceSet pSet : _preferenceSets.values())
			pSet.load(uiPreferencesRoot);
	}

	Collection<UIPreferenceSet> preferenceSets()
	{
		synchronized (_preferenceSets) {
			return Collections.unmodifiableCollection(_preferenceSets.values());
		}
	}

	public void store() throws BackingStoreException
	{
		Preferences uiPreferencesRoot = uiPreferencesRoot();

		synchronized (_preferenceSets) {
			for (UIPreferenceSet pSet : _preferenceSets.values())
				pSet.store(uiPreferencesRoot);
		}

		uiPreferencesRoot.flush();
	}

	public <Type extends UIPreferenceSet> Type preferenceSet(Class<Type> preferenceSetType)
	{
		synchronized (_preferenceSets) {
			return preferenceSetType.cast(_preferenceSets.get(preferenceSetType));
		}
	}

	public void launchEditor(Window owner) throws BackingStoreException
	{
		UIPreferencesDialog.launchEditor(owner, this);
	}
}