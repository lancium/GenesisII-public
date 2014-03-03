package edu.virginia.vcgr.genii.gjt.data.fs.grid;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import edu.virginia.vcgr.genii.gjt.data.fs.AbstractFilesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.jsdl.FileSystem;
import edu.virginia.vcgr.jsdl.FileSystemType;

public class GridFilesystem extends AbstractFilesystem implements
		Externalizable {
	static public final String COMMON_NAME = "Grid Mount";

	static public final String JSDL_NAME = "GRID";
	static public final String DESCRIPTION = "The grid namespace mounted as a file system.";

	static public final boolean CAN_EDIT = false;

	public GridFilesystem() {
		super(FilesystemType.Grid);
	}

	@Override
	public String toString() {
		return String.format("%s", filesystemType().toString());
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof GridFilesystem)
			return true;

		return false;
	}

	@Override
	public Object clone() {
		return new GridFilesystem();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

	@Override
	public FileSystem toJSDLFilesystem() {
		FileSystem ret = new FileSystem("GRID", FileSystemType.normal);

		return ret;
	}
}