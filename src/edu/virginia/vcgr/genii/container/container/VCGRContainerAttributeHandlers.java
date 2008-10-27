package edu.virginia.vcgr.genii.container.container;

import java.io.Serializable;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.container.ContainerConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.cservices.ContainerService;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.downloadmgr.DownloadManagerContainerService;
import edu.virginia.vcgr.genii.container.cservices.scratchmgr.ScratchFSManagerContainerService;

public class VCGRContainerAttributeHandlers extends AbstractAttributeHandler
	implements ContainerConstants
{
	private Serializable getContainerServiceProperty(String propertyName)
	{
		ContainerService exemplar = ContainerServices.findService(
			DownloadManagerContainerService.SERVICE_NAME);
		return exemplar.getContainerServicesProperties().getProperty(
			propertyName);
	}
	
	private void setContainerServiceProperty(String propertyName, 
		Serializable newValue)
	{
		ContainerService exemplar = ContainerServices.findService(
			DownloadManagerContainerService.SERVICE_NAME);
		exemplar.getContainerServicesProperties().setProperty(propertyName, 
			newValue);
	}
	
	private String getDownloadManagerTemporaryDirectory()
	{
		return (String)getContainerServiceProperty(
			DownloadManagerContainerService.DOWNLOAD_TMP_DIR_CSERVICE_PROPERTY);
	}
	
	private void setDownloadManagerTemporaryDirectory(String path)
	{
		setContainerServiceProperty(
			DownloadManagerContainerService.DOWNLOAD_TMP_DIR_CSERVICE_PROPERTY,
			path);
	}
	
	private String getScratchSpaceDirectory()
	{
		return (String)getContainerServiceProperty(
			ScratchFSManagerContainerService.SCRATCH_SPACE_CSERVICES_PROPERTY);
	}

	private void setScratchSpaceDirectory(String path)
	{
		setContainerServiceProperty(
			ScratchFSManagerContainerService.SCRATCH_SPACE_CSERVICES_PROPERTY,
			path);
	}
	
	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(PROPERTY_DOWNLOAD_TMPDIR, 
			"getDownloadManagerTemporaryDirectoryAttr", 
			"setDownloadManagerTemporaryDirectoryAttr");
		addHandler(PROPERTY_SCRATCH_SPACE_DIR,
			"getScratchSpaceDirectoryAttr",
			"setScratchSpaceDirectoryAttr");
	}
	
	public VCGRContainerAttributeHandlers(AttributePackage pkg) 
		throws NoSuchMethodException
	{
		super(pkg);
	}
	
	public MessageElement getDownloadManagerTemporaryDirectoryAttr()
	{
		return new MessageElement(PROPERTY_DOWNLOAD_TMPDIR,
			getDownloadManagerTemporaryDirectory());
	}
	
	public MessageElement getScratchSpaceDirectoryAttr()
	{
		return new MessageElement(PROPERTY_SCRATCH_SPACE_DIR,
			getScratchSpaceDirectory());
	}
	
	public void setDownloadManagerTemporaryDirectoryAttr(MessageElement data)
		throws ResourceException
	{
		setDownloadManagerTemporaryDirectory(
			ObjectDeserializer.toObject(data, String.class));
	}
	
	public void setScratchSpaceDirectoryAttr(MessageElement data)
		throws ResourceException
	{
		setScratchSpaceDirectory(
			ObjectDeserializer.toObject(data, String.class));
	}
}