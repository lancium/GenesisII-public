package edu.virginia.vcgr.genii.client.gui.exportdir;

public class NoContainersException extends Exception
{
	static final long serialVersionUID = 0L;

	public NoContainersException()
	{
		super("No containers are available on the local machine in which to create exports.");
	}
}