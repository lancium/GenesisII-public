package edu.virginia.g3.fsview.authgui.anon;

import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.gui.FSViewInformationManager;
import edu.virginia.g3.fsview.gui.FSViewInformationModel;

final public class AnonymousAuthenticationInformationManager implements
	FSViewInformationManager<FSViewAuthenticationInformation>
{
	@Override
	final public FSViewInformationModel<FSViewAuthenticationInformation> createModel()
	{
		return new AnonymousAuthenticationInformationModel();
	}
}