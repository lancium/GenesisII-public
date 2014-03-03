package edu.virginia.g3.fsview;

import java.io.IOException;
import java.net.URI;

import edu.virginia.g3.fsview.gui.FSViewInformationModel;

public interface FSViewFactory {
	public String[] supportedURISchemes();

	public FSViewAuthenticationInformationTypes[] supportedAuthenticationTypes();

	public FSViewSession openSession(URI fsRoot,
			FSViewAuthenticationInformation authInfo, boolean readOnly)
			throws IOException;

	public FSViewInformationModel<URI> createModel();
}