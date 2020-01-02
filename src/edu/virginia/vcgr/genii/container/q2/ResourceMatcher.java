package edu.virginia.vcgr.genii.container.q2;

import edu.virginia.vcgr.genii.container.q2.besinfo.BESInformation;
import edu.virginia.vcgr.genii.container.q2.matching.JobResourceRequirements;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class matches job id's to bes id's. It's used during the scheduling phase.
 * 
 * @author mmm2a
 */
public class ResourceMatcher
{
	static private Log _logger = LogFactory.getLog(ResourceMatcher.class);
	/**
	 * This operation indicates whether or not the given job can be run on the indicated bes container.
	 * 
	 * @param jobID
	 *            The job to match against.
	 * @param besID
	 *            The bes to match against.
	 * @return
	 */
	public boolean matches(JobResourceRequirements req, BESInformation besInfo)
	{
		boolean matched = req.matches(besInfo);
		return matched;
	}
}
