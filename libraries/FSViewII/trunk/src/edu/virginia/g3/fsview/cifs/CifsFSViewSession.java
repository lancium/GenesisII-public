package edu.virginia.g3.fsview.cifs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

import edu.virginia.g3.fsview.AbstractFSViewSession;
import edu.virginia.g3.fsview.FSViewDirectoryEntry;
import edu.virginia.g3.fsview.FSViewEntry;
import edu.virginia.g3.fsview.FSViewFactory;

final class CifsFSViewSession extends AbstractFSViewSession {
	private SmbFile _root;

	CifsFSViewSession(FSViewFactory factory, URI fsRoot,
			NtlmPasswordAuthentication authInfo, boolean readOnly)
			throws IOException {
		super(factory, readOnly);

		String fsRootString = fsRoot.toString();
		if (!fsRootString.endsWith("/"))
			fsRootString = fsRootString + "/";
		_root = new SmbFile(fsRootString, authInfo);

		if (!_root.exists())
			throw new FileNotFoundException(String.format(
					"Can't find Samba file %s!", fsRoot));
	}

	final FSViewEntry wrapSmbFile(FSViewDirectoryEntry parentEntry,
			String entryName, SmbFile sambaFile) throws IOException {
		if (entryName != null) {
			while (entryName.endsWith("/"))
				entryName = entryName.substring(0, entryName.length() - 1);
			if (entryName.length() == 0)
				entryName = "/";
		}

		if (sambaFile.isDirectory()) {
			return new CifsFSViewDirectoryEntry(this, parentEntry, entryName,
					sambaFile);
		} else {
			return new CifsFSViewRandomAccessFileEntry(this, parentEntry,
					entryName, sambaFile);
		}
	}

	@Override
	final public FSViewEntry root() throws IOException {
		return wrapSmbFile(null, null, _root);
	}
}