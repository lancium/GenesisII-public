package edu.virginia.vcgr.genii.ui.plugins.acct;

import edu.virginia.vcgr.genii.client.acct.AccountingCredentialTypes;

class CredentialBundle
{
	private long _cid;
	private AccountingCredentialTypes _credType;
	private String _credDesc;
	private boolean _isDirty = false;
	
	CredentialBundle(long cid, AccountingCredentialTypes credType,
		String credDesc)
	{
		_cid = cid;
		_credType = credType;
		_credDesc = credDesc;
	}
	
	final long cid()
	{
		return _cid;
	}
	
	final AccountingCredentialTypes credentialType()
	{
		return _credType;
	}
	
	final void credentialType(AccountingCredentialTypes credType)
	{
		_credType = credType;
		_isDirty = true;
	}
	
	final boolean isDirty()
	{
		return _isDirty;
	}
	
	@Override
	final public String toString()
	{
		return _credDesc;
	}
}