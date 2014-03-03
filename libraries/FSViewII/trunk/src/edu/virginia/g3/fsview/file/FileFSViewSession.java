package edu.virginia.g3.fsview.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import edu.virginia.g3.fsview.AbstractFSViewSession;
import edu.virginia.g3.fsview.FSViewEntry;
import edu.virginia.g3.fsview.FSViewFactory;

final class FileFSViewSession extends AbstractFSViewSession {
	private File _root;

	FileFSViewSession(URI fsRoot, FSViewFactory factory, boolean readOnly)
			throws IOException {
		super(factory, readOnly);

		_root = new File(fsRoot);
		if (!_root.exists())
			throw new FileNotFoundException(String.format(
					"File system path %s does not exist!", _root));
	}

	final FSViewEntry wrapFile(File target) throws IOException {
		if (!target.exists())
			throw new FileNotFoundException(String.format(
					"Target %s not found!", target));

		if (target.isDirectory())
			return new FileFSViewDirectoryEntry(this, null, target.getName(),
					target);
		else
			return new FileFSViewRandomAccessFileEntry(this, null,
					target.getName(), target);
	}

	@Override
	final public FSViewEntry root() throws IOException {
		return wrapFile(_root);
	}
}