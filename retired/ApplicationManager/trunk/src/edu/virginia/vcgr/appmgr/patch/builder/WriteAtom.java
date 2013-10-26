package edu.virginia.vcgr.appmgr.patch.builder;

import java.io.PrintStream;

import edu.virginia.vcgr.appmgr.patch.PatchOperationType;

public class WriteAtom extends AbstractPatchAtom
{
	private String _permissions = null;

	public WriteAtom(String relativePath)
	{
		super(PatchOperationType.WRITE, relativePath);
	}

	public void setPermissions(String permissions)
	{
		_permissions = permissions;
	}

	@Override
	public void emit(PrintStream out)
	{
		if (_permissions != null)
			out.format("\t\t<write permissions=\"%s\">%s</write>\n", _permissions, getRelativePath());
		else
			out.format("\t\t<write>%s</write>\n", getRelativePath());
	}
}