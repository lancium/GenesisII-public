package edu.virginia.vcgr.genii.client.nativeq.execution;

import java.io.IOException;

interface ProcessStreamSink
{
	public void addOutputLine(String outputLine) throws IOException;
}