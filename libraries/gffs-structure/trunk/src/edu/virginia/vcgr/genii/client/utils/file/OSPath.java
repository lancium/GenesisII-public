package edu.virginia.vcgr.genii.client.utils.file;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class OSPath implements Iterable<File>
{
	private Collection<File> _paths = new LinkedList<File>();

	public OSPath(String envVariable, String pathSeparator)
	{
		String value = System.getenv(envVariable);
		if (value != null) {
			String[] values = value.split(Pattern.quote(pathSeparator));
			if (values != null) {
				for (String element : values) {
					if (element != null) {
						element.trim();
						if (element.length() > 0)
							_paths.add(new File(element));
					}
				}
			}
		}
	}

	public OSPath(String envVariable)
	{
		this(envVariable, File.pathSeparator);
	}

	@Override
	final public Iterator<File> iterator()
	{
		return _paths.iterator();
	}

	static public Iterable<File> osPath()
	{
		return new OSPath("PATH");
	}

	static public Iterable<File> osLibraryPath()
	{
		return new OSPath("LD_LIBRARY_PATH");
	}
}