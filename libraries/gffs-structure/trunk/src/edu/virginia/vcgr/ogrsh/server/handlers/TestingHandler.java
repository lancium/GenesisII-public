package edu.virginia.vcgr.ogrsh.server.handlers;

import edu.virginia.vcgr.ogrsh.server.comm.OGRSHOperation;
import edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException;

public class TestingHandler
{
	@OGRSHOperation
	public int OGRSHAdder(int a, int b)
	{
		return a + b;
	}

	@OGRSHOperation
	public float OGRSHDivide(int a, int b) throws OGRSHException
	{
		if (b == 0)
			throw new OGRSHException("Divide by zero exception.", OGRSHException.EXCEPTION_DIVIDE_BY_ZERO);

		return (float) a / b;
	}
}