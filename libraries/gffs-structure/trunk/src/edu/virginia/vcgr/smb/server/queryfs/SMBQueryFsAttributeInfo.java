package edu.virginia.vcgr.smb.server.queryfs;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;

public class SMBQueryFsAttributeInfo
{
	public final static int FILE_CASE_SENSITIVE_SEARCH = 0x00000001;
	public final static int FILE_CASE_PRESERVED_NAMES = 0x00000002;
	public final static int FILE_UNICODE_ON_DISK = 0x00000004;
	public final static int FILE_PERSISTENT_ACLS = 0x00000008;
	public final static int FILE_FILE_COMPRESSION = 0x00000010;
	public final static int FILE_VOLUME_IS_COMPRESSED = 0x00008000;

	public static void encode(SMBBuffer output) throws SMBException
	{
		int length = SMBQueryFs.FILESYSTEM_NAME.length();

		output.putInt(FILE_CASE_SENSITIVE_SEARCH | FILE_CASE_PRESERVED_NAMES | FILE_UNICODE_ON_DISK);
		// Maximum size of filename; XXX: maybe choose a better value
		output.putInt(0x100);// output.putInt(0xffff);
		output.putInt(length * 2);
		for (int i = 0; i < length; i++)
			output.putChar(SMBQueryFs.FILESYSTEM_NAME.charAt(i));
	}
}
