package edu.virginia.vcgr.smb.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class SMBSearchState
{
	public class Entry
	{
		// Need this for . and ..
		private String name;
		private RNSPath path;

		public Entry(String name, RNSPath path)
		{
			this.name = name;
			this.path = path;
		}

		public String getName()
		{
			return name;
		}

		public RNSPath getPath()
		{
			return path;
		}
	}

	private List<Entry> listing;
	private int resumeKey;
	// Whether the client is requesting resume keys
	private boolean resume;

	public SMBSearchState(RNSPath dot, RNSPath dotdot, Collection<RNSPath> listing)
	{
		this.listing = new ArrayList<Entry>(listing.size());
		if (dot != null)
			this.listing.add(new Entry(".", dot));

		if (dotdot != null)
			this.listing.add(new Entry("..", dotdot));

		for (RNSPath path : listing)
			this.listing.add(new Entry(path.getName(), path));

		this.resumeKey = 0;
		this.resume = false;
	}

	public void reset(int resumeKey)
	{
		this.resumeKey = resumeKey;
	}

	public void reset(String fileName) throws SMBException
	{
		SMBWildcard pattern = new SMBWildcard(fileName);

		// Try a backward search first; usually it will be the first element back
		for (int i = this.resumeKey - 1; i >= 0; i--) {
			Entry e = listing.get(i);
			if (pattern.matches(e.getName())) {
				this.resumeKey = i + 1;
				return;
			}
		}

		for (int i = this.resumeKey; i < listing.size(); i++) {
			Entry e = listing.get(i);
			if (pattern.matches(e.getName())) {
				this.resumeKey = i + 1;
				return;
			}
		}

		throw new SMBException(NTStatus.NO_SUCH_FILE);
	}

	public Entry next()
	{
		return listing.get(resumeKey++);
	}

	public boolean hasNext()
	{
		return resumeKey < listing.size();
	}

	public int genResumeKey()
	{
		return resumeKey;
	}

	public boolean getResume()
	{
		return resume;
	}

	public void setResume(boolean resume)
	{
		this.resume = resume;
	}
}