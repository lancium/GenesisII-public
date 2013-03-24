package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;

public interface VExportFile extends VExportEntry
{
	public void read(long offset, ByteBuffer target) throws IOException;

	public void write(long offset, ByteBuffer source) throws IOException;

	public void truncAppend(long offset, ByteBuffer source) throws IOException;

	public long size() throws IOException;

	public boolean readable() throws IOException;

	public boolean writable() throws IOException;

	public Calendar modificationTime() throws IOException;

	public Calendar accessTime() throws IOException;

	public Calendar createTime() throws IOException;

	public void modificationTime(Calendar c) throws IOException;

	public void accessTime(Calendar c) throws IOException;
}