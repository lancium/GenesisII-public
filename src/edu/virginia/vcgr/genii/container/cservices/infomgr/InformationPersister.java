package edu.virginia.vcgr.genii.container.cservices.infomgr;

/**
 * This interface is used to indicate an object which can persist and retrieve
 * "cached" values for information.
 * 
 * @author mmm2a
 *
 * @param <InformationType>
 */
public interface InformationPersister<InformationType>
{
	/**
	 * Get information stored about the given endpoint.
	 * 
	 * @param endpoint The endpoint to get stored information
	 * about.
	 * @return Any information stored with this persister.  This
	 * result CAN be null indicating that no information is
	 * currently stored by this persister.
	 */
	public InformationResult<InformationType> get(
		InformationEndpoint endpoint);
	
	/**
	 * Persist information about an endpoint into this persister.
	 * 
	 * @param endpoint The endpoint to store information about.  This
	 * parameter cannot be null.
	 * @param information THe information to store for the endpoint.  This
	 * information can be null, indicating that any stored information for
	 * the given endpoint should be removed.
	 */
	public void persist(InformationEndpoint endpoint,
		InformationResult<InformationType> information);
}