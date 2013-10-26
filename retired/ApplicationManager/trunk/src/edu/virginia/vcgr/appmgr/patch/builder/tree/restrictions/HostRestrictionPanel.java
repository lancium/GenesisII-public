package edu.virginia.vcgr.appmgr.patch.builder.tree.restrictions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import edu.virginia.vcgr.appmgr.patch.HostRestriction;

public class HostRestrictionPanel extends JPanel
{
	static final long serialVersionUID = 0L;

	private JRadioButton _isHostnameButton;
	private JRadioButton _isIPAddressButton;
	private JTextField _entryField;
	private JCheckBox _isRegularExpressionButton;

	public HostRestrictionPanel(HostRestriction restriction)
	{
		super(new GridBagLayout());

		add(_isHostnameButton = new JRadioButton("Hostname"), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 5, 5));
		add(_isIPAddressButton = new JRadioButton("IP Address"), new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 5, 5));
		add(_entryField = new JTextField(16), new GridBagConstraints(1, 0, 1, 2, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(_isRegularExpressionButton = new JCheckBox("Regular Expression"), new GridBagConstraints(2, 0, 1, 2, 0.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		ButtonGroup group = new ButtonGroup();
		group.add(_isHostnameButton);
		group.add(_isIPAddressButton);

		if (restriction != null) {
			_isHostnameButton.setSelected(restriction.isHostname());
			_isIPAddressButton.setSelected(!restriction.isHostname());
			_entryField.setText(restriction.getValue());
			_isRegularExpressionButton.setSelected(restriction.isRegularExpression());
		} else {
			_isHostnameButton.setSelected(true);
		}
	}

	public HostRestriction getRestrictions()
	{
		boolean isHostname = _isHostnameButton.isSelected();
		boolean isRegEx = _isRegularExpressionButton.isSelected();
		String value = _entryField.getText();

		if (value == null || value.length() == 0)
			return null;

		if (isHostname) {
			if (isRegEx)
				return HostRestriction.restrictToHostnamePattern(value);
			else
				return HostRestriction.restrictToHostname(value);
		} else {
			if (isRegEx)
				return HostRestriction.restrictToIPAddressPattern(value);
			else
				return HostRestriction.restrictToIPAddress(value);
		}
	}
}