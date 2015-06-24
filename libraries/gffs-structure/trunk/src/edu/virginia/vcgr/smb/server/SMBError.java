package edu.virginia.vcgr.smb.server;

public class SMBError
{
	public static int SUCCESS = 0;
	public static int ERRbadfun = (1 << 24) | 1;
	public static int ERRbadfile = (1 << 24) | 2;
	public static int ERRbadpath = (1 << 24) | 3;
	public static int ERRnofids = (1 << 24) | 4;
	public static int ERRnoaccess = (1 << 24) | 5;
	public static int ERRbadfid = (1 << 24) | 6;

	// public static int encode(int klass, int code) {
	// return (klass << 24) | code;
	// }
}
