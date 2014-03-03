package edu.virginia.vcgr.genii.gjt.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class OverridenIOSource implements IOSource
{
	private IOSource[] _sources;

	public OverridenIOSource(IOSource... sources)
	{
		_sources = sources;
	}

	@Override
	public InputStream open() throws IOException
	{
		for (IOSource source : _sources) {
			try {
				return source.open();
			} catch (FileNotFoundException fnfe) {
				// For now, just let it pass.
			}
		}

		throw new FileNotFoundException("Unable to open multiple IO sources.");
	}
}