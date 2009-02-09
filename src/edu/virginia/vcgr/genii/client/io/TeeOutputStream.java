package edu.virginia.vcgr.genii.client.io;

import java.io.IOException;
import java.io.OutputStream;

public class TeeOutputStream extends OutputStream
{
	private OutputStream []_streams;
	
	public TeeOutputStream(OutputStream...streams)
	{
		_streams = streams;
	}
	
	@Override
	public void write(int b) throws IOException
	{
		for (OutputStream stream : _streams)
		{
			stream.write(b);
		}
	}

	@Override
	public void close() throws IOException
	{
		for (OutputStream stream : _streams)
		{
			stream.close();
		}
	}

	@Override
	public void flush() throws IOException
	{
		for (OutputStream stream : _streams)
		{
			stream.flush();
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		for (OutputStream stream : _streams)
		{
			stream.write(b, off, len);
		}
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		for (OutputStream stream : _streams)
		{
			stream.write(b);
		}
	}
}