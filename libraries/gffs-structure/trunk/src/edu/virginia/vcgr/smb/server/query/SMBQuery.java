package edu.virginia.vcgr.smb.server.query;

import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;

public class SMBQuery
{
	public final static int INFO_STANDARD = 0x1;
	public final static int INFO_QUERY_EA_SIZE = 0x2;
	public final static int INFO_QUERY_EAS_FROM_LIST = 0x3;
	public final static int INFO_QUERY_ALL_EAS = 0x4;
	public final static int IS_NAME_VALID = 0x6;
	// NT
	public final static int FILE_BASIC_INFO = 0x101;
	public final static int FILE_STANDARD_INFO = 0x102;
	public final static int FILE_EA_INFO = 0x103;
	public final static int FILE_NAME_INFO = 0x104;
	public final static int FILE_ALL_INFO = 0x107;
	public final static int FILE_ALT_NAME_INFO = 0x108;
	public final static int FILE_STREAM_INFO = 0x109;
	public final static int FILE_COMPRESSION_INFO = 0x10B;

	public static void encode(SMBFile fd, SMBBuffer buffer, boolean unicode, int level) throws SMBException
	{
		switch (level) {
			case INFO_STANDARD:
				SMBInfoStandard.encode(fd, buffer);
				break;
			case INFO_QUERY_EA_SIZE:
				SMBInfoQueryEASize.encode(fd, buffer);
				break;
			case INFO_QUERY_EAS_FROM_LIST:
				throw new SMBException(NTStatus.EAS_NOT_SUPPORTED);
			case INFO_QUERY_ALL_EAS:
				throw new SMBException(NTStatus.EAS_NOT_SUPPORTED);
			case IS_NAME_VALID:
				// Should be ok
				break;
			case FILE_BASIC_INFO:
				SMBQueryFileBasicInfo.encode(fd, buffer);
				break;
			case FILE_STANDARD_INFO:
				SMBQueryFileStandardInfo.encode(fd, buffer);
				break;
			case FILE_EA_INFO:
				SMBQueryFileEAInfo.encode(fd, buffer);
				break;
			case FILE_NAME_INFO:
				SMBQueryFileNameInfo.encode(fd, buffer);
				break;
			case FILE_ALL_INFO:
				SMBQueryFileAllInfo.encode(fd, buffer);
				break;
			case FILE_ALT_NAME_INFO:
				throw new SMBException(NTStatus.NOT_IMPLEMENTED);
			case FILE_STREAM_INFO:
				SMBQueryFileStreamInfo.encode(fd, buffer);
				break;
			case FILE_COMPRESSION_INFO:
				SMBQueryFileCompressionInfo.encode(fd, buffer);
				break;
			default:
				throw new SMBException(NTStatus.OS2_INVALID_LEVEL);
		}
	}

}
