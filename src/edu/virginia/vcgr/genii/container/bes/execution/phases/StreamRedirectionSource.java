package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public interface StreamRedirectionSource extends Serializable
{
	public InputStream openSource(ExecutionContext context) 
		throws IOException;
}