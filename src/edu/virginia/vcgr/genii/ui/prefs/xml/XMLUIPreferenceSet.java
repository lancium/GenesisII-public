package edu.virginia.vcgr.genii.ui.prefs.xml;

import java.util.prefs.Preferences;

import javax.swing.JPanel;

import edu.virginia.vcgr.genii.ui.prefs.AbstractUIPreferenceSet;

public class XMLUIPreferenceSet extends AbstractUIPreferenceSet
{
	static final private String PREFERENCE_SET_TITLE = "XML Displays";

	static final private String PREFERENCE_NODE_NAME = "xml displays";

	static final private String PREFER_TEXT_KEY = "prefer-text";

	private boolean _preferTextPane = true;

	public XMLUIPreferenceSet()
	{
		super(PREFERENCE_SET_TITLE);
	}

	@Override
	protected Preferences preferenceNode(Preferences uiPreferencesRoot)
	{
		return uiPreferencesRoot.node(PREFERENCE_NODE_NAME);
	}

	@Override
	protected void loadImpl(Preferences prefNode)
	{
		_preferTextPane = prefNode.getBoolean(PREFER_TEXT_KEY, true);
	}

	@Override
	protected void storeImpl(Preferences prefNode)
	{
		prefNode.putBoolean(PREFER_TEXT_KEY, _preferTextPane);
	}

	@Override
	public JPanel createEditor()
	{
		return new XMLUIPreferenceSetEditor(_preferTextPane);
	}

	@Override
	public void load(JPanel editor)
	{
		_preferTextPane = ((XMLUIPreferenceSetEditor) editor).preferText();
	}

	final public boolean preferText()
	{
		return _preferTextPane;
	}
}