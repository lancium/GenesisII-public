package edu.virginia.vcgr.genii.ui.trash;

import org.morgan.util.Pair;

import edu.virginia.vcgr.genii.ui.persist.PersistenceKey;

public class TrashCanEntryWrapper
{
	private Pair<String, PersistenceKey> _pair;
		
	public TrashCanEntryWrapper(Pair<String, PersistenceKey> pair)
	{
		_pair = pair;
	}
	
	public Pair<String, PersistenceKey> pair()
	{
		return _pair;
	}
	
	@Override
	public String toString()
	{
		return _pair.first();
	}
}