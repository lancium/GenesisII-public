package edu.virginia.g3.fsview.authgui;

import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformationTypes;
import edu.virginia.g3.fsview.gui.FSViewInformationModel;

public interface AuthenticationInformationModel extends FSViewInformationModel<FSViewAuthenticationInformation>
{
	public FSViewAuthenticationInformationTypes authenticationType();
}
