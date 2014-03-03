package edu.virginia.g3.fsview.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import edu.virginia.g3.fsview.AbstractFSViewSession;
import edu.virginia.g3.fsview.FSViewEntry;
import edu.virginia.g3.fsview.FSViewFactory;
import edu.virginia.g3.fsview.UsernamePasswordAuthenticationInformation;

final class SSHFSViewSession extends AbstractFSViewSession {
	static final private int DEFAULT_PORT = 22;

	private JSch _shell;
	private Session _session;
	private ChannelSftp _ftpChannel;

	private FSViewEntry _rootEntry;

	@Override
	final protected void closeImpl() throws IOException {
		try {
			if (_ftpChannel != null)
				_ftpChannel.disconnect();
		} catch (Throwable cause) {
			throw new IOException("Unable to disconnect SFTP Session.", cause);
		}

		try {
			if (_session != null)
				_session.disconnect();
		} catch (Throwable cause) {
			throw new IOException("Unable to disconnect session.", cause);
		}
	}

	SSHFSViewSession(FSViewFactory factory, URI fsRoot,
			UsernamePasswordAuthenticationInformation authInfo, boolean readOnly)
			throws IOException {
		super(factory, readOnly);

		int port = fsRoot.getPort();
		if (port < 0)
			port = DEFAULT_PORT;

		_shell = new JSch();
		_shell.setHostKeyRepository(new AcceptAllHostKeyRepository());

		try {
			_session = _shell.getSession(authInfo.username(), fsRoot.getHost(),
					port);
			_session.setPassword(authInfo.password());
			_session.connect();

			_ftpChannel = (ChannelSftp) _session.openChannel("sftp");
			_ftpChannel.connect();

			String rootPath = fsRoot.getPath();
			SftpATTRS rootStat = _ftpChannel.stat(rootPath);
			if (rootStat.isDir() || rootStat.isLink())
				_rootEntry = new SSHFSViewDirectoryEntry(this, null, null,
						rootPath, rootStat);
			else
				_rootEntry = new SSHFSViewStreamableAccessFileEntry(this, null,
						null, rootPath, rootStat);
		} catch (JSchException e) {
			throw new IOException("Unable to connect ssh session.", e);
		} catch (SftpException e) {
			throw new IOException("Unable to issue SFTP command.", e);
		}
	}

	final SSHFSViewDirectoryEntry mkdir(SSHFSViewDirectoryEntry parentEntry,
			String entryName, String path) throws IOException {
		try {
			_ftpChannel.mkdir(path);
			return new SSHFSViewDirectoryEntry(this, parentEntry, entryName,
					path, _ftpChannel.stat(path));
		} catch (SftpException e) {
			throw new IOException(String.format("Unable to make directory %s!",
					path), e);
		}
	}

	final SSHFSViewStreamableAccessFileEntry createFile(
			SSHFSViewDirectoryEntry parentEntry, String entryName, String path)
			throws IOException {
		try {
			_ftpChannel.put(path).close();
			return new SSHFSViewStreamableAccessFileEntry(this, parentEntry,
					entryName, path, _ftpChannel.stat(path));
		} catch (SftpException e) {
			throw new IOException(String.format("Unable to create file %s!",
					path), e);
		}
	}

	final void delete(String path) throws IOException {
		try {
			SftpATTRS attrs = _ftpChannel.stat(path);
			if (attrs.isDir())
				_ftpChannel.rmdir(path);
			else
				_ftpChannel.rm(path);
		} catch (SftpException e) {
			throw new IOException(String.format("Unable to delete %s!", path),
					e);
		}
	}

	@SuppressWarnings("unchecked")
	final FSViewEntry[] listEntries(SSHFSViewDirectoryEntry parentEntry,
			String path) throws IOException {
		try {
			Vector<ChannelSftp.LsEntry> entries = _ftpChannel.ls(path);
			ArrayList<FSViewEntry> ret = new ArrayList<FSViewEntry>(
					entries.size());
			for (ChannelSftp.LsEntry entry : entries) {
				SftpATTRS attrs = entry.getAttrs();
				String name = entry.getFilename();

				if (name.equals(".") || name.equals(".."))
					continue;

				String entryPath = path + "/" + name;

				if (attrs.isDir() || attrs.isLink())
					ret.add(new SSHFSViewDirectoryEntry(this, parentEntry,
							name, entryPath, attrs));
				else
					ret.add(new SSHFSViewStreamableAccessFileEntry(this,
							parentEntry, name, entryPath, attrs));
			}

			return ret.toArray(new FSViewEntry[ret.size()]);
		} catch (SftpException e) {
			throw new IOException(String.format(
					"Unable to list contents of %s!", path), e);
		}
	}

	final FSViewEntry lookup(SSHFSViewDirectoryEntry parentEntry, String name,
			String path) throws IOException {
		try {
			SftpATTRS attrs = _ftpChannel.stat(path);
			if (attrs.isDir() || attrs.isLink())
				return new SSHFSViewDirectoryEntry(this, parentEntry, name,
						path, attrs);
			else
				return new SSHFSViewStreamableAccessFileEntry(this,
						parentEntry, name, path, attrs);
		} catch (SftpException e) {
			throw new IOException(String.format("Unable to lookup entry %s!",
					path), e);
		}
	}

	final OutputStream openOutputStream(String path) throws IOException {
		try {
			return _ftpChannel.put(path);
		} catch (SftpException e) {
			throw new IOException(String.format(
					"Unable to open %s for output!", path), e);
		}
	}

	final InputStream openInputStream(String path) throws IOException {
		try {
			return _ftpChannel.get(path);
		} catch (SftpException e) {
			throw new IOException(String.format("Unable to open %s for input!",
					path), e);
		}
	}

	@Override
	final public FSViewEntry root() throws IOException {
		return _rootEntry;
	}
}