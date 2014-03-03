package edu.virginia.g3.fsview;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FSViewFileEntry extends FSViewEntry
{
	public FSViewFileEntryType fileType();

	public Long size();

	public InputStream openInputStream() throws IOException;

	public OutputStream openOutputStream() throws IOException;
}