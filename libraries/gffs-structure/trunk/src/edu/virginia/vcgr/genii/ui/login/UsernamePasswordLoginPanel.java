package edu.virginia.vcgr.genii.ui.login;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.ui.UIContext;

final class UsernamePasswordLoginPanel extends LoginPanel
{
	static final long serialVersionUID = 0L;

	static final private String NAME = "Username/Password";

	private JTextField _username = new JTextField(16);
	private JPasswordField _password = new JPasswordField(16);

	UsernamePasswordLoginPanel()
	{
		setName(NAME);

		add(new JLabel("Username"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_username, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Password"), new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_password, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));

		_username.addCaretListener(new UsernameListener());
	}

	@Override
	final public Collection<NuCredential> doLogin(UIContext uiContext) throws Throwable
	{
		if (_username.getText().length() == 0)
			JOptionPane.showMessageDialog(this, "Username cannot be empty!", "Invalid Login", JOptionPane.ERROR_MESSAGE);
		else {
			Collection<NuCredential> ret = new ArrayList<NuCredential>(1);
			ret.add(new UsernamePasswordIdentity(_username.getText(), new String(_password.getPassword())));
			return ret;
		}

		return null;
	}

	@Override
	final public boolean isLoginInformationValid()
	{
		return _username.getText().length() > 0;
	}

	private class UsernameListener implements CaretListener
	{
		@Override
		final public void caretUpdate(CaretEvent e)
		{
			fireLoginInformationValid(isLoginInformationValid());
		}
	}
}