package edu.virginia.vcgr.genii.container.cservices.scratchmgr;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.jsdl.DirectoryBasedFileSystem;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;

public class ScratchFileSystem extends DirectoryBasedFileSystem
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(ScratchFileSystem.class);
	
	private long _reservationID;
	
	public ScratchFileSystem(File directory, long reservationID)
	{
		super(directory);
		
		_reservationID = reservationID;
	}
	
	@Override
	public String toString()
	{
		return String.format(
			"SwapFileSystem(directory = %s, reservationID = %d)",
			_directory, _reservationID);
	}
	
	@Override
	public void release()
	{
		try
		{
			ScratchFSManagerContainerService service =
				ContainerServices.findService(
					ScratchFSManagerContainerService.class);
			service.releaseReservation(this);
		}
		catch (Throwable cause)
		{
			_logger.error("Unable to release swap file system.", cause);
		}
	}
	
	long getReservationID()
	{
		return _reservationID;
	}
}