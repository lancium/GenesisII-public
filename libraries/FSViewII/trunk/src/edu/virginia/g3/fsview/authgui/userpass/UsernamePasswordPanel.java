package edu.virginia.g3.fsview.authgui.userpass;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

class UsernamePasswordPanel extends JPanel
{
	static final long serialVersionUID = 0L;

	private UsernamePasswordInformationModel _model;

	private JTextField _username = new JTextField(16);
	private JPasswordField _password = new JPasswordField(16);

	UsernamePasswordPanel(UsernamePasswordInformationModel model)
	{
		super(new GridBagLayout());

		_model = model;

		_username.addCaretListener(new CaretListenerImpl(true));
		_password.addCaretListener(new CaretListenerImpl(false));

		add(new JLabel("Username"), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_username, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Password"), new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_password, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
	}

	private class CaretListenerImpl implements CaretListener
	{
		private boolean _isUsername;

		private CaretListenerImpl(boolean isUsername)
		{
			_isUsername = isUsername;
		}

		@Override
		final public void caretUpdate(CaretEvent e)
		{
			if (_isUsername) {
				_model.username(_username.getText());
			} else {
				_model.password(new String(_password.getPassword()));
			}
		}
	}
}