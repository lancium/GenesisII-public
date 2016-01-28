package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;
import java.util.Date;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBDate;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBFileAttributes;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTime;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBQueryInformation2 implements SMBCommand
{
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc)
		throws IOException, SMBException
	{
		int FID = params.getUShort();

		// handle

		SMBTree tree = c.verifyTID(h.tid);
		SMBFile file = tree.verifyFID(FID);
		TypeInformation type = new TypeInformation(file.getEPR());

		Date create = new Date(), access = create, write = create;
		long fileSize = 0;

		if (type.isByteIO()) {
			fileSize = type.getByteIOSize();
			create = type.getByteIOCreateTime();
			access = type.getByteIOAccessTime();
			write = type.getByteIOModificationTime();
		}

		long allocSize = fileSize;

		// out

		acc.startParameterBlock();
		SMBDate.fromDate(create).encode(acc);
		SMBTime.fromDate(create).encode(acc);
		SMBDate.fromDate(access).encode(acc);
		SMBTime.fromDate(access).encode(acc);
		SMBDate.fromDate(write).encode(acc);
		SMBTime.fromDate(write).encode(acc);
		acc.putInt((int) fileSize);
		acc.putInt((int) allocSize);
		acc.putShort((short) SMBFileAttributes.fromTypeInfo(type));
		acc.finishParameterBlock();

		acc.emptyDataBlock();

		c.sendSuccess(h, acc);
	}
}
