package edu.virginia.vcgr.genii.client.utils.io;

import java.io.IOException;
import java.io.Reader;

final public class EmptyReader extends Reader
{
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		return -1;
	}

	@Override
	public void close() throws IOException
	{
		// Nothing to do
	}
}