package edu.virginia.vcgr.smb.server.find;

import java.util.List;

import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBSearchState;

public class SMBFind {
	public static final int CLOSE_AFTER_REQUEST = 0x0001;
	public static final int CLOSE_AT_EOS = 0x0002;
	public static final int RETURN_RESUME_KEYS = 0x0004;
	public static final int CONTINUE_FROM_LAST = 0x0008;
	public static final int WITH_BACKUP_INTENT = 0x0010;
	
	public final static int INFO_STANDARD = 0x1;
	public final static int INFO_QUERY_EA_SIZE = 0x2;
	public final static int INFO_QUERY_EAS_FROM_LIST = 0x3;
	public final static int FILE_DIRECTORY_INFO = 0x101;
	public final static int FILE_FULL_DIRECTORY_INFO = 0x102;
	public final static int FILE_NAMES_INFO = 0x103;
	public final static int FILE_BOTH_DIRECTORY_INFO = 0x104;
	
	public static int encode(SMBSearchState search, int searchCount, boolean resume, int infoLevel, List<String> xattr, boolean unicode, SMBBuffer buffer) throws SMBException {
		switch (infoLevel) {
		case INFO_STANDARD:
			return SMBInfoStandard.encode(search, searchCount, resume, unicode, buffer);
		case INFO_QUERY_EA_SIZE:
			return SMBQueryEASize.encode(search, searchCount, resume, unicode, buffer);
		case INFO_QUERY_EAS_FROM_LIST:
			throw new SMBException(NTStatus.EAS_NOT_SUPPORTED);
		case FILE_DIRECTORY_INFO:
			return SMBFindFileDirectoryInfo.encode(search, searchCount, unicode, buffer);
		case FILE_FULL_DIRECTORY_INFO:
			return SMBFindFileFullDirectoryInfo.encode(search, searchCount, unicode, buffer);
		case FILE_NAMES_INFO:
			return SMBFindNamesInfo.encode(search, searchCount, unicode, buffer);
		case FILE_BOTH_DIRECTORY_INFO:
			return SMBFindFileBothDirectoryInfo.encode(search, searchCount, unicode, buffer);
		default:
			throw new SMBException(NTStatus.OS2_INVALID_LEVEL);
		}
	}
}
