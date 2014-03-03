package edu.virginia.g3.fsview.ssh;

import java.io.IOException;
import java.net.URI;

import edu.virginia.g3.fsview.AbstractFSViewFactory;
import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformationTypes;
import edu.virginia.g3.fsview.FSViewSession;
import edu.virginia.g3.fsview.UsernamePasswordAuthenticationInformation;

final public class SSHFSViewFactory extends AbstractFSViewFactory {
	static final private String[] SUPPORTED_URI_SCHEMES = { "ssh", "scp",
			"sftp" };
	static final private String DESCRIPTION = "SSH/SCP/SFTP File System";

	public SSHFSViewFactory() {
		super(new SSHFSViewInformationManager(), SUPPORTED_URI_SCHEMES,
				DESCRIPTION,
				FSViewAuthenticationInformationTypes.UsernamePassword);
	}

	@Override
	final public FSViewSession openSession(URI fsRoot,
			FSViewAuthenticationInformation authInfo, boolean readOnly)
			throws IOException {
		return new SSHFSViewSession(this, fsRoot,
				(UsernamePasswordAuthenticationInformation) authInfo, readOnly);
	}
}