package edu.virginia.vcgr.smb.server;

import java.util.Date;

public class FileTime
{
	private long time;

	private FileTime(long time)
	{
		this.time = time;
	}

	public void encode(SMBBuffer acc)
	{
		acc.putLong(time);
	}

	public static FileTime decode(SMBBuffer acc)
	{
		return new FileTime(acc.getLong());
	}

	public static FileTime fromMillis(long millis)
	{
		// There are 11644473600000 milliseconds between 1600 and 1970
		long millisWin = millis + 11644473600000L;
		// 100 ns units
		long time = millisWin * 10000;

		return new FileTime(time);
	}

	public static FileTime fromDate(Date create)
	{
		return fromMillis(create.getTime());
	}

	public long toMillis()
	{
		long millisWin = time / 10000;
		return millisWin - 11644473600000L;
	}

	public boolean isZero()
	{
		return time == 0;
	}

	public boolean isMax()
	{
		return time == -1;
	}
}
