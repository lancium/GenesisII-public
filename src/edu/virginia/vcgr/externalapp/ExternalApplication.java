package edu.virginia.vcgr.externalapp;

import java.io.File;

public interface ExternalApplication
{
	public ExternalApplicationToken launch(
		File content, ExternalApplicationCallback...callbacks)
			throws ExternalApplicationException;
}