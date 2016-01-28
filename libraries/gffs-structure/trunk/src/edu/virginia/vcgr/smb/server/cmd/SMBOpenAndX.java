package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;
import java.util.Date;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.SMBAndX;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBFileAttributes;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;
import edu.virginia.vcgr.smb.server.UTime;

public class SMBOpenAndX implements SMBCommand
{
	public final static int REQ_ATTRIB = 0x0001;
	public final static int REQ_OPLOCK = 0x0002;
	public final static int REQ_OPLOCK_BATCH = 0x0004;

	public final static int ACCESS_MODE = 0x0007;
	public final static int SHARE_MODE = 0x0070;
	public final static int REFERENCE_LOCALITY = 0x0700;
	public final static int CACHE_MODE = 0x1000;
	public final static int WRITETHROUGH_MODE = 0x4000;

	public static final int SHARE_COMPAT = 0x00;
	public static final int SHARE_DENY_RW = 0x10;
	public static final int SHARE_DENY_WRITE = 0x20;
	public static final int SHARE_DENY_READ = 0x30;
	public static final int SHARE_DENY_NONE = 0x40;

	public final static int FILE_EXISTS_OPTS = 0x0003;
	public final static int FILE_EXISTS_FAIL = 0x0000;
	public final static int FILE_EXISTS_APPEND = 0x0001;
	public final static int FILE_EXISTS_TRUNCATE = 0x0002;
	public final static int CREATE_FILE = 0x0010;

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc)
		throws IOException, SMBException
	{
		SMBAndX chain = SMBAndX.decode(params);
		int flags = params.getUShort();
		int accessMode = params.getUShort();
		// Used to check if the file already exists; Windows Servers don't use this and neither do we
		int searchAttr = params.getUShort();
		// Used to create file
		int fileAttr = params.getUShort();
		UTime creationTime = UTime.decode(params);
		int openMode = params.getUShort();
		long allocationSize = params.getUInt();
		// In ms
		long timeout = params.getUInt();
		params.getInt();

		String path = data.getString(h.isUnicode());

		if (accessMode == 0 || searchAttr == 0 || allocationSize == 0 || timeout == 0) {
			// do nothing; just silence warnings about unused vars.
		}

		//
		SMBTree tree = c.verifyTID(h.tid);
		RNSPath file = tree.lookup(path, h.isCaseSensitive());

		boolean existed = file.exists();
		boolean create = (openMode & CREATE_FILE) != 0;
		boolean excl = (openMode & FILE_EXISTS_OPTS) == FILE_EXISTS_FAIL;
		boolean trunc = (openMode & FILE_EXISTS_OPTS) == FILE_EXISTS_TRUNCATE;

		SMBFile fd = SMBTree.open(file, fileAttr, create, excl, trunc);
		int FID = tree.allocateFID(fd);
		h.fid = FID;

		if (!existed)
			fd.setCreateTime(creationTime.toMillis());

		acc.startParameterBlock();
		int from = SMBAndX.reserve(acc);
		acc.putShort((short) FID);
		if ((flags & REQ_ATTRIB) != 0) {
			TypeInformation type = new TypeInformation(fd.getEPR());
			Date write = new Date();
			long fileSize = 0;
			if (type.isByteIO()) {
				write = type.getByteIOModificationTime();
				fileSize = type.getByteIOSize();
			}

			acc.putShort((short) SMBFileAttributes.fromTypeInfo(type));
			UTime.fromMillis(write.getTime()).encode(acc);
			acc.putInt((int) fileSize);
			// future: maybe base off of ACCESS_MODE
			if (type.isRByteIO())
				acc.putShort((short) (2 | SHARE_DENY_NONE));
			else
				acc.putShort((short) (0 | SHARE_DENY_NONE));
			// Always a disk
			acc.putShort((short) 0);
			// We don't support pipes
			acc.putShort((short) 0);
			if (existed && !trunc)
				acc.putShort((short) 1);
			else if (!existed)
				acc.putShort((short) 2);
			else
				acc.putShort((short) 3);
		} else {
			acc.putShort((short) 0);
			acc.putInt(0);
			acc.putInt(0);
			acc.putShort((short) 0);
			acc.putShort((short) 0);
			acc.putShort((short) 0);
			acc.putShort((short) 0);
			acc.putShort((short) 0);
		}
		acc.putShort((short) 0);
		acc.putShort((short) 0);
		acc.putShort((short) 0);
		acc.finishParameterBlock();

		acc.emptyDataBlock();

		SMBAndX.encode(acc, from, chain.getCommand());
		c.doAndX(h, chain, message, acc);
	}
}
