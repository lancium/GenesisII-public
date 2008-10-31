package edu.virginia.vcgr.genii.client.postlog.empty;

import edu.virginia.vcgr.genii.client.postlog.PostEvent;
import edu.virginia.vcgr.genii.client.postlog.PostTarget;

public class EmptyPostTarget implements PostTarget
{
	@Override
	public void post(PostEvent event)
	{
		// We just swallow the event.
	}
}