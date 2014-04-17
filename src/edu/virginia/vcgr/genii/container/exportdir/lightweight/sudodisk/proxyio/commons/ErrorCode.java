package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons;

public class ErrorCode
{

	public static final byte SUCCESS_CODE = 0x00;
	public static final byte BAD_PATH_CODE = 0x01;
	public static final byte FNF_CODE = 0x02;
	public static final byte IO_ERROR_CODE = 0x03;
	public static final byte DELETE_FAIL_CODE = 0x04;
	public static final byte ATTR_FETCH_FAIL_CODE = 0x05;
	public static final byte MKDIR_FAIL_CODE = 0x06;
	public static final byte DIR_ALREADY_EXISTS_CODE = 0x07;
	public static final byte INVALID_CMD_CODE = 0x08;
	public static final byte NW_READ_ERROR_CODE = 0x09;
	public static final byte INVALID_REQ_ERROR_CODE = 0x0a;
	public static final byte LL_NOT_DIR_ERROR_CODE = 0x0b;
	public static final byte LL_FAIL_ERROR_CODE = 0x0c;
	public static final byte FILE_ALREADY_EXISTS_CODE = 0x0d;
	public static final byte CREATE_FAIL_CODE = 0x0e;
	public static final byte CANNOT_READ_CODE = 0x0f;
	public static final byte CANNOT_WRITE_CODE = 0x10;
	public static final byte NOT_DIR_CODE = 0x11;

	public static String getErrorMsgFromErrorCode(byte code)
	{
		switch (code) {
			case 0x00:
				return "Success";
			case 0x01:
				return "Bad path - possibly null";
			case 0x02:
				return "File/Dir Not Found";
			case 0x03:
				return "An I/O Error Occurred during the operation";
			case 0x04:
				return "Unable to delete file";
			case 0x05:
				return "Unable to fetch attributes";
			case 0x06:
				return "Unable to create directory";
			case 0x07:
				return "The directory already exists";
			case 0x08:
				return "Invalid cmd";
			case 0x09:
				return "Network request read error by server";
			case 0x0a:
				return "Bad request";
			case 0x0b:
				return "Dir operation requested on file";
			case 0x0c:
				return "Unable to list files - possible ACL error";
			case 0x0d:
				return "The file already exists";
			case 0x0e:
				return "Unable to create file";
			case 0x0f:
				return "Cannot read file/dir";
			case 0x10:
				return "No write permission for file/dir";
			case 0x11:
				return "The given file system path isn't a directory";
			default:
				return "Unable to understand error code";
		}
	}
}
