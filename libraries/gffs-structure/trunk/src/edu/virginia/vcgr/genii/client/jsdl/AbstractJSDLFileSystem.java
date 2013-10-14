package edu.virginia.vcgr.genii.client.jsdl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public abstract class AbstractJSDLFileSystem implements JSDLFileSystem, Serializable
{
	static final long serialVersionUID = 0L;

	protected abstract File relativeToImpl(String relativePath) throws IOException;

	@Override
	public File relativeTo(String relativePath) throws IOException
	{
		if (relativePath == null)
			throw new IllegalArgumentException("relativePath argument cannot be null.");
		if (relativePath.startsWith("/") || relativePath.startsWith("\\"))
			throw new IllegalArgumentException("relativePath argument cannot be absolute.");

		return relativeToImpl(relativePath).getAbsoluteFile();
	}
}