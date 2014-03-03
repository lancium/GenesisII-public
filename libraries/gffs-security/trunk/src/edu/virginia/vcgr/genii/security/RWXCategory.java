package edu.virginia.vcgr.genii.security;

import java.util.EnumSet;

public enum RWXCategory {
	INHERITED,
	READ,
	WRITE,
	EXECUTE,
	OPEN,
	CLOSED;

	// a constant representing all rwx capabilities.
	public final static EnumSet<RWXCategory> FULL_ACCESS = EnumSet.of(RWXCategory.READ, RWXCategory.WRITE, RWXCategory.EXECUTE);

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
		}
		return null;
	}
}