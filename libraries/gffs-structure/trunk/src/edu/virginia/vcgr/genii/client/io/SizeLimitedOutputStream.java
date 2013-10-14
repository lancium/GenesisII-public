package edu.virginia.vcgr.genii.client.io;

import java.io.IOException;
import java.io.OutputStream;

public class SizeLimitedOutputStream extends OutputStream
{
	private byte[] _storage;
	private int _next;

	public SizeLimitedOutputStream(int size)
	{
		_storage = new byte[size];
		_next = 0;
	}

	@Override
	public void write(int b) throws IOException
	{
		byte[] data = new byte[1];
		data[0] = (byte) b;
		write(data);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		int toWrite = _storage.length - _next;
		toWrite = Math.min(len, toWrite);
		System.arraycopy(b, off, _storage, _next, toWrite);
		_next += toWrite;
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		write(b, 0, b.length);
	}

	final public byte[] toArray()
	{
		byte[] ret = new byte[_next];
		System.arraycopy(_storage, 0, ret, 0, _next);
		return ret;
	}

	final public boolean isFull()
	{
		return _next >= _storage.length;
	}
}