package edu.virginia.vcgr.genii.container.cservices.infomgr;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

/**
 * An interface that represents the ability to acquire information
 * about an endpoint.  Usually this will involve making a SOAP outcall
 * though that is not in fact required.
 * 
 * @author mmm2a
 *
 * @param <ResultType>
 */
public interface InformationResolver<ResultType>
{
	/**
	 * Acquire new up-to-date information about the given endpoint.
	 * 
	 * @param endpoint THe endpoint to acuire updated information
	 * for.
	 * @param timeout The amount of time to wait before giving
	 * up.
	 * @param timeoutUnits The units of time for the timeout value.
	 * @return Any information acquired.  This result CAN be null
	 * at the callers discretion.
	 * 
	 * @throws RemoteException
	 */
	public ResultType acquire(
		InformationEndpoint endpoint, long timeout, TimeUnit timeoutUnits) 
		throws RemoteException;
}