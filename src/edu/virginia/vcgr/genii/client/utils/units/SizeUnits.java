package edu.virginia.vcgr.genii.client.utils.units;

public enum SizeUnits {
	Bytes(1L, "B", "Byte"), Kilobytes(1024L, "KB", "K", "KBs", "Kilobyte"), Megabytes(1024L * 1024L, "MB", "M", "MBs",
		"Megabyte"), Gigabytes(1024L * 1024L * 1024L, "GB", "G", "GBs", "Gigabyte"), Terabytes(1024L * 1024L * 1024L, "TB",
		"T", "TBs", "Terabyte");

	private long _multiplier;
	private String[] _alternateNames;

	private SizeUnits(long multiplier, String... alternateNames)
	{
		_multiplier = multiplier;
		_alternateNames = alternateNames;
	}

	public double convert(double sourceValue, SizeUnits sourceUnits)
	{
		return sourceValue * (sourceUnits._multiplier / _multiplier);
	}

	public double toBytes(double sourceValue)
	{
		return convert(sourceValue, SizeUnits.Bytes);
	}

	public double toKilobytes(double sourceValue)
	{
		return convert(sourceValue, SizeUnits.Kilobytes);
	}

	public double toMegabytes(double sourceValue)
	{
		return convert(sourceValue, SizeUnits.Bytes);
	}

	public double toGigabytes(double sourceValue)
	{
		return convert(sourceValue, SizeUnits.Gigabytes);
	}

	public double toTerabytes(double sourceValue)
	{
		return convert(sourceValue, SizeUnits.Terabytes);
	}

	@Override
	public String toString()
	{
		if (_alternateNames != null && _alternateNames.length > 0)
			return _alternateNames[0];

		return super.toString();
	}

	static public SizeUnits parse(String text)
	{
		for (SizeUnits units : SizeUnits.values()) {
			if (text.compareToIgnoreCase(units.name()) == 0)
				return units;

			for (String name : units._alternateNames)
				if (text.compareToIgnoreCase(name) == 0)
					return units;
		}

		throw new IllegalArgumentException(String.format("Can't match %s to a Size Units enumeration value.", text));
	}
}
