package edu.virginia.vcgr.genii.client.nativeq.execution;

import java.io.IOException;

class StringBuilderProcessStreamSink implements ProcessStreamSink
{
	private StringBuilder _builder = new StringBuilder();
	
	@Override
	final public void addOutputLine(String outputLine) throws IOException
	{
		_builder.append(outputLine);
		_builder.append('\n');
	}
	
	@Override
	final public String toString()
	{
		return _builder.toString();
	}
}