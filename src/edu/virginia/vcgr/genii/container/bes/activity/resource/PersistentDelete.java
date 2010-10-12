package edu.virginia.vcgr.genii.container.bes.activity.resource;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.io.FileSystemUtils;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.percall.ExponentialBackoffScheduler;
import edu.virginia.vcgr.genii.container.cservices.percall.OutcallActor;
import edu.virginia.vcgr.genii.container.cservices.percall.PersistentOutcallContainerService;

public class PersistentDelete
{
	static private class PersistentDeleteActor implements OutcallActor
	{
		static final long serialVersionUID = 0L;
		
		private File _path;
		
		private PersistentDeleteActor(File path)
		{
			_path = path;
		}
		
		@Override
		final public boolean enactOutcall(ICallingContext callingContext,
			EndpointReferenceType target) throws Throwable
		{
			if (!FileSystemUtils.recursiveDelete(_path, false))
				return false;
			
			File path = new File(_path.getAbsolutePath());
			if (path.exists())
				return false;
			
			return true;
		}
	}
	
	static public void persistentDelete(File dir)
	{
		PersistentOutcallContainerService service = 
			ContainerServices.findService(
				PersistentOutcallContainerService.class);
		service.schedule(new PersistentDeleteActor(dir), 
			new ExponentialBackoffScheduler(
				30L, TimeUnit.DAYS, null, 8, 1L, TimeUnit.MINUTES,
				15L, TimeUnit.SECONDS), null, null);
	}
}