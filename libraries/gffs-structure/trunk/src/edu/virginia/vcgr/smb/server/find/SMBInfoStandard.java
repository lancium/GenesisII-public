package edu.virginia.vcgr.smb.server.find;

import java.util.Date;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBDate;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFileAttributes;
import edu.virginia.vcgr.smb.server.SMBSearchState;
import edu.virginia.vcgr.smb.server.SMBTime;
import edu.virginia.vcgr.smb.server.SMBSearchState.Entry;
import edu.virginia.vcgr.smb.server.queryfs.SMBQueryFs;

public class SMBInfoStandard
{
	public static int encode(SMBSearchState search, int searchCount, boolean resume, boolean unicode, SMBBuffer buffer) throws SMBException
	{
		int count = 0;
		while (count < searchCount) {
			if (!search.hasNext())
				break;

			Entry cur = search.next();
			String fileName = cur.getName();
			RNSPath path = cur.getPath();

			Date create = new Date(), access = create, write = create;
			long size = 0;
			int attrs = SMBFileAttributes.SYSTEM;

			try {
				TypeInformation type = new TypeInformation(path.getEndpoint());

				if (type.isByteIO()) {
					create = type.getByteIOCreateTime();
					access = type.getByteIOAccessTime();
					write = type.getByteIOModificationTime();
					size = type.getByteIOSize();
				}

				attrs = SMBFileAttributes.fromTypeInfo(type);
			} catch (RNSException e) {

			}

			SMBDate createDate = SMBDate.fromDate(create);
			SMBTime createTime = SMBTime.fromDate(create);
			SMBDate accessDate = SMBDate.fromDate(access);
			SMBTime accessTime = SMBTime.fromDate(access);
			SMBDate writeDate = SMBDate.fromDate(write);
			SMBTime writeTime = SMBTime.fromDate(write);

			int allocSize = SMBQueryFs.UNIT_SIZE * SMBQueryFs.SECTOR_SIZE;
			long fileSize = (size + allocSize - 1) / allocSize;

			if (resume)
				buffer.putInt(search.genResumeKey());
			createDate.encode(buffer);
			createTime.encode(buffer);
			accessDate.encode(buffer);
			accessTime.encode(buffer);
			writeDate.encode(buffer);
			writeTime.encode(buffer);
			buffer.putInt((int) fileSize);
			buffer.putInt((int) allocSize);
			buffer.putShort((short) attrs);
			buffer.put((byte) buffer.strlen(fileName, unicode));
			buffer.putString(fileName, unicode);

			count++;
		}

		return count;
	}
}
