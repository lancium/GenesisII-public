package edu.virginia.vcgr.genii.container.cservices.infomgr;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

public interface InformationResolver<ResultType>
{
	public ResultType acquire(
		InformationEndpoint endpoint, long timeout, TimeUnit timeoutUnits) 
		throws RemoteException;
}