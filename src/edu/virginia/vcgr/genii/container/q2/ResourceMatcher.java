package edu.virginia.vcgr.genii.container.q2;

/**
 * This class matches job id's to bes id's.  It's used during the
 * scheduling phase.
 * 
 * @author mmm2a
 */
public class ResourceMatcher
{
	/**
	 * This operation indicates whether or not the given job can be
	 * run on the indicated bes container.  I have grand visions of this
	 * function one day actually matching things like OS, architecture, etc.
	 * Until that day, it simply returns true indicating that all combinations
	 * match.
	 * 
	 * @param jobID The job to match against.
	 * @param besID The bes to match against.
	 * @return
	 */
	public boolean matches(Long jobID, Long besID)
	{
		return true;
	}
}