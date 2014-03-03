package edu.virginia.g3.fsview.ssh;

import java.awt.Component;
import java.net.URI;

import edu.virginia.g3.fsview.gui.AbstractFSViewInformationModel;
import edu.virginia.g3.fsview.gui.AcceptabilityState;

final class SSHFSViewInformationModel extends AbstractFSViewInformationModel<URI>
{
	private String _hostname;
	private int _port;
	private String _path;

	SSHFSViewInformationModel()
	{
		super("SSH Access");

		hostname(null);
		port(22);
		path(null);
	}

	final void hostname(String hostname)
	{
		if (hostname == null)
			hostname = "";

		_hostname = hostname;

		fireContentsChanged();
	}

	final void port(int port)
	{
		_port = port;

		fireContentsChanged();
	}

	final void path(String name)
	{
		if (name == null)
			name = "";

		_path = name;

		fireContentsChanged();
	}

	@Override
	final public AcceptabilityState isAcceptable()
	{
		if (_hostname.length() == 0)
			return AcceptabilityState.deny(SSHFSViewInformationModel.class, "Hostname cannot be empty");

		if (_path.length() == 0)
			return AcceptabilityState.deny(SSHFSViewInformationModel.class, "Path cannot be empty");

		return AcceptabilityState.accept(SSHFSViewInformationModel.class);
	}

	@Override
	final public URI wrap()
	{
		return URI.create(String.format("ssh://%s:%d/%s", _hostname, _port, _path));
	}

	@Override
	final public Component createGuiComponent()
	{
		return new SSHFSViewInformationPanel(this);
	}
}