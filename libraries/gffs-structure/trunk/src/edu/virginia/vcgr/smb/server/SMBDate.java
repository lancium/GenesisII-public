package edu.virginia.vcgr.smb.server;

import java.util.Calendar;
import java.util.Date;

public class SMBDate {
	private int date;
	
	private SMBDate(int date) {
		this.date = date;
	}
	
	public void encode(SMBBuffer buffer) {
		buffer.putShort((short)date);
	}
	
	public static SMBDate decode(SMBBuffer acc) {
		return new SMBDate(acc.getUShort());
	}
	
	public static SMBDate fromMillis(long millis) {
		Calendar tmp = Calendar.getInstance();
		tmp.setTimeInMillis(millis);
		
		int year = tmp.get(Calendar.YEAR) - 1980;
		int month = tmp.get(Calendar.MONTH) + 1;
		int day = tmp.get(Calendar.DAY_OF_MONTH);
		
		return new SMBDate((year << 9) | (month << 5) | day);
	}

	public static SMBDate fromDate(Date d) {
		return fromMillis(d.getTime());
	}

	public long toMillis(SMBTime createTime) {
		Calendar tmp = Calendar.getInstance();
		toCalendar(tmp);
		createTime.toCalendar(tmp);
		
		return tmp.getTimeInMillis();
	}

	private void toCalendar(Calendar tmp) {
		int year = (date >> 9) & 0x7f;
		int month = (date >> 5) & 0xf;
		int day = (date >> 0) & 0x1f;
		
		tmp.set(Calendar.YEAR, year);
		tmp.set(Calendar.MONTH, month);
		tmp.set(Calendar.DAY_OF_MONTH, day);
	}
}
