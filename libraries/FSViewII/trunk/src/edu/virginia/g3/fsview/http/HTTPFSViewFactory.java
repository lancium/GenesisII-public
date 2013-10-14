package edu.virginia.g3.fsview.http;

import java.io.IOException;
import java.net.URI;

import edu.virginia.g3.fsview.AbstractFSViewFactory;
import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformationTypes;
import edu.virginia.g3.fsview.FSViewSession;

final public class HTTPFSViewFactory extends AbstractFSViewFactory
{
	static final private String[] SUPPORTED_URI_SCHEMES = { "http", "https" };
	static final private String DESCRIPTION = "HTTP(S) File System";

	public HTTPFSViewFactory()
	{
		super(new HTTPFSViewInformationManager(), SUPPORTED_URI_SCHEMES, DESCRIPTION,
			FSViewAuthenticationInformationTypes.Anonymous);
	}

	@Override
	final public FSViewSession openSession(URI fsRoot, FSViewAuthenticationInformation authInfo, boolean readOnly)
		throws IOException
	{
		return new HTTPFSViewSession(this, fsRoot.toURL());
	}
}