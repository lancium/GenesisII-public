package edu.virginia.vcgr.genii.client.jsdl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public interface JSDLFileSystem extends Serializable
{
	public File relativeTo(String relativePath) throws IOException;
	public void release();
}