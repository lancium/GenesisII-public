package edu.virginia.vcgr.genii.gjt.units;

public enum FriendlyTimeUnit {
	Seconds(1L), Minutes(60L), Hours(60L * 60L), Days(60L * 60L * 24L), Weeks(
			60L * 60L * 24L * 7L);

	private long _multiplier;

	private FriendlyTimeUnit(long multilpier) {
		_multiplier = multilpier;
	}

	final public long toSeconds(long value) {
		return value * _multiplier;
	}
}