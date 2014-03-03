package edu.virginia.g3.fsview.authgui.domuserpass;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

class DomainUsernamePasswordPanel extends JPanel
{
	static final long serialVersionUID = 0L;

	static final private int DOMAIN_FIELD = 0;
	static final private int USERNAME_FIELD = 1;
	static final private int PASSWORD_FIELD = 2;

	private DomainUsernamePasswordModel _model;

	private JTextField _domain = new JTextField(16);
	private JTextField _username = new JTextField(16);
	private JPasswordField _password = new JPasswordField(16);

	DomainUsernamePasswordPanel(DomainUsernamePasswordModel model)
	{
		super(new GridBagLayout());

		_model = model;

		_domain.addCaretListener(new CaretListenerImpl(DOMAIN_FIELD));
		_username.addCaretListener(new CaretListenerImpl(USERNAME_FIELD));
		_password.addCaretListener(new CaretListenerImpl(PASSWORD_FIELD));

		add(new JLabel("Domain"), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_domain, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Username"), new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_username, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Password"), new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_password, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
	}

	private class CaretListenerImpl implements CaretListener
	{
		private int _field;

		private CaretListenerImpl(int field)
		{
			_field = field;
		}

		@Override
		final public void caretUpdate(CaretEvent e)
		{
			switch (_field) {
				case DOMAIN_FIELD:
					_model.domain(_domain.getText());
					break;

				case USERNAME_FIELD:
					_model.username(_username.getText());
					break;

				case PASSWORD_FIELD:
					_model.password(new String(_password.getPassword()));
					break;
			}
		}
	}
}