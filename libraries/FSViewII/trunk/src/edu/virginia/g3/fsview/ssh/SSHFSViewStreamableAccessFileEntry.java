package edu.virginia.g3.fsview.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import com.jcraft.jsch.SftpATTRS;

import edu.virginia.g3.fsview.AbstractFSViewStreamableFileEntry;
import edu.virginia.g3.fsview.FSViewDirectoryEntry;

final class SSHFSViewStreamableAccessFileEntry extends
		AbstractFSViewStreamableFileEntry<SSHFSViewSession> {
	private SftpATTRS _attrs;
	private String _sshPath;

	@Override
	final protected OutputStream openOutpuStreamImpl() throws IOException {
		return typedSession().openOutputStream(_sshPath);
	}

	@Override
	final protected boolean canWriteImpl() {
		return (_attrs.getPermissions() & 0222) > 0;
	}

	SSHFSViewStreamableAccessFileEntry(SSHFSViewSession session,
			FSViewDirectoryEntry parentEntry, String entryName, String sshPath,
			SftpATTRS attrs) {
		super(SSHFSViewSession.class, session, parentEntry, entryName);

		_sshPath = sshPath;
		_attrs = attrs;
	}

	@Override
	final public InputStream openInputStream() throws IOException {
		return typedSession().openInputStream(_sshPath);
	}

	@Override
	final public boolean canRead() {
		return (_attrs.getPermissions() & 0444) > 0;
	}

	@Override
	final public Long size() {
		return _attrs.getSize();
	}

	@Override
	final public Calendar lastAccessed() {
		long last = _attrs.getATime() * 1000L;
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(last);
		return ret;
	}

	@Override
	final public Calendar lastModified() {
		long last = _attrs.getMTime() * 1000L;
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(last);
		return ret;
	}
}