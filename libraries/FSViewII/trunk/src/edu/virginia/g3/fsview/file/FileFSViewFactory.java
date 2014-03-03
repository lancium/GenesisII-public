package edu.virginia.g3.fsview.file;

import java.io.IOException;
import java.net.URI;

import edu.virginia.g3.fsview.AbstractFSViewFactory;
import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformationTypes;
import edu.virginia.g3.fsview.FSViewSession;

final public class FileFSViewFactory extends AbstractFSViewFactory
{
	static final private String[] SUPPORTED_SCHEMES = { "file" };
	static final private String DESCRIPTION = "Local File System";

	public FileFSViewFactory()
	{
		super(new FileFSViewInformationManager(), SUPPORTED_SCHEMES, DESCRIPTION,
			FSViewAuthenticationInformationTypes.Anonymous);
	}

	@Override
	final public FSViewSession openSession(URI fsRoot, FSViewAuthenticationInformation authInfo, boolean readOnly)
		throws IOException
	{
		return new FileFSViewSession(fsRoot, this, readOnly);
	}
}