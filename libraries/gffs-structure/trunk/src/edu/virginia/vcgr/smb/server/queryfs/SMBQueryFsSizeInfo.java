package edu.virginia.vcgr.smb.server.queryfs;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;

public class SMBQueryFsSizeInfo
{
	public static void encode(SMBBuffer output) throws SMBException
	{
		output.putLong(SMBQueryFs.TOTAL_UNITS);
		output.putLong(SMBQueryFs.TOTAL_UNITS);
		output.putInt(SMBQueryFs.UNIT_SIZE);
		output.putInt(SMBQueryFs.SECTOR_SIZE);
	}
}
