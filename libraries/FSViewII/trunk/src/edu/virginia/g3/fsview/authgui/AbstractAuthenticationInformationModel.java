package edu.virginia.g3.fsview.authgui;

import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformationTypes;
import edu.virginia.g3.fsview.gui.AbstractFSViewInformationModel;

public abstract class AbstractAuthenticationInformationModel extends
		AbstractFSViewInformationModel<FSViewAuthenticationInformation>
		implements AuthenticationInformationModel {
	private FSViewAuthenticationInformationTypes _authenticationType;

	protected AbstractAuthenticationInformationModel(
			FSViewAuthenticationInformationTypes authenticationType) {
		super(authenticationType.toString());

		_authenticationType = authenticationType;
	}

	@Override
	final public FSViewAuthenticationInformationTypes authenticationType() {
		return _authenticationType;
	}
}