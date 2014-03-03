package edu.virginia.g3.fsview.cifs;

import java.net.URI;

import edu.virginia.g3.fsview.gui.FSViewInformationManager;
import edu.virginia.g3.fsview.gui.FSViewInformationModel;

final class CifsViewInformationManager implements FSViewInformationManager<URI>
{
	@Override
	final public FSViewInformationModel<URI> createModel()
	{
		return new CifsFSViewInformationModel();
	}
}