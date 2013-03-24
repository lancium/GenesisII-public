package edu.virginia.vcgr.genii.client.gpath;

public enum GeniiPathType {
	Grid("grid", "rns"), Local("local", "file");

	private String[] _protocols;

	private GeniiPathType(String... protocols)
	{
		if (protocols.length == 0)
			throw new IllegalArgumentException("Must have at least one protocol for GeniiPathType enum.");

		_protocols = protocols;
	}

	public String[] protocols()
	{
		return _protocols;
	}

	@Override
	public String toString()
	{
		return _protocols[0];
	}
}