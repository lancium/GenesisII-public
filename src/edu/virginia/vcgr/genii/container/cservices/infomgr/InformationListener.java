package edu.virginia.vcgr.genii.container.cservices.infomgr;

/**
 * This interface is used by callers who wish to be asynchronously notified when information about
 * an endpoint becomes available.
 * 
 * @author mmm2a
 * 
 * @param <InformationType>
 */
public interface InformationListener<InformationType>
{
	/**
	 * Receive updated information about an endpoint that the listener was waiting for.
	 * 
	 * @param endpoint
	 *            The endpoint that the information pertains to.
	 * @param information
	 *            The information about the endpoint. This parameter is guaranteed never to be null,
	 *            even when no information could be obtained. The Information Manager will always
	 *            fill in information, even if it is stale, when available but will also indicate
	 *            that the information is stale by setting the exception value in the result.
	 */
	public void informationUpdated(InformationEndpoint endpoint, InformationResult<InformationType> information);
}