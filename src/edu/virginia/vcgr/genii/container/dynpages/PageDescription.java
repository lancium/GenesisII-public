package edu.virginia.vcgr.genii.container.dynpages;

import java.io.IOException;

public interface PageDescription
{
	public DynamicPage loadPage() throws IOException;
}