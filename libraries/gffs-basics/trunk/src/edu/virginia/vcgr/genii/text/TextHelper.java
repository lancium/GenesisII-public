package edu.virginia.vcgr.genii.text;

public class TextHelper
{

	/**
	 * returns a string with N spaces in it.
	 */
	static public String indentation(int indent)
	{
		StringBuilder toReturn = new StringBuilder();
		for (int i = 0; i < indent; i++)
			toReturn.append(" ");
		return toReturn.toString();
	}

	// used by bytesToHex.
	final protected static char[] hexadecimalDigits = "0123456789ABCDEF".toCharArray();

	/**
	 * turns a byte array into a string of hexadecimal digits. thanks to:
	 * http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
	 */
	public static String bytesToHex(byte[] bytes)
	{
		char[] toReturn = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			toReturn[j * 2] = hexadecimalDigits[v >>> 4];
			toReturn[j * 2 + 1] = hexadecimalDigits[v & 0x0F];
		}
		return new String(toReturn);
	}

}
