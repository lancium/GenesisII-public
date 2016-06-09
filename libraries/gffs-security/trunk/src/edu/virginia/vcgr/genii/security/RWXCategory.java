package edu.virginia.vcgr.genii.security;

import java.util.EnumSet;

public enum RWXCategory {
	INHERITED,
	READ,
	WRITE,
	EXECUTE,
	OPEN,
	CLOSED,
	APPEND,
	OWNER;

	// a constant representing all rwx capabilities.
	public final static EnumSet<RWXCategory> FULL_ACCESS = EnumSet.of(RWXCategory.READ, RWXCategory.WRITE, RWXCategory.EXECUTE);

	public static char convertToChar(RWXCategory cat)
	{
		char c = '?';
		switch (cat) {
			case READ:
				c = 'r';
				break;
			case WRITE:
				c = 'w';
				break;
			case EXECUTE:
				c = 'x';
				break;
			case APPEND:
				c = 'a';
				break;
			case OWNER:
				c = 'o';
				break;
			case OPEN:
				c = '*';
				break;
			case CLOSED:
				c = 'c';
				break;
			case INHERITED:
				c = 'i';
				break;
		}
		return c;
	}

	public static RWXCategory getMatchingCategory(String categoryName)
	{

		if (categoryName == null)
			return null;

		if ("INHERITED".equalsIgnoreCase(categoryName)) {
			return INHERITED;
		} else if ("READ".equalsIgnoreCase(categoryName)) {
			return READ;
		} else if ("WRITE".equalsIgnoreCase(categoryName)) {
			return WRITE;
		} else if ("EXECUTE".equalsIgnoreCase(categoryName)) {
			return EXECUTE;
		} else if ("OPEN".equalsIgnoreCase(categoryName)) {
			return OPEN;
		} else if ("CLOSED".equalsIgnoreCase(categoryName)) {
			return CLOSED;
		} else if ("APPEND".equalsIgnoreCase(categoryName)) {
			return APPEND;
		} else if ("OWNER".equalsIgnoreCase(categoryName)) {
			return OWNER;
		}
		return null;
	}
}