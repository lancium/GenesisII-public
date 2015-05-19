package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;
import java.util.Date;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.FileTime;
import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBAndX;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBExtFileAttributes;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBFileAttributes;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBNTCreateAndX implements SMBCommand
{
	public static final int FILE_SHARE_NONE = 0x0;
	public static final int FILE_SHARE_READ = 0x1;
	public static final int FILE_SHARE_WRITE = 0x2;
	public static final int FILE_SHARE_DELETE = 0x4;

	public static final int FILE_SUPERSEDE = 0x0;
	public static final int FILE_OPEN = 0x1;
	public static final int FILE_CREATE = 0x2;
	public static final int FILE_OPEN_IF = 0x3;
	public static final int FILE_OVERWRITE = 0x4;
	public static final int FILE_OVERWRITE_IF = 0x5;

	public static final int FILE_DIRECTORY_FILE = 0x1;
	public static final int FILE_NON_DIRECTORY_FILE = 0x40;

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		SMBAndX chain = SMBAndX.decode(params);
		params.get();
		int nameLength = params.getUShort();
		int flags = params.getInt();
		int rootFID = params.getInt();
		int desiredAccess = params.getInt();
		long reqAllocSize = params.getLong();
		int fileAttr = params.getInt();
		int shareAccess = params.getInt();
		int createDisp = params.getInt();
		int createOptions = params.getInt();
		int impersonationLevel = params.getInt();
		int securityFlags = params.get();

		if (desiredAccess == 0 || reqAllocSize == 0 || flags == 0 || shareAccess == 0 || impersonationLevel == 0 || securityFlags == 0
			|| nameLength == 0) {
			// just silence unused var warnings.
		}

		String fileName = data.getString(h.isUnicode());

		SMBTree tree = c.verifyTID(h.tid);
		RNSPath file;
		if (rootFID != 0) {
			SMBFile root = tree.verifyFID(rootFID);
			file = SMBTree.lookup(root.getPath(), fileName, h.isCaseSensitive());
		} else {
			file = tree.lookup(fileName, h.isCaseSensitive());
		}

		// FILE_SUPERSEDE is a bit weird; it's actually a delete and create
		boolean excl = createDisp == FILE_CREATE;
		boolean create =
			createDisp == FILE_SUPERSEDE || createDisp == FILE_CREATE || createDisp == FILE_OPEN_IF || createDisp == FILE_OVERWRITE_IF;
		boolean trunc = createDisp == FILE_SUPERSEDE || createDisp == FILE_OVERWRITE || createDisp == FILE_OVERWRITE_IF;
		boolean dir = (createOptions & FILE_DIRECTORY_FILE) != 0;
		boolean nonDir = (createOptions & FILE_NON_DIRECTORY_FILE) != 0;

		if (dir)
			fileAttr = SMBFileAttributes.DIRECTORY;

		SMBFile fd = SMBTree.open(file, fileAttr, create, excl, trunc);

		TypeInformation info = new TypeInformation(fd.getEPR());
		int attr = SMBExtFileAttributes.fromTypeInfo(info);

		if (dir && (attr & SMBExtFileAttributes.DIRECTORY) == 0)
			throw new SMBException(NTStatus.NO_SUCH_FILE);

		if (nonDir && (attr & SMBExtFileAttributes.DIRECTORY) != 0)
			throw new SMBException(NTStatus.FILE_IS_A_DIRECTORY);

		int FID = tree.allocateFID(fd);

		Date createTime = info.getByteIOCreateTime();
		Date accessTime = info.getByteIOAccessTime();
		Date writeTime = info.getByteIOModificationTime();
		Date changeTime = writeTime;
		long fileSize = 0;
		if (info.isByteIO())
			fileSize = info.getByteIOSize();
		long allocSize = fileSize;

		acc.startParameterBlock();
		int andx = SMBAndX.reserve(acc);
		// No oplock granted
		acc.put((byte) 0);
		acc.putShort((short) FID);
		// Return the input ?
		acc.putInt(createDisp);
		FileTime.fromDate(createTime).encode(acc);
		FileTime.fromDate(accessTime).encode(acc);
		FileTime.fromDate(writeTime).encode(acc);
		FileTime.fromDate(changeTime).encode(acc);
		acc.putInt(attr);
		acc.putLong(allocSize);
		acc.putLong(fileSize);
		// File or directory
		acc.putShort((short) 0);
		// Not a pipe
		acc.putShort((short) 0);
		if (info.isRNS())
			acc.put((byte) 1);
		else
			acc.put((byte) 0);
		acc.finishParameterBlock();

		acc.emptyDataBlock();

		SMBAndX.encode(acc, andx, chain.getCommand());

		c.doAndX(h, chain, message, acc);
	}
}
