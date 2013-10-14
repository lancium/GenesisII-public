package edu.virginia.vcgr.genii.client.utils.io;

import java.io.IOException;
import java.io.Writer;

final public class EmptyWriter extends Writer
{
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		// Nothing to do
	}

	@Override
	public void flush() throws IOException
	{
		// Nothing to do
	}

	@Override
	public void close() throws IOException
	{
		// Nothing to do
	}
}