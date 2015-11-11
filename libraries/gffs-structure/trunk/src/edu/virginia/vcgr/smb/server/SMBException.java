package edu.virginia.vcgr.smb.server;

public class SMBException extends Exception
{
	private static final long serialVersionUID = 1L;

	private int ntstatus;

	public SMBException(int ntstatus)
	{
		// set the message for later use from our status strings.
		super(NTStatus.messageForStatus(ntstatus));
		this.ntstatus = ntstatus;
	}

	public int getStatus()
	{
		return ntstatus;
	}
}
