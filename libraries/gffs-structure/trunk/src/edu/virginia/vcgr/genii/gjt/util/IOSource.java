package edu.virginia.vcgr.genii.gjt.util;

import java.io.IOException;
import java.io.InputStream;

public interface IOSource
{
	public InputStream open() throws IOException;
}