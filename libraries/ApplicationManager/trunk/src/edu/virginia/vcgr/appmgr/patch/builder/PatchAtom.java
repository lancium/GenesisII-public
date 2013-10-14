package edu.virginia.vcgr.appmgr.patch.builder;

import java.io.PrintStream;

import edu.virginia.vcgr.appmgr.patch.PatchOperationType;

public interface PatchAtom
{
	public PatchOperationType getOperationType();

	public void emit(PrintStream out);
}