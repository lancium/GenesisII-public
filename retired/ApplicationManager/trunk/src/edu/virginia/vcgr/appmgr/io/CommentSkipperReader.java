package edu.virginia.vcgr.appmgr.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class CommentSkipperReader extends BufferedReader
{
	public CommentSkipperReader(Reader reader)
	{
		super(reader);
	}

	public CommentSkipperReader(Reader reader, int sz)
	{
		super(reader, sz);
	}

	@Override
	public String readLine() throws IOException
	{
		String line = super.readLine();
		if (line == null)
			return null;

		if (line.length() == 0)
			return line;

		char lastChar = line.charAt(0);
		char nextChar;

		if (lastChar == '#')
			return "";

		for (int lcv = 1; lcv < line.length(); lcv++) {
			nextChar = line.charAt(lcv);
			if (nextChar == '#' && lastChar != '\\')
				return line.substring(0, lcv);
			lastChar = nextChar;
		}

		return line;
	}
}