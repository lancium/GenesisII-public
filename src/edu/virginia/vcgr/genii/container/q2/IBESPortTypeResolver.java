package edu.virginia.vcgr.genii.container.q2;

import java.sql.Connection;

import org.ggf.bes.BESPortType;

public interface IBESPortTypeResolver
{
	public BESPortType createClientStub(
		Connection connection, long besID)
			throws Throwable;
}