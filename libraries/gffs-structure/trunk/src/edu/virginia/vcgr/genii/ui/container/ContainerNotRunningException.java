package edu.virginia.vcgr.genii.ui.container;

public class ContainerNotRunningException extends Exception
{
	static final long serialVersionUID = 0L;

	public ContainerNotRunningException()
	{
		super("Container is not running.");
	}
}