package edu.virginia.vcgr.appmgr.patch.builder;

import java.io.PrintStream;

import edu.virginia.vcgr.appmgr.patch.PatchOperationType;

class DeleteAtom extends AbstractPatchAtom
{
	DeleteAtom(String relativePath)
	{
		super(PatchOperationType.DELETE, relativePath);
	}

	@Override
	public void emit(PrintStream out)
	{
		out.format("\t\t<delete>%s</delete>\n", getRelativePath());
	}
}