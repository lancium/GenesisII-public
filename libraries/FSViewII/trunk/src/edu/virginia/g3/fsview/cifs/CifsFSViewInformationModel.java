package edu.virginia.g3.fsview.cifs;

import java.awt.Component;
import java.net.URI;

import edu.virginia.g3.fsview.gui.AbstractFSViewInformationModel;
import edu.virginia.g3.fsview.gui.AcceptabilityState;

final class CifsFSViewInformationModel extends AbstractFSViewInformationModel<URI>
{
	private String _hostname;
	private String _shareName;

	CifsFSViewInformationModel()
	{
		super("Windows Share");

		hostname(null);
		shareName(null);
	}

	final void hostname(String hostname)
	{
		if (hostname == null)
			hostname = "";

		_hostname = hostname;

		fireContentsChanged();
	}

	final void shareName(String name)
	{
		if (name == null)
			name = "";

		_shareName = name;

		fireContentsChanged();
	}

	@Override
	final public AcceptabilityState isAcceptable()
	{
		if (_hostname.length() == 0)
			return AcceptabilityState.deny(CifsFSViewInformationModel.class, "Hostname cannot be empty");

		if (_shareName.length() == 0)
			return AcceptabilityState.deny(CifsFSViewInformationModel.class, "Share name cannot be empty");

		return AcceptabilityState.accept(CifsFSViewInformationModel.class);
	}

	@Override
	final public URI wrap()
	{
		return URI.create(String.format("smb://%s/%s", _hostname, _shareName));
	}

	@Override
	final public Component createGuiComponent()
	{
		return new CifsFSViewInformationPanel(this);
	}
}