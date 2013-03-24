package edu.virginia.vcgr.genii.container.exportdir.lightweight.svn;

import org.tmatesoft.svn.core.SVNException;

import edu.virginia.vcgr.genii.client.cache.LRUCache;

class SVNExportEntryCache extends LRUCache<SVNPathIdentifier, SVNExportEntry>
{
	static final long serialVersionUID = 0L;

	SVNExportEntryCache(int maxSize)
	{
		super(maxSize);
	}

	public SVNExportEntry lookup(Object key) throws SVNException
	{
		SVNPathIdentifier id = (SVNPathIdentifier) key;
		SVNExportEntry entry = get(id);
		if (entry == null) {
			entry = new SVNExportEntry(id.repository(), id.repositoryURL(), id.revision(), id.repository().info(
				id.relativePath(), id.revision()), id.relativePath(), this);
			put(id, entry);
		}

		return entry;
	}
}