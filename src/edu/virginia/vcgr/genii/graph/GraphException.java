package edu.virginia.vcgr.genii.graph;

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