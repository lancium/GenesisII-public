package edu.virginia.vcgr.smb.server.queryfs;

import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;

public class SMBQueryFs {
	public static final short SECTOR_SIZE = 512;
	// Number of sectors in an allocation unit; completely arbitrary
	public static final int UNIT_SIZE = 8;
	public static final long TOTAL_SIZE = 1L * 1024 * 1024 * 1024 * 1024 * 16;
	public static final long TOTAL_UNITS = TOTAL_SIZE / (UNIT_SIZE * SECTOR_SIZE);
	
	public static final int INFO_ALLOCATION = 0x0001;
	public static final int INFO_VOLUME = 0x0002;
	public static final int VOLUME_INFO = 0x0102;
	public static final int SIZE_INFO = 0x0103;
	public static final int DEVICE_INFO = 0x0104;
	public static final int ATTRIBUTE_INFO = 0x105;
	public static final int FULL_SIZE_INFO = 0x03ef;
	
	// XSEDE GRID
	public static final int SERIAL_NUMBER = 0x5EDE641D;
	public static final String VOLUME_LABEL = "XSEDE Grid";
	public static final String FILESYSTEM_NAME = "Genesis II Grid";
	
	
	public static void encode(SMBBuffer buffer, boolean unicode, int level) throws SMBException {
		switch (level) {
		case INFO_ALLOCATION:
			SMBInfoAllocation.encode(buffer);
			break;
		case INFO_VOLUME:
			SMBInfoVolume.encode(buffer, unicode);
			break;
		case VOLUME_INFO:
			SMBQueryFsVolumeInfo.encode(buffer);
			break;
		case SIZE_INFO:
			SMBQueryFsSizeInfo.encode(buffer);
			break;
		case FULL_SIZE_INFO:
			SMBQueryFsFullSizeInfo.encode(buffer);
			break;
		case DEVICE_INFO:
			SMBQueryFsDeviceInfo.encode(buffer);
			break;
		case ATTRIBUTE_INFO:
			SMBQueryFsAttributeInfo.encode(buffer);
			break;
		default:
			throw new SMBException(NTStatus.OS2_INVALID_LEVEL);
		}
	}
}
