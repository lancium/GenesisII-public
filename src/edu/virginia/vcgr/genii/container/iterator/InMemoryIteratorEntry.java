package edu.virginia.vcgr.genii.container.iterator;

public class InMemoryIteratorEntry
{

	private String entryName;
	private String id;
	private boolean isExists;
	private FileOrDir fd;

	public InMemoryIteratorEntry(String entryName, String id, boolean isExists, FileOrDir fd)
	{
		this.entryName = entryName;
		this.id = id;
		this.isExists = isExists;
		this.fd = fd;
	}

	public String getEntryName()
	{
		return entryName;
	}

	public String getId()
	{
		return id;
	}

	public boolean isExistent()
	{
		return isExists;
	}

	public FileOrDir getType()
	{
		return fd;
	}

}
