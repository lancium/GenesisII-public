package edu.virginia.vcgr.genii.container.resource;

import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;

/**
 * Resource Key Translaters handle the job of translating from internal key
 * representations to WS-Addressing ReferenceParameterTypes and back.
 * 
 * @author Mark Morgan (mmm2a@cs.virginia.edu)
 */
public interface IResourceKeyTranslater
{
	/**
	 * Unwrap the internal key representation from the reference parameters.
	 * 
	 * @param targetEPR The ReferenceParameters container to unwrap.  This parameter
	 * may not be null, but the contents may be null indicating a service resource.
	 * @return The internal key represented by this addressing information.
	 * @throws ResourceUnknownFaultType Because one can always interpret the
	 * inability to decipher reference parameters as an inability to recognize
	 * a resource, we only throw the ResourceUnknownFaultType here.
	 */
	public Object unwrap(ReferenceParametersType targetEPR)
		throws ResourceUnknownFaultType;
	
	/**
	 * Wrap up the internal key representation for a resource into a 
	 * WS-Addressing ReferenceParametersType structure container structure.
	 * 
	 * @param key The internal key representation.  This value can be null
	 * indicating that it's the service resource (in which case, the
	 * ReferenceParametersType should be null as well).
	 * @return WS-Addressing ReferenceParametersType block.
	 * @throws ResourceException If anything goes wrong.
	 */
	public ReferenceParametersType wrap(Object key) throws ResourceException;
}
