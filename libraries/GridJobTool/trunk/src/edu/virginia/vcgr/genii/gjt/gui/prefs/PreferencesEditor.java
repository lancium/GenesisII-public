package edu.virginia.vcgr.genii.gjt.gui.prefs;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import edu.virginia.vcgr.genii.gjt.gui.util.ButtonPanel;
import edu.virginia.vcgr.genii.gjt.prefs.ToolPreference;
import edu.virginia.vcgr.genii.gjt.prefs.ToolPreferences;

public class PreferencesEditor extends JDialog {
	private static final long serialVersionUID = 0L;

	private JCheckBox _popupForWarnings = new JCheckBox(
			"Show Popup for Warnings?");
	private JSpinner _paramSweepLimit = new JSpinner(new SpinnerNumberModel(
			1000, 0, Integer.MAX_VALUE, 1));
	private JCheckBox _limitOperatingSystemChoices = new JCheckBox(
			"Limit Operating System Choices?");
	private JCheckBox _limitProcessorChoices = new JCheckBox(
			"Limit Processor Architecture Choices?");

	public PreferencesEditor(ToolPreferences preferences) {
		setTitle("Preferences");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);

		Container container = getContentPane();
		container.setLayout(new GridBagLayout());

		_popupForWarnings.setSelected(((Boolean) preferences
				.preference(ToolPreference.PopupForWarnings)).booleanValue());
		_paramSweepLimit.setValue(preferences
				.preference(ToolPreference.ParameterSweepPopupLimit));
		_limitOperatingSystemChoices.setSelected(((Boolean) preferences
				.preference(ToolPreference.LimitOperatingSystemChoices))
				.booleanValue());
		_limitProcessorChoices.setSelected(((Boolean) preferences
				.preference(ToolPreference.LimitProcessorArchitectures))
				.booleanValue());

		container.add(_popupForWarnings, new GridBagConstraints(0, 0, 2, 1,
				1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add(new JLabel("Parameter Sweep Warning Limit"),
				new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 5, 5));
		container.add(_paramSweepLimit, new GridBagConstraints(1, 1, 1, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add(_limitOperatingSystemChoices, new GridBagConstraints(0,
				2, 2, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		container.add(_limitProcessorChoices, new GridBagConstraints(0, 3, 2,
				1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));

		JButton button = new JButton(new OKAction(preferences));
		getRootPane().setDefaultButton(button);

		container.add(ButtonPanel.createHorizontalPanel(button,
				new CancelAction()), new GridBagConstraints(0, 4, 2, 1, 1.0,
				1.0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
	}

	private class OKAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private ToolPreferences _toolPreferences;

		private OKAction(ToolPreferences toolPreferences) {
			super("OK");

			_toolPreferences = toolPreferences;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();

			Map<ToolPreference, Object> preferences = _toolPreferences
					.preferences();

			preferences.put(ToolPreference.PopupForWarnings, new Boolean(
					_popupForWarnings.isSelected()));
			preferences.put(ToolPreference.ParameterSweepPopupLimit,
					_paramSweepLimit.getValue());
			preferences.put(ToolPreference.LimitOperatingSystemChoices,
					new Boolean(_limitOperatingSystemChoices.isSelected()));
			preferences.put(ToolPreference.LimitProcessorArchitectures,
					new Boolean(_limitProcessorChoices.isSelected()));

			_toolPreferences.commit(preferences);
		}
	}

	private class CancelAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private CancelAction() {
			super("Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
}
