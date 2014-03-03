package edu.virginia.g3.fsview.authgui.userpass;

import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.gui.FSViewInformationManager;
import edu.virginia.g3.fsview.gui.FSViewInformationModel;

final public class UsernamePasswordInformationManager implements FSViewInformationManager<FSViewAuthenticationInformation>
{
	@Override
	final public FSViewInformationModel<FSViewAuthenticationInformation> createModel()
	{
		return new UsernamePasswordInformationModel();
	}
}