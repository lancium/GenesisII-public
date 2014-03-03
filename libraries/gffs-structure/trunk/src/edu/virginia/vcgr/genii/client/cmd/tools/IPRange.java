package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.morgan.util.io.StreamUtils;

public class IPRange implements Iterable<String>
{
	static final private Pattern MASK_PATTERN = Pattern
		.compile("^\\s*(\\d{1,3}|\\*)\\.(\\d{1,3}|\\*)\\.(\\d{1,3}|\\*)\\.(\\d{1,3}|\\*)\\s*$");

	static private void generate(Collection<String> values, String[] groups, int start, Set<String> ignoreList)
	{
		if (start >= groups.length) {
			String value = String.format("%s.%s.%s.%s", groups[0], groups[1], groups[2], groups[3]);
			if (!ignoreList.contains(value))
				values.add(value);
		} else {
			if (groups[start].equals("*")) {
				for (int lcv = 0; lcv < 256; lcv++) {
					groups[start] = Integer.toString(lcv);
					generate(values, groups, start, ignoreList);
				}
			} else {
				generate(values, groups, start + 1, ignoreList);
			}
		}
	}

	static private Set<String> readIgnoreListFile(File file) throws IOException
	{
		FileReader reader = null;
		String line;
		Set<String> ret = new HashSet<String>();

		if (file == null)
			return ret;

		try {
			BufferedReader in = new BufferedReader(reader = new FileReader(file));
			while ((line = in.readLine()) != null) {
				int index = line.indexOf('#');
				if (index >= 0)
					line = line.substring(0, index);
				line = line.trim();
				if (line.length() > 0)
					ret.add(line);
			}

			return ret;
		} finally {
			StreamUtils.close(reader);
		}
	}

	private String _mask;
	private Set<String> _ignoreList;

	public IPRange(String mask, Set<String> ignoreList)
	{
		_mask = mask;
		_ignoreList = ignoreList;
	}

	public IPRange(String mask, File ignoreListFile) throws IOException
	{
		this(mask, readIgnoreListFile(ignoreListFile));
	}

	@Override
	public Iterator<String> iterator()
	{
		Matcher matcher = MASK_PATTERN.matcher(_mask);
		if (!matcher.matches())
			throw new RuntimeException(String.format("Mask \"%s\" is not valid.\n", _mask));

		Collection<String> values = new LinkedList<String>();
		String[] groups = new String[] { matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4) };

		generate(values, groups, 0, _ignoreList);
		return values.iterator();
	}
}