package edu.virginia.vcgr.appmgr.security;

public interface Verifier
{
	public void verify(String entryName, Object... signers) throws VerificationException;
}