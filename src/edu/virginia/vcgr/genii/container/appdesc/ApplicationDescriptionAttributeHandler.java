package edu.virginia.vcgr.genii.container.appdesc;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.appdesc.ApplicationDescriptionConstants;
import edu.virginia.vcgr.genii.client.appdesc.ApplicationVersion;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;

public class ApplicationDescriptionAttributeHandler extends
		AbstractAttributeHandler
{
	public ApplicationDescriptionAttributeHandler(AttributePackage pkg)
		throws NoSuchMethodException
	{
		super(pkg);
	}
	
	private String getApplicationDescription() throws ResourceException,
		ResourceUnknownFaultType
	{
		IResource resource;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = rKey.dereference();
		
		return (String)resource.getProperty(
			ApplicationDescriptionServiceImpl.APPLICATION_DESCRIPTION_PROPERTY_NAME);
	}
	
	private ApplicationVersion getApplicationVersion() throws ResourceException,
		ResourceUnknownFaultType
	{
		IResource resource;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = rKey.dereference();
		
		return (ApplicationVersion)resource.getProperty(
			ApplicationDescriptionServiceImpl.APPLICATION_VERSION_PROPERTY_NAME);
	}
	
	public MessageElement getApplicationDescriptionAttr()
		throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(
			ApplicationDescriptionConstants.APPLICATION_DESCRIPTION_ATTR_QNAME,
			getApplicationDescription());
	}
	
	public MessageElement getApplicationVersionAttr()
		throws ResourceUnknownFaultType, ResourceException
	{
		ApplicationVersion version = getApplicationVersion();
		String versionS = (version == null) ? null : version.toString();
		
		return new MessageElement(
			ApplicationDescriptionConstants.APPLICATION_VERSION_ATTR_QNAME,
			versionS);
	}
	
	public Collection<MessageElement> getSupportDocumentAttributes()
		throws ResourceException, ResourceUnknownFaultType
	{
		ArrayList<MessageElement> supportDocuments =
			new ArrayList<MessageElement>();
		
		IRNSResource resource;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRNSResource)rKey.dereference();
		
		Collection<InternalEntry> entries = resource.retrieveEntries(".*");
		for (InternalEntry entry : entries)
		{
			MessageElement supportDocument = null;
			
			MessageElement []attributes = entry.getAttributes();
			for (MessageElement element : attributes)
			{
				QName name = element.getQName();
				if (name.equals(
					ApplicationDescriptionConstants.SUPPORT_DOCUMENT_ATTR_QNAME))
				{
					supportDocument = element;
					break;
				}
			}
			
			if (supportDocument == null)
				throw new ResourceException(
					"Corrupt application state: No support document attribute.");
			supportDocuments.add(supportDocument);
		}
		
		return supportDocuments;
	}
	
	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(
			ApplicationDescriptionConstants.APPLICATION_DESCRIPTION_ATTR_QNAME, 
			"getApplicationDescriptionAttr");
		addHandler(
			ApplicationDescriptionConstants.APPLICATION_VERSION_ATTR_QNAME, 
			"getApplicationVersionAttr");
		addHandler(
			ApplicationDescriptionConstants.SUPPORT_DOCUMENT_ATTR_QNAME,
			"getSupportDocumentAttributes");
	}
}