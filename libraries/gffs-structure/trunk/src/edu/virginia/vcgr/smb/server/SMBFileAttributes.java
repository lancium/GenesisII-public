package edu.virginia.vcgr.smb.server;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.filters.RNSFilter;

public class SMBFileAttributes implements RNSFilter {
	public static final int NORMAL = 0x0;
	public static final int READONLY = 0x1;
	public static final int HIDDEN = 0x2;
	public static final int SYSTEM = 0x4;
	public static final int VOLUME = 0x8;
	public static final int DIRECTORY = 0x10;
	public static final int ARCHIVE = 0x20;
	public static final int SEARCH_READONLY = 0x100;
	public static final int SEARCH_HIDDEN = 0x200;
	public static final int SEARCH_SYSTEM = 0x400;
	public static final int SEARCH_DIRECTORY = 0x1000;
	public static final int SEARCH_ARCHIVE = 0x2000;
	
	public static final int SEARCH = SEARCH_READONLY | SEARCH_HIDDEN | SEARCH_SYSTEM | SEARCH_DIRECTORY | SEARCH_ARCHIVE;
	
	private int fileAttr;
	
	public SMBFileAttributes(int fileAttr) {
		this.fileAttr = fileAttr;
	}
	
	//public int 
	
	public static SMBFileAttributes fromSearch(int fileAttr) {
		return new SMBFileAttributes(fileAttr | READONLY | ARCHIVE);
	}
	
	public static int fromTypeInfo(TypeInformation info) {
		if (info.isRNS()) {
			return DIRECTORY;
		} else if (info.isByteIO()) {
			return NORMAL;
		} else {
			return SYSTEM;
		}
	}

	@Override
	public boolean matches(RNSPath testEntry) {
		int include = fileAttr & (READONLY | HIDDEN | SYSTEM | DIRECTORY | ARCHIVE);
		int exclude = fileAttr & (SEARCH_READONLY | SEARCH_HIDDEN | SEARCH_SYSTEM | SEARCH_DIRECTORY | SEARCH_ARCHIVE);
		exclude >>= 8;
		
		try {
			TypeInformation info = new TypeInformation(testEntry.getEndpoint());
			int attr = fromTypeInfo(info);
			
			if ((exclude & attr) != exclude)
				return false;
			
			if ((include & attr) != attr)
				return false;
			
			return true;
		} catch (RNSPathDoesNotExistException e) {
			return false;
		}
	}
}
