package edu.virginia.g3.fsview.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.virginia.g3.fsview.AbstractFSViewStreamableFileEntry;

class HTTPFSViewStreamableAccessFileEntry extends
		AbstractFSViewStreamableFileEntry<HTTPFSViewSession> {
	@Override
	final protected OutputStream openOutpuStreamImpl() throws IOException {
		throw new IOException(
				"Not allowed to open output streams to HTTP(S) connections!");
	}

	@Override
	final protected boolean canWriteImpl() {
		return false;
	}

	HTTPFSViewStreamableAccessFileEntry(HTTPFSViewSession session) {
		super(HTTPFSViewSession.class, session, null, null);
	}

	@Override
	final public InputStream openInputStream() throws IOException {
		return typedSession().openInputStream();
	}

	@Override
	final public boolean canRead() {
		return true;
	}
}