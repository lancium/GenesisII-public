package edu.virginia.g3.fsview;

import edu.virginia.g3.fsview.authgui.anon.AnonymousAuthenticationInformationManager;
import edu.virginia.g3.fsview.authgui.domuserpass.DomainUsernamePasswordManager;
import edu.virginia.g3.fsview.authgui.userpass.UsernamePasswordInformationManager;
import edu.virginia.g3.fsview.gui.FSViewInformationManager;
import edu.virginia.g3.fsview.gui.FSViewInformationModel;

public enum FSViewAuthenticationInformationTypes {
	Anonymous("Anonymous", new AnonymousAuthenticationInformationManager()), DomainUsernamePassword(
			"Domain/Username/Password", new DomainUsernamePasswordManager()), UsernamePassword(
			"Username/Password", new UsernamePasswordInformationManager());

	private String _title;
	private FSViewInformationManager<FSViewAuthenticationInformation> _informationManager;

	private FSViewAuthenticationInformationTypes(
			String title,
			FSViewInformationManager<FSViewAuthenticationInformation> informationManager) {
		_title = title;
		_informationManager = informationManager;
	}

	final public FSViewInformationModel<FSViewAuthenticationInformation> createModel() {
		return _informationManager.createModel();
	}

	@Override
	final public String toString() {
		return _title;
	}
}