package edu.virginia.vcgr.genii.client.nativeq;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The ScriptBasedQueueConnection class provides mechanisms for handling
 * queue'ing systems that take scripts to run, and where the interactions
 * with the queing system are based off of "fork/exec"'ing commands to the
 * OS (like qsub, qdel, qstat, etc.).  This interface is used bye clients that
 * with to parse the output of such a fork/exec'd command and parse it.
 * 
 * @author mmm2a
 */
public interface ScriptLineParser
{
	/**
	 * Retrieve the list of regular expressions that this particular parser
	 * can handle.
	 * 
	 * @return The list of regular expressions that this parser can handle.
	 */
	public Pattern[] getHandledPatterns();
	
	/**
	 * Given that a match has been found to one of the patterns indicated,
	 * parse that matcher's line.
	 * 
	 * @param matcher The matcher that matches a line of output.
	 * 
	 * @throws NativeQueueException
	 */
	public void parseLine(Matcher matcher) throws NativeQueueException;
}