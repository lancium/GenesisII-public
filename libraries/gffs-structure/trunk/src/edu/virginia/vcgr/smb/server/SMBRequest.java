package edu.virginia.vcgr.smb.server;

public class SMBRequest {
	/* The complete message; the current position follows this request */
	public SMBBuffer message;
	public SMBHeader header;
	public SMBBuffer params, data;
	
	public SMBRequest(SMBBuffer message, SMBHeader header, SMBBuffer params,
			SMBBuffer data) {
		this.message = message;
		this.header = header;
		this.params = params;
		this.data = data;
	}
}
