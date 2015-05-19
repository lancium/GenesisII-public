package edu.virginia.vcgr.smb.server;

import java.util.Calendar;
import java.util.Date;

public class SMBTime {
	private int time;
	
	private SMBTime(int time) {
		this.time = time;
	}
	
	public void encode(SMBBuffer buffer) {
		buffer.putShort((short)time);
	}
	
	public static SMBTime decode(SMBBuffer acc) {
		return new SMBTime(acc.getUShort());
	}

	public static SMBTime fromMillis(long millis) {
		Calendar tmp = Calendar.getInstance();
		tmp.setTimeInMillis(millis);
		
		int hours = tmp.get(Calendar.HOUR_OF_DAY);
		int min = tmp.get(Calendar.MINUTE);
		int sec = tmp.get(Calendar.SECOND);
		
		return new SMBTime ((hours << 11) | (min << 5) | (sec >> 1));
	}
	
	public static SMBTime fromDate(Date d) {
		return fromMillis(d.getTime());
	}

	public void toCalendar(Calendar tmp) {
		int hours = (time >> 11) & 0x1f;
		int min = (time >> 5) & 0x3f;
		int sec = (time << 1) & 0x3f;
		
		tmp.set(Calendar.HOUR_OF_DAY, hours);
		tmp.set(Calendar.MINUTE, min);
		tmp.set(Calendar.SECOND, sec);
	}
}
