package edu.virginia.vcgr.genii.container.resource;

/**
 * Resource providers allow for a loose binding between factories (and implicitly
 * resource types) and translaters that know how to deal with key translation.
 * This is important as it allows for us to more easily reuse different pieces of
 * code (for example, simple key translaters that understand string keys, etc.).
 * 
 * @author Mark Morgan (mmm2a@cs.virginia.edu)
 */
public interface IResourceProvider
{
	/**
	 * Retrieve the factory that can instantiate resource classes.
	 * 
	 * @return The providers resource factory.
	 */
	public IResourceFactory getFactory();
	
	/**
	 * Retrieve the key translater for this bundle.
	 * 
	 * @return The key translater.
	 */
	public IResourceKeyTranslater getTranslater();
	
	/**
	 * Register (on restart) with a vulture all of the
	 * resources which have scheduled termintation times.
	 * 
	 * @param serviceName The name of the service to register terminations for.
	 * @param vulture The vulture with which to register.
	 */
/*
	public void registerScheduledTermintations(String serviceName,
		LifetimeVulture vulture);
*/
}