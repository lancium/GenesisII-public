package org.morgan.dpage;

import java.io.IOException;

public interface PageDescription
{
	public DynamicPage loadPage() throws IOException;
}