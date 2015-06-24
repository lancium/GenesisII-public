package edu.virginia.vcgr.smb.server.find;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBSearchState;
import edu.virginia.vcgr.smb.server.SMBSearchState.Entry;

public class SMBFindNamesInfo
{

	public static int encode(SMBSearchState search, int searchCount, boolean unicode, SMBBuffer buffer) throws SMBException
	{
		int count = 0;
		int nextOffsetLoc = 0;
		while (count < searchCount) {
			if (!search.hasNext())
				break;

			Entry cur = search.next();
			String fileName = cur.getName();

			nextOffsetLoc = buffer.skip(4);
			buffer.putInt(0);
			buffer.putInt(buffer.strlen(fileName, unicode));
			buffer.putString(fileName, unicode);

			buffer.putInt(nextOffsetLoc, buffer.position() - nextOffsetLoc);

			count++;
		}

		if (count > 0)
			buffer.putInt(nextOffsetLoc, 0);

		return count;
	}

}
