package edu.virginia.vcgr.genii.security;

/**
 * A base for objects that can describe their contents at various levels of
 * detail.
 */
public interface Describable {
	public String describe(VerbosityLevel verbosity);
}
