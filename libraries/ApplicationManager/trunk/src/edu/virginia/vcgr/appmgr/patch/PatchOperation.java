package edu.virginia.vcgr.appmgr.patch;

import java.io.IOException;
import java.io.PrintStream;

public interface PatchOperation
{
	public boolean satisfies();

	public PatchOperationTransaction perform(PrintStream log) throws IOException;
}