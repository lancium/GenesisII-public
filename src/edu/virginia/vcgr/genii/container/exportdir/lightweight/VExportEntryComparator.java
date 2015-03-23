package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.util.Comparator;

/**
 * helpful comparison object for sorting lists of VExportEntry
 */
public class VExportEntryComparator
{
	public VExportEntryComparator()
	{
	}

	static public Comparator<VExportEntry> getComparator()
	{
		return new Comparator<VExportEntry>()
		{
			@Override
			public int compare(VExportEntry c1, VExportEntry c2)
			{
				if ((c1 == null) && (c2 == null))
					return 0;
				if (c1 == null)
					return -1;
				if (c2 == null)
					return 1;
				return c1.getName().compareTo(c2.getName());
			}
		};
	}
}
