package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public interface StreamRedirectionSink extends Serializable
{
	public OutputStream openSink(ExecutionContext context) throws IOException;
}
