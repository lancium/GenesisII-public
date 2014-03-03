package edu.virginia.vcgr.genii.gjt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileIOSource implements IOSource {
	private File _file;

	public FileIOSource(File file) {
		_file = file;
	}

	@Override
	public InputStream open() throws IOException {
		return new FileInputStream(_file);
	}
}