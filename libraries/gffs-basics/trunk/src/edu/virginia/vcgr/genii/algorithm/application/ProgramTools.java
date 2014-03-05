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
		/*
		 * don't start at the very first frame; we want to skip backwards to the direct caller of
		 * this function.
		 */
		int startFrame = 3;
		int endFrame = Math.min(howManyFrames + 3, elements.length - 1);
		for (int i = startFrame; i < endFrame; i++) {
			if (toReturn.length() != 0) {
				toReturn.append(" <- ");
			}
			toReturn.append(getStackFrame(i));
		}
		return toReturn.toString();
	}

	/**
	 * returns the Nth frame backwards starting from this function. 0 is this method, 1 is the
	 * invoker, 2 is the invoker's invoker, etc.
	 */
	public static String getStackFrame(int which)
	{
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		/* a little self-protection to avoid accessing missing parts of the array. */
		if (which >= elements.length)
			which = elements.length - 1;
		return elements[which].toString();
	}

}
