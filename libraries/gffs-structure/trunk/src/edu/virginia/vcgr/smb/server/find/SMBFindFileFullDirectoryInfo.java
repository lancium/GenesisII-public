package edu.virginia.vcgr.smb.server.find;

import java.util.Date;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.FileTime;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBExtFileAttributes;
import edu.virginia.vcgr.smb.server.SMBSearchState;
import edu.virginia.vcgr.smb.server.SMBSearchState.Entry;

public class SMBFindFileFullDirectoryInfo {
	public static int encode(SMBSearchState search, int searchCount, boolean unicode, SMBBuffer buffer) throws SMBException {
		int count = 0;
		int nextOffsetLoc = 0;
		while (count < searchCount) {
			if (!search.hasNext())
				break;
			
			Entry cur = search.next();
			String fileName = cur.getName();
			RNSPath path = cur.getPath();
			
			Date create = new Date(), access = create, write = create, attrChange = create;
			long fileSize = 0;
			int attrs = SMBExtFileAttributes.SYSTEM;
			
			try {
				TypeInformation type = new TypeInformation(path.getEndpoint());
				
				if (type.isByteIO()) {
					create = type.getByteIOCreateTime();
					access = type.getByteIOAccessTime();
					write = type.getByteIOModificationTime();
					attrChange = write;
					fileSize = type.getByteIOSize();
				}
				
				attrs = SMBExtFileAttributes.fromTypeInfo(type);
			} catch (RNSException e) {
				
			}
				
			long allocationSize = fileSize;
			
			nextOffsetLoc = buffer.skip(4);
			buffer.putInt(0);
			FileTime.fromDate(create).encode(buffer);
			FileTime.fromDate(access).encode(buffer);
			FileTime.fromDate(write).encode(buffer);
			FileTime.fromDate(attrChange).encode(buffer);
			buffer.putLong(fileSize);
			buffer.putLong(allocationSize);
			buffer.putInt(attrs);
			buffer.putInt(buffer.strlen(fileName, unicode));
			// No EAs
			buffer.putInt(0);
			buffer.putString(fileName, unicode);

			buffer.putInt(nextOffsetLoc, buffer.position() - nextOffsetLoc);
			
			count++;
		}
		
		if (count > 0)
			buffer.putInt(nextOffsetLoc, 0);
		
		return count;
	}
}
