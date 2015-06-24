package edu.virginia.vcgr.smb.server;

public class NTStatus
{
	public static final int SUCCESS = 0;
	public static final int NOT_IMPLEMENTED = 0xC0000002;
	public static final int INVALID_SMB = 0x00010002;
	public static final int NO_MORE_FILES = 0x80000006;
	public static final int INSUFF_SERVER_RESOURCES = 0xC0000205;
	public static final int SMB_BAD_TID = 0x00050002;
	public static final int OBJECT_PATH_NOT_FOUND = 0xC000003A;
	public static final int ACCESS_DENIED = 0xC0000022;
	public static final int SMB_BAD_FID = 0x00060001;
	public static final int INTERNAL_ERROR = 0xC00000E5;
	public static final int BUFFER_OVERFLOW = 0x80000005;
	public static final int OBJECT_NAME_COLLISION = 0xC0000035;
	public static final int NO_SUCH_FILE = 0xC000000F;
	public static final int DATA_ERROR = 0xC000003E;
	public static final int DIRECTORY_NOT_EMPTY = 0xC0000101;
	public static final int OS2_NEGATIVE_SEEK = 0x00830001;
	public static final int END_OF_FILE = 0xC0000011;
	public static final int INVALID_PARAMETER = 0xC000000D;
	public static final int EAS_NOT_SUPPORTED = 0xC000004F;
	public static final int OS2_INVALID_LEVEL = 0x007C0001;
	public static final int FILE_IS_A_DIRECTORY = 0xC00000BA;
}
