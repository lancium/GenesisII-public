package edu.virginia.vcgr.genii.container.exportdir.lightweight.svn;

import org.tmatesoft.svn.core.io.SVNRepository;

class SVNPathIdentifier
{
	private SVNRepository _repository;
	private String _svnRepositoryURL;
	private String _relativePath;
	private long _revision;
	
	SVNPathIdentifier(SVNRepository repository,
		String svnRepositoryURL, 
		String relativePath, long revision)
	{
		_repository = repository;
		_svnRepositoryURL = svnRepositoryURL;
		_relativePath = relativePath;
		_revision = revision;
	}
	
	final SVNRepository repository()
	{
		return _repository;
	}
	
	final String repositoryURL()
	{
		return _svnRepositoryURL;
	}
	
	final String relativePath()
	{
		return _relativePath;
	}
	
	final long revision()
	{
		return _revision;
	}
	
	@Override
	final public boolean equals(Object other)
	{
		if (other instanceof SVNPathIdentifier)
		{
			SVNPathIdentifier otherPath = (SVNPathIdentifier)other;
			return (_svnRepositoryURL.equals(otherPath._svnRepositoryURL) &&
				_relativePath.equals(otherPath._relativePath) &&
				_revision == otherPath._revision);
		} else
			return false;
	}
	
	@Override
	final public int hashCode()
	{
		return (int)(_svnRepositoryURL.hashCode() ^ 
			_relativePath.hashCode() ^ _revision);
	}
	
	@Override
	final public String toString()
	{
		return String.format("%s%s@%d",
			_svnRepositoryURL, 
			_relativePath.length() == 0 ? "" : "/" + _relativePath,
			_revision);
	}
}