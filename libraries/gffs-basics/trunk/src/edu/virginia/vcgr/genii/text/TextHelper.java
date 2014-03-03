package edu.virginia.vcgr.genii.text;

import java.util.Random;

public class TextHelper {
	/**
	 * returns a string with N spaces in it.
	 */
	static public String indentation(int indent) {
		StringBuilder toReturn = new StringBuilder();
		for (int i = 0; i < indent; i++)
			toReturn.append(" ");
		return toReturn.toString();
	}

	// used by bytesToHex.
	final protected static char[] hexadecimalDigits = "0123456789ABCDEF"
			.toCharArray();

	/**
	 * turns a byte array into a string of hexadecimal digits. thanks to:
	 * http://
	 * stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-
	 * string-in-java
	 */
	public static String bytesToHex(byte[] bytes) {
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
	 * creates a random string of the requested length, using the character set
	 * provided.
	 */
	public static String randomString(Random rng, String characters, int length) {
		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(rng.nextInt(characters.length()));
		}
		return new String(text);
	}

	static String passwdSourceChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-+=_:;<>?/~!@#$%^&*";

	/**
	 * creates a random string of numbers, letters in both cases, and a few
	 * choice special characters.
	 */
	public static String randomPasswordString(Random rng, int length) {
		return randomString(rng, passwdSourceChars, length);
	}
}
