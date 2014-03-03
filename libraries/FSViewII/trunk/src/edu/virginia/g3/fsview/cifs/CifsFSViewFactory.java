package edu.virginia.g3.fsview.cifs;

import java.io.IOException;
import java.net.URI;

import jcifs.smb.NtlmPasswordAuthentication;

import edu.virginia.g3.fsview.AbstractFSViewFactory;
import edu.virginia.g3.fsview.DomainUsernamePassswordAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformationTypes;
import edu.virginia.g3.fsview.FSViewSession;
import edu.virginia.g3.fsview.UsernamePasswordAuthenticationInformation;

final public class CifsFSViewFactory extends AbstractFSViewFactory
{
	static final private String[] SUPPORTED_URI_SCHEMES = { "smb" };
	static final private String DESCRIPTION = "Samba File System";

	public CifsFSViewFactory()
	{
		super(new CifsViewInformationManager(), SUPPORTED_URI_SCHEMES, DESCRIPTION,
			FSViewAuthenticationInformationTypes.UsernamePassword, FSViewAuthenticationInformationTypes.DomainUsernamePassword);
	}

	@Override
	final public FSViewSession openSession(URI fsRoot, FSViewAuthenticationInformation authInfo, boolean readOnly)
		throws IOException
	{
		DomainUsernamePassswordAuthenticationInformation dup;

		switch (authInfo.authenticationType()) {
			case UsernamePassword:
				authInfo =
					new DomainUsernamePassswordAuthenticationInformation(
						((UsernamePasswordAuthenticationInformation) authInfo).username(),
						((UsernamePasswordAuthenticationInformation) authInfo).password());

			case DomainUsernamePassword:
				dup = (DomainUsernamePassswordAuthenticationInformation) authInfo;
				break;

			default:
				throw new IllegalArgumentException("Authentication information must be either "
					+ "UsernamePassword or DomainUsernamePassword.");
		}

		NtlmPasswordAuthentication smbAuthInfo = new NtlmPasswordAuthentication(dup.domain(), dup.username(), dup.password());
		return new CifsFSViewSession(this, fsRoot, smbAuthInfo, readOnly);
	}
}