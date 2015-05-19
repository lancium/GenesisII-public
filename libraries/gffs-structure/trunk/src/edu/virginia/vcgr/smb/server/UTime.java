package edu.virginia.vcgr.smb.server;

import java.util.Date;
import java.util.TimeZone;

public class UTime {
	private int time;
	
	private UTime(int time) {
		this.time = time;
	}
	
	public void encode(SMBBuffer acc) {
		acc.putInt(time);
	}
	
	public static UTime decode(SMBBuffer acc) {
		return new UTime(acc.getInt());
	}
	
	public static UTime fromMillis(long millis) {
		long localMillis = millis + TimeZone.getDefault().getOffset(millis);
		
		return new UTime((int)(localMillis / 1000));
	}
	
	public static UTime fromDate(Date create) {
		long millis = create.getTime();
		
		return fromMillis(millis);
	}
	
	public long toMillis() {
		return 1000 * (time - TimeZone.getDefault().getOffset(time));
	}

	public boolean isZero() {
		return time == 0;
	}
	
	public boolean isMax() {
		return time == -1;
	}
}
