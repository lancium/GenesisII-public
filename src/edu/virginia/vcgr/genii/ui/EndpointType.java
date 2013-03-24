package edu.virginia.vcgr.genii.ui;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public enum EndpointType {
	DIRECTORY(), FILE(), BES(), QUEUE(), HEAVY_EXPORT(), LIGHT_EXPORT(), USER(), UNKNOWN();

	static public EndpointType determineType(RNSPath target) throws RNSPathDoesNotExistException
	{
		return determineType(target.getEndpoint());
	}

	static public EndpointType determineType(EndpointReferenceType target)
	{
		return determineType(new TypeInformation(target));
	}

	static public EndpointType determineType(TypeInformation typeInfo)
	{
		if (typeInfo.isQueue())
			return QUEUE;
		else if (typeInfo.isBES())
			return BES;
		else if (typeInfo.isIDP())
			return USER;
		else if (typeInfo.isLightweightExport())
			return LIGHT_EXPORT;
		else if (typeInfo.isExport())
			return HEAVY_EXPORT;
		else if (typeInfo.isFSProxy())
			return LIGHT_EXPORT;
		else if (typeInfo.isEnhancedRNS())
			return DIRECTORY;
		else if (typeInfo.isRNS())
			return DIRECTORY;
		else if (typeInfo.isByteIO())
			return FILE;
		else
			return UNKNOWN;
	}

	static public boolean isLocal(EndpointReferenceType target)
	{
		return false;
	}

	static public boolean isLocal(RNSPath path) throws RNSPathDoesNotExistException
	{
		return isLocal(path.getEndpoint());
	}
}