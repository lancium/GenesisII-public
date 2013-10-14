package edu.virginia.vcgr.genii.client.resource;

public interface Rollbackable
{
	void rollbackResource();

	void commitResource() throws ResourceException;
}
