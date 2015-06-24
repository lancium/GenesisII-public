package edu.virginia.vcgr.smb.server.set;

import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;

public class SMBSet
{
	public static final int INFO_STANDARD = 0x0001;
	public static final int INFO_SET_EAS = 0x0002;
	public static final int FILE_BASIC_INFO = 0x0101;
	public static final int FILE_DISPOSITION_INFO = 0x0102;
	public static final int FILE_ALLOCATION_INFO = 0x0103;
	public static final int FILE_END_OF_FILE_INFO = 0x0104;

	public static void encode(SMBFile fd, SMBBuffer dataIn, boolean unicode, int level) throws SMBException
	{
		switch (level) {
			case INFO_STANDARD:
				SMBInfoStandard.encode(fd, dataIn);
				break;
			case INFO_SET_EAS:
				throw new SMBException(NTStatus.EAS_NOT_SUPPORTED);
			case FILE_BASIC_INFO:
				SMBSetFileBasicInfo.encode(fd, dataIn);
				break;
			case FILE_DISPOSITION_INFO:
				SMBSetFileDispositionInfo.encode(fd, dataIn);
				break;
			case FILE_ALLOCATION_INFO:
				SMBSetFileAllocationInfo.encode(fd, dataIn);
				break;
			case FILE_END_OF_FILE_INFO:
				SMBSetFileEndOfFileInfo.encode(fd, dataIn);
				break;
			default:
				throw new SMBException(NTStatus.OS2_INVALID_LEVEL);
		}
	}
}
