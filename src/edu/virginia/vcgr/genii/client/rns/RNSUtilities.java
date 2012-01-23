package edu.virginia.vcgr.genii.client.rns;

import java.io.FileNotFoundException;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.RNSMetadataType;
import org.ggf.rns.RNSSupportType;
import org.ggf.rns.SupportsRNSType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;

/**
 * This class is a collection of utilities that make looking up certain
 * things in the Genesis II system by path easier.
 * 
 * @author mmm2a
 */
public class RNSUtilities
{
	/**
	 * This is a complicated method that takes user supplied information
	 * and attempts to find the service path indicated by it.  THe algorithm is
	 * as follows:
	 * First, if the user information is empty, then the default service name on
	 * the default container is used.  Otherwise, we see if the user gave us a
	 * complete specified path (relative or absolute) to the service.  If not, 
	 * we see if they gave us the path to a container and then assume the default
	 * service name on that container.  If the user information doesn't resolve at
	 * all, we then assume that it might be the name of a container and look up that
	 * name in the default containers directory.  If we find a container there, we
	 * then use the default service name in that container.  If that fails, then we
	 * instead see if the default container has the user supplied string as a service
	 * named inside of it.  Failing all of this, we fault.
	 * 
	 * @param pathToDefaultContainer The full or relative path to the container
	 * that we will use as the default container.
	 * @param defaultServiceName The default name of the service that we are 
	 * looking for.
	 * @param requiredPortTypes All port types required to be implemented by
	 * our service.
	 * @param userHints Any supplied user hints (path to service, path to container,
	 * name of service, name of container, or empty string).
	 * @return The path to the service we are looking for.
	 * 
	 * @throws ConfigurationException
	 * @throws RNSException
	 */
	static public RNSPath findService(
		String pathToDefaultContainer, String defaultServiceName,
		PortType []requiredPortTypes, String userHints)
		throws RNSException, FileNotFoundException
	{
		RNSPath current = RNSPath.getCurrent();
		RNSPath result;
		
		/* If the user didn't given any hints at all, we have no
		 * choice but to go with the default and hope.
		 */
		if (userHints == null || userHints.length() == 0)
		{
			return current.lookup(
				pathToDefaultContainer + "/Services/" + defaultServiceName, 
				RNSPathQueryFlags.MUST_EXIST);
		}
		
		/* The user gave us some information on what service to find, let's
		 * see if he or she fully qualified the service
		 */
		result = current.lookup(userHints);
		if (!result.exists())
		{
			/* Whatever the user entered, it can't be directly resolved, now
			 * let's check and see if they gave us the name of a container.
			 */
			result = current.lookup(
				"/containers/" + userHints);
			if (!result.exists())
			{
				/* Well, it wasn't a container.  Maybe it's the name of the
				 * service IN the default container.
				 */
				result = current.lookup(
					pathToDefaultContainer + "/Services/" + userHints);
			}
		}
		
		/* If we have a valid path, we have to see if it is a path to a
		 * container, or to the service itself
		 */
		TypeInformation typeInfo = new TypeInformation(result.getEndpoint());
		PortType []implementedPortTypes = typeInfo.getImplementedPortTypes();
		if (implementedPortTypes == null || implementedPortTypes.length == 0)
		{
			/* It isn't a service that indicates port types, so we can only guess
			 * at this point.
			 */
			return result;
		}
		
		boolean missedOne = false;
		for (PortType requiredPortType : requiredPortTypes)
		{
			if (!typeInfo.hasPortType(requiredPortType))
			{
				missedOne = true;
				break;
			}
		}
		
		if (missedOne)
		{
			/* Well, it wasn't the service we were looking for.  So, let's
			 * see if it was a container.
			 */
			if (typeInfo.isContainer())
			{
				/* It was a container, so now we can see if that container has
				 * the default service name inside of it.
				 */
				result = result.lookup("Services/" + defaultServiceName, 
					RNSPathQueryFlags.MUST_EXIST);
				typeInfo = new TypeInformation(result.getEndpoint());
				for (PortType requiredPortType : requiredPortTypes)
				{
					if (!typeInfo.hasPortType(requiredPortType))
					{
						throw new RNSException(
							"Couldn't find an IDP service using the supplied information \""
							+ userHints + "\".");
					}
				}
			} else
			{	
				/* It isn't the service, and it isn't a container, so we basically
				 * have to give up.
				 */
				throw new RNSException(
					"Couldn't find an IDP service using the supplied information \""
					+ userHints + "\".");
			}
		}
		
		return result;
	}
	
	static public RNSMetadataType createMetadata(EndpointReferenceType target,
		MessageElement []any)
	{
		RNSSupportType supportType = RNSSupportType.value3;
		
		if (target != null)
		{
			TypeInformation typeInfo = new TypeInformation(target);
			if (typeInfo.isRNS())
				supportType = RNSSupportType.value1;
			else if (!typeInfo.isUnknown())
				supportType = RNSSupportType.value2;
		}
		SupportsRNSType supportsRNS = new SupportsRNSType(supportType);
		
		return new RNSMetadataType(supportsRNS, any);
	}
}