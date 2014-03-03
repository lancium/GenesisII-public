package edu.virginia.g3.fsview;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

final class RandomFileEntryInputStream extends InputStream {
	private FSViewRandomAccessFileEntry _entry;
	private long _offset;

	private Long _mark = null;

	private ByteBuffer read(int size) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(size);
		_entry.read(_offset, buffer);
		buffer.flip();
		_offset += buffer.remaining();
		return buffer;
	}

	RandomFileEntryInputStream(FSViewRandomAccessFileEntry entry) {
		_entry = entry;
		_offset = 0L;
	}

	@Override
	final public int read() throws IOException {
		ByteBuffer buffer = read(1);
		if (buffer.hasRemaining())
			return buffer.get();

		return -1;
	}

	@Override
	final public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	final public int read(byte[] b, int off, int len) throws IOException {
		ByteBuffer buffer = read(len);
		int read = buffer.remaining();
		buffer.get(b, off, read);
		return read;
	}

	@Override
	final public long skip(long n) throws IOException {
		_offset += n;
		return _offset;
	}

	@Override
	final public synchronized void mark(int readlimit) {
		_mark = _offset;
	}

	@Override
	final public synchronized void reset() throws IOException {
		_offset = _mark;
	}

	@Override
	final public boolean markSupported() {
		return true;
	}
}