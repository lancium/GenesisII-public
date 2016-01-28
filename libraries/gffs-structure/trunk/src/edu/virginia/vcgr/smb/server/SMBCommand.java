package edu.virginia.vcgr.smb.server;

import java.io.IOException;

public interface SMBCommand
{
	/**
	 * 
	 * @param c
	 * @param h
	 * @param params
	 * @param data
	 * @param message
	 * @param acc
	 *            The message data for the
	 * @throws IOException
	 * @throws SMBException
	 */
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc)
		throws IOException, SMBException;
}
