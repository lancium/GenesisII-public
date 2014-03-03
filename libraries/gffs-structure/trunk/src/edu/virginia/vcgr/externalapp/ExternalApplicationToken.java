package edu.virginia.vcgr.externalapp;

import java.io.File;

import edu.virginia.vcgr.genii.client.cmd.ToolException;

public interface ExternalApplicationToken {
	public File getResult() throws ToolException;
}