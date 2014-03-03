package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import edu.virginia.vcgr.genii.gjt.conf.Configuration;
import edu.virginia.vcgr.genii.gjt.prefs.ToolPreference;
import edu.virginia.vcgr.genii.gjt.prefs.ToolPreferenceListener;
import edu.virginia.vcgr.genii.gjt.prefs.ToolPreferences;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;

@SuppressWarnings("rawtypes")
public class OperatingSystemComboModel extends AbstractListModel implements
		ComboBoxModel {
	static final long serialVersionUID = 0L;

	private OperatingSystemNames _selection = null;
	private Vector<OperatingSystemNames> _elements;

	private void resetModel(ToolPreferences preferences) {
		_elements.clear();
		boolean filter = ((Boolean) (preferences
				.preference(ToolPreference.LimitOperatingSystemChoices)))
				.booleanValue();

		_elements.add(null);

		for (OperatingSystemNames name : OperatingSystemNames.values()) {
			if (!filter
					|| Configuration.configuration.operatingSystemNamesFilter()
							.contains(name))
				_elements.add(name);
		}

		if (!_elements.contains(_selection))
			_elements.add(_selection);

		fireContentsChanged(this, 0, _elements.size() - 1);
	}

	OperatingSystemComboModel(ToolPreferences preferences) {
		preferences.addPreferenceListener(new PreferenceListener(),
				ToolPreference.LimitOperatingSystemChoices);

		_elements = new Vector<OperatingSystemNames>();
		resetModel(preferences);
	}

	@Override
	public Object getSelectedItem() {
		return _selection;
	}

	@Override
	public void setSelectedItem(Object anItem) {
		_selection = (OperatingSystemNames) anItem;
		if (!_elements.contains(anItem)) {
			_elements.add(_selection);
			fireIntervalAdded(this, _elements.size() - 1, _elements.size() - 1);
		}
	}

	@Override
	public Object getElementAt(int index) {
		return _elements.get(index);
	}

	@Override
	public int getSize() {
		return _elements.size();
	}

	private class PreferenceListener implements ToolPreferenceListener {
		@Override
		public void preferenceChanged(ToolPreferences preferences,
				ToolPreference preference, Object newValue) {
			resetModel(preferences);
		}
	}
}