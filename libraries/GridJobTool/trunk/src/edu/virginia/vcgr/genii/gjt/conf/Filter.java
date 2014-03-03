package edu.virginia.vcgr.genii.gjt.conf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.virginia.vcgr.genii.gjt.util.ClassRelativeIOSource;
import edu.virginia.vcgr.genii.gjt.util.FileIOSource;
import edu.virginia.vcgr.genii.gjt.util.IOSource;
import edu.virginia.vcgr.genii.gjt.util.IOUtils;
import edu.virginia.vcgr.genii.gjt.util.OverridenIOSource;

public class Filter<E extends Enum<E>> {
	static private Logger _logger = Logger.getLogger(Filter.class);

	static private final String FILENAME_FORMAT = "%s-filter.dat";

	static private String getShortName(Class<?> theClass) {
		String className = theClass.getName();
		int lastIndex = className.lastIndexOf('.');
		if (lastIndex >= 0)
			className = className.substring(lastIndex + 1);

		return className;
	}

	private void addFilterElement(Class<E> filterClass, EnumSet<E> filterSet,
			String line, int lineno) {
		try {
			E enumeration = Enum.valueOf(filterClass, line);
			filterSet.add(enumeration);
		} catch (IllegalArgumentException iae) {
			_logger.error(
					String.format(
							"The name %s at line %d does not exist in the enumeration.",
							line, lineno), iae);
		}
	}

	private Set<E> _filterSet;

	Filter(File configurationDirectory, Class<E> filterClass)
			throws IOException {
		int lineno = 0;
		EnumSet<E> filterSet;
		String filename = String.format(FILENAME_FORMAT,
				getShortName(filterClass));
		IOSource source = new OverridenIOSource(new FileIOSource(new File(
				configurationDirectory, filename)), new ClassRelativeIOSource(
				Filter.class, filename));
		String line;

		InputStream in = null;
		BufferedReader reader = null;
		try {
			in = source.open();
			reader = new BufferedReader(new InputStreamReader(in));
			filterSet = EnumSet.noneOf(filterClass);

			while ((line = reader.readLine()) != null) {
				lineno++;
				line = line.trim();
				if (line.length() > 0)
					addFilterElement(filterClass, filterSet, line, lineno);
			}
		} catch (FileNotFoundException fnfe) {
			filterSet = EnumSet.allOf(filterClass);
		} finally {
			IOUtils.close(reader);
			IOUtils.close(in);
		}

		_filterSet = filterSet;
	}

	final public boolean contains(E value) {
		return _filterSet.contains(value);
	}
}