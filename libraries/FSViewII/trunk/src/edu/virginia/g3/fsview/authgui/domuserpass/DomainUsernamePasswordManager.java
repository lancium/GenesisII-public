package edu.virginia.g3.fsview.authgui.domuserpass;

import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.gui.FSViewInformationManager;
import edu.virginia.g3.fsview.gui.FSViewInformationModel;

final public class DomainUsernamePasswordManager implements FSViewInformationManager<FSViewAuthenticationInformation>
{
	@Override
	final public FSViewInformationModel<FSViewAuthenticationInformation> createModel()
	{
		return new DomainUsernamePasswordModel();
	}
}