package edu.virginia.vcgr.smb.server.queryfs;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;

public class SMBInfoAllocation {
	public static void encode(SMBBuffer output) throws SMBException {
		// XSEDE GRID
		output.putInt(0);//output.putInt(0x5EDE641D);
		output.putInt(SMBQueryFs.UNIT_SIZE);
		output.putInt(0xffffffff);
		output.putInt(0xffffffff);
		output.putShort(SMBQueryFs.SECTOR_SIZE);
	}
}
