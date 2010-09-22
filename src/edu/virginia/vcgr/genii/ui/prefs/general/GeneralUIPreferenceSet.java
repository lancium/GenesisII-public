package edu.virginia.vcgr.genii.ui.prefs.general;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JPanel;

import edu.virginia.vcgr.genii.client.install.ContainerInformation;
import edu.virginia.vcgr.genii.client.install.InstallationState;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;
import edu.virginia.vcgr.genii.ui.prefs.AbstractUIPreferenceSet;

public class GeneralUIPreferenceSet extends AbstractUIPreferenceSet
{
	static final private String PREFERENCE_SET_TITLE = "General";
	
	static final private String PREFERENCE_NODE_NAME = "general";
	
	static final private String LOCAL_CONTAINER_KEY = "local-container";
	
	private boolean _localChecked = false;
	private Object _localCheckedLock = new Object();
	private String _localContainerName;
	
	@Override
	final protected Preferences preferenceNode(Preferences uiPreferencesRoot)
	{
		return uiPreferencesRoot.node(PREFERENCE_NODE_NAME);
	}

	@Override
	protected void loadImpl(Preferences prefNode)
	{
		_localContainerName = prefNode.get(LOCAL_CONTAINER_KEY, null);
	}

	@Override
	protected void storeImpl(Preferences prefNode)
	{
		prefNode.put(LOCAL_CONTAINER_KEY, _localContainerName);
	}
	
	public GeneralUIPreferenceSet()
	{
		super(PREFERENCE_SET_TITLE);
	}
	
	final public String localContainerName()
	{
		synchronized(_localCheckedLock)
		{
			if (!_localChecked)
			{
				_localChecked = true;
				try
				{
					HashMap<String, ContainerInformation> info =
						InstallationState.getRunningContainers();
					if (info != null)
					{
						if (_localContainerName != null)
						{
							if (!info.containsKey(_localContainerName))
								_localContainerName = null;
						}
						
						if (_localContainerName == null)
						{
							List<String> list = new Vector<String>(
								info.keySet());
							if (list.size() > 0)
							{
								Collections.sort(list);
								_localContainerName = list.get(0);
							}
						}
					}
				}
				catch (FileLockException fle)
				{
					// Can't do anything about this right now.
				}
			}
		}
		
		return _localContainerName;
	}

	@Override
	final public JPanel createEditor()
	{
		return new GeneralUIPreferenceSetEditor(this);
	}

	@Override
	final public void load(JPanel editor)
	{
		_localContainerName =
			((GeneralUIPreferenceSetEditor)editor).selectedContainer();
	}
}