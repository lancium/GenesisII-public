package edu.virginia.vcgr.genii.container.cservices.besstatus;

import java.util.Map;

public interface BESStatusListener
{
	// statusMap is null if a timeout occurs.
	public void statusUpdate(Map<BESName, BESStatusInformation> statusMap);
}