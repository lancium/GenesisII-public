package edu.virginia.g3.fsview.authgui.userpass;

import java.awt.Component;

import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformationTypes;
import edu.virginia.g3.fsview.UsernamePasswordAuthenticationInformation;
import edu.virginia.g3.fsview.authgui.AbstractAuthenticationInformationModel;
import edu.virginia.g3.fsview.gui.AcceptabilityState;

class UsernamePasswordInformationModel extends
		AbstractAuthenticationInformationModel {
	private String _username;
	private String _password;

	UsernamePasswordInformationModel() {
		super(FSViewAuthenticationInformationTypes.UsernamePassword);

		username("");
		password("");
	}

	final void username(String username) {
		if (username == null)
			username = "";

		_username = username;

		fireContentsChanged();
	}

	final void password(String password) {
		if (password == null)
			password = "";

		_password = password;

		fireContentsChanged();
	}

	@Override
	final public AcceptabilityState isAcceptable() {
		if (_username.length() > 0)
			return AcceptabilityState
					.accept(UsernamePasswordInformationModel.class);

		return AcceptabilityState.deny(UsernamePasswordInformationModel.class,
				"Username cannot be empty");
	}

	@Override
	final public FSViewAuthenticationInformation wrap() {
		return new UsernamePasswordAuthenticationInformation(_username,
				_password);
	}

	@Override
	final public Component createGuiComponent() {
		return new UsernamePasswordPanel(this);
	}
}