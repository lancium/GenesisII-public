package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;
import java.util.Date;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBFileAttributes;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBOpen implements SMBCommand
{
	public final static int ACCESS_MODE = 0x0007;
	public final static int SHARE_MODE = 0x0070;
	public final static int REFERENCE_LOCALITY = 0x0700;
	public final static int CACHE_MODE = 0x1000;
	public final static int WRITETHROUGH_MODE = 0x4000;

	public static final int ACCESS_READ = 0x0;
	public static final int ACCESS_WRITE = 0x1;
	public static final int ACCESS_RW = 0x2;
	public static final int ACCESS_EXEC = 0x3;

	public static final int SHARE_COMPAT = 0x00;
	public static final int SHARE_DENY_RW = 0x10;
	public static final int SHARE_DENY_WRITE = 0x20;
	public static final int SHARE_DENY_READ = 0x30;
	public static final int SHARE_DENY_NONE = 0x40;

	public static final int CACHE_ENABLED = 0x0000;
	public static final int CACHE_DISABLED = 0x1000;

	public static final int WRITETHROUGH_DISABLED = 0x0000;
	public static final int WRITETHROUGH_ENABLED = 0x4000;

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		int reqAccessMode = params.getUShort();
		// Not used by NT servers; so neither do we
		/* int searchAttr = */params.getUShort();

		String path = data.getSMBString(h.isUnicode());

		// handling

		SMBTree tree = c.verifyTID(h.tid);
		RNSPath file = tree.lookup(path, h.isCaseSensitive());
		SMBFile fd = SMBTree.open(file, SMBFileAttributes.NORMAL, false, false, false);
		int FID = tree.allocateFID(fd);

		TypeInformation info = new TypeInformation(fd.getEPR());
		long fileSize = 0;
		Date write = new Date();

		if (info.isByteIO()) {
			fileSize = info.getByteIOSize();
			write = info.getByteIOModificationTime();
		}

		int accessMode =
			(reqAccessMode & ACCESS_MODE) | SHARE_DENY_NONE | (reqAccessMode & REFERENCE_LOCALITY) | CACHE_ENABLED | WRITETHROUGH_ENABLED;

		// out

		acc.startParameterBlock();
		acc.putShort((short) FID);
		acc.putShort((short) SMBFileAttributes.fromTypeInfo(info));
		// future: fix wrong timezone
		acc.putInt((int) (write.getTime() / 1000));
		acc.putInt((int) fileSize);
		acc.putShort((short) accessMode);
		acc.finishParameterBlock();

		acc.emptyDataBlock();

		c.sendSuccess(h, acc);
	}
}
