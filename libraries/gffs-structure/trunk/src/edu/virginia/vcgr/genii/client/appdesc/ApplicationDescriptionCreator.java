package edu.virginia.vcgr.genii.client.appdesc;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rcreate.ResourceCreator;

public class ApplicationDescriptionCreator
{
	static public QName APPLICATION_NAME_CREATION_PARAMETER = new QName(
		ApplicationDescriptionConstants.APPLICATION_DESCRIPTION_NS, "application-name");
	static public QName APPLICATION_VERSION_CREATION_PARAMETER = new QName(
		ApplicationDescriptionConstants.APPLICATION_DESCRIPTION_NS, "application-version");

	static public EndpointReferenceType createApplicationDescription(EndpointReferenceType serviceEPR, String name,
		ApplicationVersion applicationVersion) throws CreationException
	{
		return ResourceCreator.createNewResource(serviceEPR, new MessageElement[] {
			new MessageElement(APPLICATION_NAME_CREATION_PARAMETER, name),
			new MessageElement(APPLICATION_VERSION_CREATION_PARAMETER, applicationVersion.toString()) }, null);
	}

	static public EndpointReferenceType createApplicationDescription(String serviceName, String name,
		ApplicationVersion applicationVersion) throws CreationException
	{
		return ResourceCreator.createNewResource(serviceName,
			new MessageElement[] {
				new MessageElement(APPLICATION_NAME_CREATION_PARAMETER, name),
				new MessageElement(APPLICATION_VERSION_CREATION_PARAMETER, (applicationVersion == null) ? null
					: applicationVersion.toString()) }, null);
	}
}