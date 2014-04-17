package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons;

public class Constants
{
	public static final int MAX_NUM_ARGS = 2;
	public static int MAX_WRITE_BUF_SIZE = 50 * 1024 * 1024; // 50MB!

	public static String FILE_READ_CMD = "r";
	public static String FILE_WRITE_CMD = "w";
	public static String FILE_TRUNCAPPEND_CMD = "a";
	public static String FILE_STAT_CMD = "s";
	public static String FILE_DELETE_CMD = "d";
	public static String FILE_CREAT_CMD = "c";
	public static String FILE_CAN_READ_CMD = "cr";
	public static String FILE_CAN_WRITE_CMD = "cw";
	public static String FILE_DOESEXIST_CMD = "de";

	public static String DIR_CREATE_CMD = "c";
	public static String DIR_DELETE_CMD = "d";
	public static String DIR_LISTLONG_CMD = "l";
	public static String DIR_CAN_READ_CMD = "cr";
	public static String DIR_CAN_WRITE_CMD = "cw";
	public static String DIR_ISDIR_CMD = "id";
	public static String DIR_DOESEXIST_CMD = "de";

	public final static byte FILE_READ_CMD_BYTE = 0x00;
	public final static byte FILE_WRITE_CMD_BYTE = 0x01;
	public final static byte FILE_TRUNCAPPEND_CMD_BYTE = 0x02;
	public final static byte FILE_STAT_CMD_BYTE = 0x04;
	public final static byte FILE_DELETE_CMD_BYTE = 0x08;
	public static final byte FILE_CREATE_CMD_BYTE = 0x10;
	public static final byte FILE_CAN_READ_CMD_BYTE = 0x20;
	public static final byte FILE_CAN_WRITE_CMD_BYTE = 0x40;
	public static final byte FILE_DOESEXIST_CMD_BYTE = 0x60;

	public final static byte DIR_CREATE_CMD_BYTE = 0x00;
	public final static byte DIR_DELETE_CMD_BYTE = 0x01;
	public final static byte DIR_LISTLONG_CMD_BYTE = 0x02;
	public static final byte DIR_CAN_READ_CMD_BYTE = 0x04;
	public static final byte DIR_CAN_WRITE_CMD_BYTE = 0x08;
	public static final byte DIR_ISDIR_CMD_BYTE = 0x10;
	public static final byte DIR_DOESEXIST_CMD_BYTE = 0x20;

	public static final byte FILE_OPN_BYTE = 0x00;
	public static final byte DIR_OPN_BYTE = 0x11;
	public static final int NONCE_SIZE = 16;

	public static int READ_ARGS_SIZE = 16; // 2 LONGS
	public static int WRITE_ARGS_SIZE = 8; // 1 LONG

	public static final String NO_RESPONSE_MSG = "No error msg from server";
	public static final String LARGE_RESPONSE_ERROR = "Server sending an error msg"
		+ " which is larger than what it said it would send";
	public static final String SMALL_RESPONSE_ERROR = "Server sent contents"
		+ " which is smaller in size than what it said it would send";

	public static final int READ_TIMEOUT_VAL = 60 * 1000; // 1min
}
