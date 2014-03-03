package edu.virginia.g3.fsview;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface FSViewRandomAccessFileEntry extends FSViewFileEntry {
	public void truncate(long newLength) throws IOException;

	public void append(ByteBuffer content) throws IOException;

	public void read(long offset, ByteBuffer sink) throws IOException;

	public void write(long offset, ByteBuffer source) throws IOException;
}