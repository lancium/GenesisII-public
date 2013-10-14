package org.morgan.dpage;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

class ScratchDirectory extends File implements Closeable
{
	private static final long serialVersionUID = 1L;

	static private void recursiveDelete(File directory)
	{
		if (directory.isDirectory())
			for (File entry : directory.listFiles())
				recursiveDelete(entry);

		directory.delete();
	}

	@Override
	protected void finalize() throws Throwable
	{
		close();
	}

	ScratchDirectory(File parent, String child)
	{
		super(parent, child);
	}

	@Override
	synchronized public void close() throws IOException
	{
		if (exists())
			recursiveDelete(this);
	}
}