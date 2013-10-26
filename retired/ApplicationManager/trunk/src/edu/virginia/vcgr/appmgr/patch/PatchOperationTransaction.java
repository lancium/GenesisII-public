package edu.virginia.vcgr.appmgr.patch;

public interface PatchOperationTransaction
{
	public void commit();

	public void rollback();
}