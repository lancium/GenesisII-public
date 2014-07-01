package edu.virginia.vcgr.genii.container.exportdir;

import java.util.ArrayList;
import java.util.Arrays;

public class GridMapUserList extends ArrayList<String>
{
	private static final long serialVersionUID = 1L;

	GridMapUserList()
	{
	}

	GridMapUserList(String[] names)
	{
		this.addAll(Arrays.asList(names));
	}
}
