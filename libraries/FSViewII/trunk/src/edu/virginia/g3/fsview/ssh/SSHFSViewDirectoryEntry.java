package edu.virginia.g3.fsview.ssh;

import java.io.IOException;
import java.util.Calendar;

import com.jcraft.jsch.SftpATTRS;

import edu.virginia.g3.fsview.AbstractFSViewDirectoryEntry;
import edu.virginia.g3.fsview.FSViewDirectoryEntry;
import edu.virginia.g3.fsview.FSViewEntry;
import edu.virginia.g3.fsview.FSViewFileEntry;

class SSHFSViewDirectoryEntry extends
		AbstractFSViewDirectoryEntry<SSHFSViewSession> {
	private SftpATTRS _attrs;
	private String _sshPath;

	@Override
	final protected FSViewFileEntry createFileImpl(String name)
			throws IOException {
		return typedSession().createFile(this, name, _sshPath + "/" + name);
	}

	@Override
	final protected FSViewDirectoryEntry createDirectoryImpl(String name)
			throws IOException {
		return typedSession().mkdir(this, name, _sshPath + "/" + name);
	}

	@Override
	final protected void deleteImpl(String name) throws IOException {
		typedSession().delete(_sshPath + "/" + name);
	}

	@Override
	final protected boolean canWriteImpl() {
		return (_attrs.getPermissions() & 0222) > 0;
	}

	SSHFSViewDirectoryEntry(SSHFSViewSession session,
			FSViewDirectoryEntry parentEntry, String entryName, String sshPath,
			SftpATTRS attrs) {
		super(SSHFSViewSession.class, session, parentEntry, entryName);

		_sshPath = sshPath;
		_attrs = attrs;
	}

	@Override
	final public FSViewEntry[] listEntries() throws IOException {
		return typedSession().listEntries(this, _sshPath);
	}

	@Override
	final public boolean canRead() {
		return (_attrs.getPermissions() & 0444) > 0;
	}

	@Override
	final public FSViewEntry lookup(String name) throws IOException {
		return typedSession().lookup(this, name, _sshPath + "/" + name);
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