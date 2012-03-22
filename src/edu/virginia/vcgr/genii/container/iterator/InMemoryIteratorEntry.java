package edu.virginia.vcgr.genii.container.iterator;

public class InMemoryIteratorEntry {
	
	private String entryName;
	private String id;
	private boolean isExists;
	
	public InMemoryIteratorEntry(String entryName, String id, boolean isExists)
	{
		this.entryName = entryName;
		this.id = id;
		this.isExists = isExists;
	}

	public String getEntryName() {
		return entryName;
	}

	public String getId() {
		return id;
	}

	public boolean isExistent() {
		return isExists;
	}
	
	

}
