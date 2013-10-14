package edu.virginia.vcgr.appmgr.patch.builder;

import java.io.PrintStream;

public class DeleteWriteAtom extends WriteAtom
{
	public DeleteWriteAtom(String relativePath)
	{
		super(relativePath);
	}

	@Override
	public void emit(PrintStream out)
	{
		out.format("\t\t<delete>%s</delete>\n", getRelativePath());
		super.emit(out);
	}
}