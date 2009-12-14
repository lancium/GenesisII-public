package edu.virginia.vcgr.genii.client.nativeq.execution;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.client.nativeq.ScriptExecutionException;

public class ParsingExecutionEngine
{
	static public Map<Pattern, List<Matcher>> executeAndParse(
		ProcessBuilder builder, Pattern...patterns) throws NativeQueueException
	{
		StringBuilderProcessStreamSink errSink =
			new StringBuilderProcessStreamSink();
		Map<Pattern, List<Matcher>> ret = 
			new HashMap<Pattern, List<Matcher>>(patterns.length);
		
		for (Pattern pattern : patterns)
			ret.put(pattern, new LinkedList<Matcher>());
		
		try
		{
			Process proc = builder.start();
			StreamUtils.close(proc.getOutputStream());
			
			ActiveStreamCopier errCopier = new ActiveStreamCopier(
				proc.getErrorStream(), errSink);
			ActiveStreamCopier outCopier = new ActiveStreamCopier(proc.getInputStream(), 
				new PatternParserProcessStreamSink(ret));
			
			int result = proc.waitFor();
			
			try { errCopier.join(); } catch (Throwable cause) {}
			try { outCopier.join(); } catch (Throwable cause) {}
			
			if (result != 0)
				throw new ScriptExecutionException(builder.command(), result,
					"Executable exited with non-zero exit code.");
		}
		catch (InterruptedException ie)
		{
			throw new NativeQueueException(String.format(
				"Unable to execute command \"%s\".", builder.command()), ie);
		}
		catch (IOException ioe)
		{
			throw new NativeQueueException(String.format(
				"Unable to execute command \"%s\".", builder.command()), ioe);
		}
		
		return ret;
	}
}