package edu.virginia.g3.fsview;

import java.io.IOException;

public interface FSViewDirectoryEntry extends FSViewEntry {
	public FSViewEntry lookup(String name) throws IOException;

	public FSViewEntry[] listEntries() throws IOException;

	public FSViewFileEntry createFile(String name) throws IOException;

	public FSViewDirectoryEntry createDirectory(String name) throws IOException;

	public void delete(String name) throws IOException;
}