package edu.virginia.vcgr.genii.text;

import java.util.Random;

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
	 * converts just one byte to a hexadecimal equivalent.
	 */
	public static String byteToHex(byte toHex)
	{
		int v = toHex & 0xFF;
		return new String("" + hexadecimalDigits[v >>> 4] + hexadecimalDigits[v & 0x0F]);		
	}
	
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

	/**
	 * creates a random string of the requested length, using the character set provided.
	 */
	public static String randomString(Random rng, String characters, int length)
	{
		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(rng.nextInt(characters.length()));
		}
		return new String(text);
	}

	static String passwdSourceChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-+=_:;<>?/~!@#$%^&*";

	/**
	 * creates a random string of numbers, letters in both cases, and a few choice special characters.
	 */
	public static String randomPasswordString(Random rng, int length)
	{
		return randomString(rng, passwdSourceChars, length);
	}

	public static int bytesPerLine = 16;

	public static String dumpByteArray(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i += bytesPerLine) {
			StringBuilder index = new StringBuilder();
			index.append(Integer.toHexString(i));
			while (index.length() < 4) {
				index.insert(0, "0");
			}
			sb.append(index);
			sb.append(": ");

			for (int j = i; j < bytes.length && j < i + bytesPerLine; j++) {
				sb.append(byteToHex(bytes[j]));
				// don't put a space after the last byte.
				if (j < i + bytesPerLine - 1)
					sb.append(" ");
			}

			sb.append("\n");
		}
		return sb.toString();
	}

	static public void main(String[] args) throws Throwable
	{
		byte[] bytes =
			{ 0x12, 0x23, (byte) 0xad, (byte) 0xff, 0x4f, 0x59, (byte) 0x8c, (byte) 0x94, 0x69, 0x6e, 0x63, 0x6c, 0x75, 0x64, 0x65, 0x20,
				0x76, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x73, 0x2e, 0x64, 0x65, 0x66, 0x0a, 0x0a, 0x50, 0x52, 0x4f, 0x4a, 0x45, 0x43,
				0x54, 0x20, 0x3d, 0x20, 0x66, 0x72, 0x65, 0x64, 0x73, 0x70, 0x61, 0x63, 0x65, 0x0a, 0x42, 0x55, 0x49, 0x4c, 0x44, 0x5f, 0x42,
				0x45, 0x46, 0x4f, 0x52, 0x45, 0x20, 0x3d, 0x20, 0x66, 0x65, 0x69, 0x73, 0x74, 0x79 };

		System.out.println("here is the dumped form of our example array:");
		System.out.println(dumpByteArray(bytes));
	}
}
