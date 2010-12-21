package edu.virginia.vcgr.genii.ui.prefs.history;

import java.util.prefs.Preferences;

import javax.swing.JPanel;

import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.ui.prefs.AbstractUIPreferenceSet;

public class HistoryUIPreferenceSet extends AbstractUIPreferenceSet
{
	static final private String PREFERENCE_SET_TITLE = "Resource History";
	
	static final private String PREFERENCE_NODE_NAME = "history";
	
	static final private String PREFERRED_LEVEL_KEY = "preferred-level";
	
	static final private HistoryEventLevel DEFAULT_PREFERRED_LEVEL =
		HistoryEventLevel.Information;
	
	private HistoryEventLevel _preferredLevel = null;
	
	@Override
	final protected Preferences preferenceNode(Preferences uiPreferencesRoot)
	{
		return uiPreferencesRoot.node(PREFERENCE_NODE_NAME);
	}

	@Override
	final protected void loadImpl(Preferences prefNode)
	{
		String value = prefNode.get(PREFERRED_LEVEL_KEY, null);
		if (value == null)
			_preferredLevel = null;
		else
			_preferredLevel = HistoryEventLevel.valueOf(value);
	}

	@Override
	final protected void storeImpl(Preferences prefNode)
	{
		if (_preferredLevel == null)
			prefNode.remove(PREFERRED_LEVEL_KEY);
		else
			prefNode.put(PREFERRED_LEVEL_KEY, _preferredLevel.name());
	}
	
	public HistoryUIPreferenceSet()
	{
		super(PREFERENCE_SET_TITLE);
	}
	
	final public HistoryEventLevel preferredLevel()
	{
		if (_preferredLevel == null)
			return DEFAULT_PREFERRED_LEVEL;
		
		return _preferredLevel;
	}
	
	@Override
	final public JPanel createEditor()
	{
		return new HistoryUIPreferenceEditor(this);
	}

	@Override
	final public void load(JPanel editor)
	{
		_preferredLevel = 
			((HistoryUIPreferenceEditor)editor).preferredLevel();
	}
}