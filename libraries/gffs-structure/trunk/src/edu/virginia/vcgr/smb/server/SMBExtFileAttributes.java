package edu.virginia.vcgr.smb.server;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;

public class SMBExtFileAttributes {
	public final static int READONLY = 0x1;
	public final static int HIDDEN = 0x2;
	public final static int SYSTEM = 0x4;
	public final static int DIRECTORY = 0x10;
	public final static int ARCHIVE = 0x20;
	public final static int NORMAL = 0x80;
	public final static int TEMPORARY = 0x100;
	public final static int COMPRESSED = 0x800;
	public final static int POSIX_SEMANTICS = 0x01000000;
	public final static int BACKUP_SEMANTICS = 0x02000000;
	public final static int DELETE_ON_CLOSE = 0x04000000;
	public final static int SEQUENTIAL_SCAN = 0x08000000;
	public final static int RANDOM_ACCESS = 0x10000000;
	public final static int NO_BUFFERING = 0x20000000;
	public final static int WRITE_THROUGH = 0x80000000;
	
	public static int fromTypeInfo(TypeInformation info) {
		if (info.isRNS()) {
			return DIRECTORY;
		} else if (info.isByteIO()) {
			return NORMAL;
		} else {
			return SYSTEM;
		}
	}
}
