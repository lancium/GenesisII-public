package edu.virginia.vcgr.genii.gjt.units;

public enum SizeUnit {
	Bytes(1L), Kilobytes(1024L), Megabytes(1024L * 1024L), Gigabytes(
			1024L * 1024L * 1024L), Terabytes(1024L * 1024L * 1024L * 1024L);

	private long _multiplier;

	private SizeUnit(long multiplier) {
		_multiplier = multiplier;
	}

	final public long toBytes(long value) {
		return value * _multiplier;
	}
}