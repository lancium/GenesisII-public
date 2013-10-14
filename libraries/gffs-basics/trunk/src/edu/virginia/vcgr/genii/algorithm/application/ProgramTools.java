package edu.virginia.vcgr.genii.algorithm.application;

/**
 * Some utility functions for getting information about the running application.
 * 
 * @author Chris Koeritz
 */
public class ProgramTools
{
	/**
	 * produces a list of the stack for a certain number of its elements, called stack frames. this
	 * will ignore the fact that this function is invoked, and start counting stack frames from the
	 * immediate caller's perspective (including it).
	 */
	public static String showLastFewOnStack(int howManyFrames)
	{
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		StringBuilder toReturn = new StringBuilder();
		// 0 skips getStackTrace, and 1 skips this function.
		for (int i = 0; i < Math.min(howManyFrames, elements.length); i++) {
			// /2; i < Math.min(howManyFrames + 2, elements.length); i++) {
			if (toReturn.length() != 0) {
				toReturn.append(" <- ");
			}
			toReturn.append(getStackFrame(i));
		}
		return toReturn.toString();
	}

	/**
	 * returns the n-th frame back from this function. 1 will be the caller of this function, 2 will
	 * be the caller of that caller, etc.
	 */
	public static String getStackFrame(int howManyBack)
	{
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		return elements[howManyBack + 2].toString();
	}

}
