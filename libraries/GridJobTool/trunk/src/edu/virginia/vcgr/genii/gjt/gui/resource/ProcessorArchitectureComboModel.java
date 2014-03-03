package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import edu.virginia.vcgr.genii.gjt.conf.Configuration;
import edu.virginia.vcgr.genii.gjt.prefs.ToolPreference;
import edu.virginia.vcgr.genii.gjt.prefs.ToolPreferenceListener;
import edu.virginia.vcgr.genii.gjt.prefs.ToolPreferences;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

@SuppressWarnings("rawtypes")
public class ProcessorArchitectureComboModel extends AbstractListModel
		implements ComboBoxModel {
	static final long serialVersionUID = 0L;

	private ProcessorArchitecture _selection = null;
	private Vector<ProcessorArchitecture> _elements;

	private void resetModel(ToolPreferences preferences) {
		_elements.clear();
		boolean filter = ((Boolean) (preferences
				.preference(ToolPreference.LimitProcessorArchitectures)))
				.booleanValue();

		_elements.add(null);

		for (ProcessorArchitecture name : ProcessorArchitecture.values()) {
			if (!filter
					|| Configuration.configuration
							.processorArchitectureFilter().contains(name))
				_elements.add(name);
		}

		if (!_elements.contains(_selection))
			_elements.add(_selection);

		fireContentsChanged(this, 0, _elements.size() - 1);
	}

	public ProcessorArchitectureComboModel(ToolPreferences preferences) {
		preferences.addPreferenceListener(new PreferenceListener(),
				ToolPreference.LimitOperatingSystemChoices);

		_elements = new Vector<ProcessorArchitecture>();
		resetModel(preferences);
	}

	@Override
	public Object getSelectedItem() {
		return _selection;
	}

	@Override
	public void setSelectedItem(Object anItem) {
		_selection = (ProcessorArchitecture) anItem;
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