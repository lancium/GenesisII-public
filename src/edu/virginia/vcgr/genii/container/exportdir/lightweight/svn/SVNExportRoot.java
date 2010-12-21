package edu.virginia.vcgr.genii.container.exportdir.lightweight.svn;

import java.io.IOException;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.AbstractVExportRoot;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportEntry;

public class SVNExportRoot extends AbstractVExportRoot
{
	static
	{
		SVNRepositoryFactoryImpl.setup();
		DAVRepositoryFactory.setup();
		FSRepositoryFactory.setup();
	}
	
	static final private int SVN_CACHE_SIZE = 256;

	static private SVNExportEntryCache _entryCache = new SVNExportEntryCache(
		SVN_CACHE_SIZE);
	
	private SVNRepository _repository;
	private String _svnURL;
	private long _revision;
	
	public SVNExportRoot(
		String svnURL, String svnUser, String svnPass, Long svnRevision)
			throws IOException
	{
		_svnURL = svnURL;
		try
		{
			if (svnUser != null && svnPass == null)
				svnPass = "";
			
			SVNClientManager mgr = SVNClientManager.newInstance(
				null, svnUser, svnPass);
			SVNURL url = SVNURL.parseURIEncoded(svnURL);
			_repository = mgr.createRepository(url, true);
			
			if (svnRevision == null)
				_revision = _repository.getLatestRevision();
			else
				_revision = svnRevision;
		}
		catch (SVNException e)
		{
			throw new IOException("Unable to connect to SVN repository.", e);
		}
	}
	
	@Override
	protected VExportEntry internalLookup(String normalizedPath)
		throws IOException
	{
		try
		{
			return _entryCache.lookup(new SVNPathIdentifier(
				_repository, _svnURL, normalizedPath, _revision));
		}
		catch (SVNException e)
		{
			throw new IOException(String.format(
				"Unable to locate svn path \"%s\".", normalizedPath), e);
		}
	}
}