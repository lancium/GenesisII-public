package edu.virginia.vcgr.genii.gjt.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ClassRelativeIOSource implements IOSource {
	private Class<?> _class;
	private String _resourceName;

	public ClassRelativeIOSource(Class<?> theClass, String resourceName) {
		_class = theClass;
		_resourceName = resourceName;
	}

	@Override
	public InputStream open() throws IOException {
		InputStream in = _class.getResourceAsStream(_resourceName);
		if (in == null)
			throw new FileNotFoundException(String.format(
					"Unable to load resource \"%s\" from class \"%s\".",
					_resourceName, _class));

		return in;
	}
}