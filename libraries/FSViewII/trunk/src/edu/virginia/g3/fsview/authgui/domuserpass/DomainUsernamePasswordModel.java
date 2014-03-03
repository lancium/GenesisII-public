package edu.virginia.g3.fsview.authgui.domuserpass;

import java.awt.Component;

import edu.virginia.g3.fsview.DomainUsernamePassswordAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformationTypes;
import edu.virginia.g3.fsview.authgui.AbstractAuthenticationInformationModel;
import edu.virginia.g3.fsview.gui.AcceptabilityState;

class DomainUsernamePasswordModel extends AbstractAuthenticationInformationModel
{
	private String _domain;
	private String _username;
	private String _password;

	DomainUsernamePasswordModel()
	{
		super(FSViewAuthenticationInformationTypes.DomainUsernamePassword);

		domain("");
		username("");
		password("");
	}

	final void domain(String domain)
	{
		if (domain == null)
			domain = "";

		_domain = domain;
	}

	final void username(String username)
	{
		if (username == null)
			username = "";

		_username = username;

		fireContentsChanged();
	}

	final void password(String password)
	{
		if (password == null)
			password = "";

		_password = password;

		fireContentsChanged();
	}

	@Override
	final public AcceptabilityState isAcceptable()
	{
		if (_username.length() > 0)
			return AcceptabilityState.accept(DomainUsernamePasswordModel.class);

		return AcceptabilityState.deny(DomainUsernamePasswordModel.class, "Username cannot be empty");
	}

	@Override
	final public FSViewAuthenticationInformation wrap()
	{
		return new DomainUsernamePassswordAuthenticationInformation(_domain, _username, _password);
	}

	@Override
	final public Component createGuiComponent()
	{
		return new DomainUsernamePasswordPanel(this);
	}
}