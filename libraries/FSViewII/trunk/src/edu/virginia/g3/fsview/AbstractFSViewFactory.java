package edu.virginia.g3.fsview;

import java.net.URI;

import edu.virginia.g3.fsview.gui.FSViewInformationManager;
import edu.virginia.g3.fsview.gui.FSViewInformationModel;

public abstract class AbstractFSViewFactory implements FSViewFactory {
	private String[] _supportedURISchemes;
	private String _description;
	private FSViewAuthenticationInformationTypes[] _supportedAuthenticationTypes;
	private FSViewInformationManager<URI> _informationManager;

	protected AbstractFSViewFactory(
			FSViewInformationManager<URI> informationManager,
			String[] supportedURISchemes, String description,
			FSViewAuthenticationInformationTypes authType,
			FSViewAuthenticationInformationTypes... additionalAuthTypes) {
		if (informationManager == null)
			throw new IllegalArgumentException(
					"Information manager cannot be null.");

		if (supportedURISchemes == null || supportedURISchemes.length == 0)
			throw new IllegalArgumentException(
					"Must have at least 1 supported URI scheme.");

		if (description == null)
			throw new IllegalArgumentException("Description cannot be null.");

		if (authType == null)
			throw new IllegalArgumentException(
					"Must have at least 1 supported authentication type.");

		_informationManager = informationManager;
		_supportedURISchemes = supportedURISchemes;
		_description = description;
		_supportedAuthenticationTypes = new FSViewAuthenticationInformationTypes[additionalAuthTypes.length + 1];
		_supportedAuthenticationTypes[0] = authType;
		System.arraycopy(additionalAuthTypes, 0, _supportedAuthenticationTypes,
				1, additionalAuthTypes.length);
	}

	@Override
	final public String[] supportedURISchemes() {
		return _supportedURISchemes;
	}

	@Override
	final public FSViewAuthenticationInformationTypes[] supportedAuthenticationTypes() {
		return _supportedAuthenticationTypes;
	}

	@Override
	final public FSViewInformationModel<URI> createModel() {
		return _informationManager.createModel();
	}

	@Override
	final public String toString() {
		return _description;
	}
}