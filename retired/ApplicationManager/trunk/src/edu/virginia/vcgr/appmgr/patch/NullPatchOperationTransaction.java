package edu.virginia.vcgr.appmgr.patch;

class NullPatchOperationTransaction implements PatchOperationTransaction
{
	@Override
	public void commit()
	{
	}

	@Override
	public void rollback()
	{
	}
}