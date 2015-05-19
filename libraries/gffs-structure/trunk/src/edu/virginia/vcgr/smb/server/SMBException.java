package edu.virginia.vcgr.smb.server;

public class SMBException extends Exception {
	private static final long serialVersionUID = 1L;

	private int ntstatus;
	
	public SMBException(int ntstatus) {
		this.ntstatus = ntstatus;
	}
	
	public int getStatus() {
		return ntstatus;
	}
}
