package edu.virginia.vcgr.genii.algorithm.graph;

public class GraphException extends Exception
{
	static final long serialVersionUID = 0L;

	public GraphException(String msg)
	{
		super(msg);
	}

	public GraphException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}