package edu.virginia.vcgr.genii.ui.prefs.security;

import java.util.prefs.Preferences;

import javax.swing.JPanel;

import edu.virginia.vcgr.genii.client.security.VerbosityLevel;
import edu.virginia.vcgr.genii.ui.prefs.AbstractUIPreferenceSet;

public class SecurityUIPreferenceSet extends AbstractUIPreferenceSet
{
	static final private String PREFERENCE_SET_TITLE = "Security";
	
	static final private String PREFERENCE_NODE_NAME = "security";
	
	static final private String ACL_VERBOSITY_LEVEL_KEY = "acl-verbosity-level";
	
	private VerbosityLevel _verbosityLevel = VerbosityLevel.LOW;
	
	public SecurityUIPreferenceSet()
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
		_verbosityLevel = VerbosityLevel.valueOf(
			prefNode.get(ACL_VERBOSITY_LEVEL_KEY, VerbosityLevel.LOW.name()));
	}

	@Override
	protected void storeImpl(Preferences prefNode)
	{
		prefNode.put(ACL_VERBOSITY_LEVEL_KEY, _verbosityLevel.name());
	}

	@Override
	public JPanel createEditor()
	{
		return new SecurityUIPreferenceSetEditor(_verbosityLevel);
	}

	@Override
	public void load(JPanel editor)
	{
		_verbosityLevel =
			((SecurityUIPreferenceSetEditor)editor).aclVerbosityLevel();
	}
	
	final public VerbosityLevel aclVerbosityLevel()
	{
		return _verbosityLevel;
	}
}