package edu.virginia.vcgr.genii.container.exportdir.lightweight.svn;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class PartialBlockOutputStream extends OutputStream
{
	private long _bytesToSkip;
	private ByteBuffer _buffer;

	PartialBlockOutputStream(ByteBuffer buffer, long bytesToSkip)
	{
		_buffer = buffer;
		_bytesToSkip = bytesToSkip;
	}

	@Override
	public void write(int b) throws IOException
	{
		byte[] data = new byte[] { (byte) b };
		write(data);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		if (_buffer.remaining() <= 0)
			return;
		if (_bytesToSkip > 0) {
			if (len > _bytesToSkip) {
				len -= _bytesToSkip;
				off += _bytesToSkip;
				_bytesToSkip = 0;
			} else {
				_bytesToSkip -= len;
				len = 0;
			}
		}

		if (len > 0) {
			len = Math.min(len, _buffer.remaining());
			_buffer.put(b, off, len);
		}
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		write(b, 0, b.length);
	}
}