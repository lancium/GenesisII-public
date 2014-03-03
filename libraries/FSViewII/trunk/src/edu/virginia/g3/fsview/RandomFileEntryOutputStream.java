package edu.virginia.g3.fsview;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

final class RandomFileEntryOutputStream extends OutputStream {
	private FSViewRandomAccessFileEntry _entry;
	private long _offset;

	RandomFileEntryOutputStream(FSViewRandomAccessFileEntry entry) {
		_entry = entry;
		_offset = 0L;
	}

	@Override
	final public void write(int b) throws IOException {
		write(new byte[] { (byte) b });
	}

	@Override
	final public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	final public void write(byte[] b, int off, int len) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(b, off, len);
		_entry.write(_offset, buffer);
		_offset += len;
	}
}