package edu.virginia.g3.fsview;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

public abstract class AbstractFSViewSession implements FSViewSession {
	private Object _closeLock = new Object();

	private boolean _isOpen = true;
	private Calendar _openTime;
	private FSViewFactory _factory;

	private boolean _readOnly;

	protected void closeImpl() throws IOException {
		// Nothing to do by default.
	}

	protected AbstractFSViewSession(FSViewFactory factory, boolean readOnly) {
		_factory = factory;
		_openTime = Calendar.getInstance();

		_readOnly = readOnly;
	}

	@Override
	final protected void finalize() throws Throwable {
		close();
	}

	@Override
	final public FSViewFactory factory() {
		return _factory;
	}

	@Override
	final public void close() throws IOException {
		synchronized (_closeLock) {
			if (_isOpen) {
				closeImpl();
				_isOpen = false;
			}
		}
	}

	@Override
	final public FSViewEntry lookup(String path) throws IOException {
		FSViewEntry current = root();
		for (String entity : path.split("/")) {

			if (current.entryType() != FSViewEntryType.Directory)
				throw new FileNotFoundException(String.format(
						"Unable to lookup path %s!", path));

			if (entity == null || entity.length() == 0 || entity.equals("."))
				continue;

			if (entity.equals("..")) {
				FSViewEntry next = current.parent();
				if (next != null)
					current = next;
			} else {
				current = ((FSViewDirectoryEntry) current).lookup(entity);
				if (current == null)
					throw new FileNotFoundException(String.format(
							"Unable to lookup path %s!", path));
			}
		}

		return current;
	}

	@Override
	final public boolean isOpen() {
		synchronized (_closeLock) {
			return _isOpen;
		}
	}

	@Override
	final public String toString() {
		return String.format("[%1$tT %1$tD] %2$s Session", _openTime, _factory);
	}

	@Override
	final public boolean isReadOnly() {
		return _readOnly;
	}
}