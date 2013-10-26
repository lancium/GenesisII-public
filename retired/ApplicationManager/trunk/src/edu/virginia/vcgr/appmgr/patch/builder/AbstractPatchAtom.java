package edu.virginia.vcgr.appmgr.patch.builder;

import edu.virginia.vcgr.appmgr.patch.PatchOperationType;

abstract class AbstractPatchAtom implements PatchAtom
{
	private String _relativePath;
	private PatchOperationType _operationType;

	protected AbstractPatchAtom(PatchOperationType operationType, String relativePath)
	{
		_relativePath = relativePath;
		_operationType = operationType;
	}

	protected String getRelativePath()
	{
		return _relativePath;
	}

	@Override
	public PatchOperationType getOperationType()
	{
		return _operationType;
	}
}