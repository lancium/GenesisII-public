package edu.virginia.vcgr.appmgr.patch.builder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.appmgr.io.CommentSkipperReader;
import edu.virginia.vcgr.appmgr.io.IOUtils;

public class PatchRC
{
	static public final String IGNORES_SECTION_NAME = "auto-ignore";
	static public final String CAUTIONS_SECTION_NAME = "caution";

	static private final Pattern SECTION_HEADER = Pattern.compile("^\\[([^\\[\\]]+)\\]$");

	static private File getDefaultPatchRC()
	{
		String homeDir = System.getProperty("user.home");
		return new File(homeDir, ".patchrc");
	}

	static private String sanitizePath(String relativePath)
	{
		if (relativePath.startsWith("./"))
			return relativePath.substring(2);

		return relativePath;
	}

	private Set<String> _ignores = new HashSet<String>();
	private Set<String> _cautions = new HashSet<String>();

	public PatchRC(File patchRCPath) throws IOException
	{
		int lineno = 0;
		Set<String> currentSection = null;

		if (!patchRCPath.exists())
			return;

		FileReader reader = null;
		CommentSkipperReader in = new CommentSkipperReader(reader = new FileReader(patchRCPath));

		try {
			String line;

			while ((line = in.readLine()) != null) {
				lineno++;
				line = line.trim();
				if (line.length() == 0)
					continue;
				Matcher m = SECTION_HEADER.matcher(line);
				if (m.matches()) {
					String section = m.group(1);
					if (section.equals(IGNORES_SECTION_NAME))
						currentSection = _ignores;
					else if (section.equals(CAUTIONS_SECTION_NAME))
						currentSection = _cautions;
					else
						throw new IOException(String.format("Unrecognized patch rc section \"%s\".", section));
				} else {
					if (currentSection == null)
						throw new IOException(String.format("No section defined at line %d in patchrc file.\n", lineno));
					else
						currentSection.add(line);
				}
			}
		} finally {
			in.close();
			IOUtils.close(reader);
		}
	}

	public PatchRC() throws IOException
	{
		this(getDefaultPatchRC());
	}

	public Set<String> ignores()
	{
		return _ignores;
	}

	public boolean isIgnore(String relativePath)
	{
		return _ignores.contains(sanitizePath(relativePath));

	}

	public Set<String> cautions()
	{
		return _cautions;
	}

	public boolean isCaution(String relativePath)
	{
		return _cautions.contains(sanitizePath(relativePath));
	}
}