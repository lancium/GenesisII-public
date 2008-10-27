package edu.virginia.vcgr.genii.container.cservices;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public interface ContainerService
{
	public boolean started();
	public String serviceName();
	
	public void load(DatabaseConnectionPool connectionPool,
		ContainerServicesProperties cservicesProperties)
			throws Throwable;
	public void start() throws Throwable;
	
	public ContainerServicesProperties getContainerServicesProperties();
}