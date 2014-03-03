package edu.virginia.vcgr.genii.container.deployer;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class DeploySnapshot implements Iterable<DeployFacet>
{
	private SortedSet<DeployFacet> _facets;

	static private class DeployFacetComparator implements Comparator<DeployFacet>
	{
		public int compare(DeployFacet arg0, DeployFacet arg1)
		{
			return arg0.getComponentID().compareTo(arg1.getComponentID());
		}
	}

	public DeploySnapshot(Collection<DeployFacet> facets)
	{
		_facets = new TreeSet<DeployFacet>(new DeployFacetComparator());

		_facets.addAll(facets);
	}

	public boolean equals(DeploySnapshot other)
	{
		if (_facets.size() != other._facets.size())
			return false;

		Iterator<DeployFacet> one = _facets.iterator();
		Iterator<DeployFacet> two = other._facets.iterator();

		while (one.hasNext()) {
			DeployFacet fOne = one.next();
			DeployFacet fTwo = two.next();

			if (!fOne.equals(fTwo))
				return false;
		}

		return true;
	}

	public boolean equals(Object other)
	{
		return equals((DeploySnapshot) other);
	}

	public int hashCode()
	{
		int result = 0;
		Iterator<DeployFacet> iter = _facets.iterator();

		while (iter.hasNext()) {
			result <<= 3;
			result ^= iter.next().hashCode();
		}

		return result;
	}

	public Iterator<DeployFacet> iterator()
	{
		return _facets.iterator();
	}
}