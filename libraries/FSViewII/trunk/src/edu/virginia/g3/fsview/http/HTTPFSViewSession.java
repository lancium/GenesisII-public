package edu.virginia.g3.fsview.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import edu.virginia.g3.fsview.AbstractFSViewSession;
import edu.virginia.g3.fsview.FSViewEntry;

class HTTPFSViewSession extends AbstractFSViewSession {
	private URL _url;

	HTTPFSViewSession(HTTPFSViewFactory factory, URL url) throws IOException {
		super(factory, true);
		_url = url;
	}

	final InputStream openInputStream() throws IOException {
		return _url.openStream();
	}

	@Override
	final public FSViewEntry root() throws IOException {
		return new HTTPFSViewStreamableAccessFileEntry(this);
	}
}