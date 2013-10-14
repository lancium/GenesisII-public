package edu.virginia.g3.fsview.ssh;

import java.net.URI;

import edu.virginia.g3.fsview.gui.FSViewInformationManager;
import edu.virginia.g3.fsview.gui.FSViewInformationModel;

final class SSHFSViewInformationManager implements FSViewInformationManager<URI>
{
	@Override
	final public FSViewInformationModel<URI> createModel()
	{
		return new SSHFSViewInformationModel();
	}
}