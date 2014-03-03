package edu.virginia.vcgr.genii.gjt.data;

public interface Describer<Type> {
	public int maximumVerbosity();

	public String describe(Type type, int verbosity);
}