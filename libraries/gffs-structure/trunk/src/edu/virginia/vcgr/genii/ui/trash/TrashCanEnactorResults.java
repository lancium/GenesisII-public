package edu.virginia.vcgr.genii.ui.trash;

import java.util.Collection;

import org.morgan.util.Pair;

import edu.virginia.vcgr.genii.ui.persist.PersistenceKey;

class TrashCanEnactorResults
{
	private Collection<Pair<String, PersistenceKey>> _unsuccessfulResults;

	TrashCanEnactorResults(Collection<Pair<String, PersistenceKey>> unsuccessfulResults)
	{
		_unsuccessfulResults = unsuccessfulResults;
	}

	Collection<Pair<String, PersistenceKey>> unsuccessfulResults()
	{
		return _unsuccessfulResults;
	}
}