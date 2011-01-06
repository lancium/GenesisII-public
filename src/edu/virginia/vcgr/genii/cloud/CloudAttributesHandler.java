package edu.virginia.vcgr.genii.cloud;


import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.bes.resource.IBESResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class CloudAttributesHandler extends AbstractAttributeHandler
implements CloudConstants{


	static private Log _logger = LogFactory.getLog(
			CloudAttributesHandler.class);

	public CloudAttributesHandler(AttributePackage pkg) 
	throws NoSuchMethodException {
		super(pkg);
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException {
		// TODO Auto-generated method stub
		//Sets size of resources, returns count
		addHandler(SPAWN_RESOURCES_ATTR, "getStatus", "spawnResources");  
		addHandler(SHRINK_RESOURCES_ATTR, "getStatus", "shrinkResources");
		addHandler(STATUS_ATTR, "getStatus");

	}


	public void spawnResources(MessageElement element)
	throws ResourceException, ResourceUnknownFaultType
	{
		IBESResource resource = null;
		resource =
			(IBESResource)ResourceManager.getCurrentResource().dereference();
		String besid = resource.getKey();
		CloudManager tManage = CloudMonitor.getManager(besid);

		if (tManage != null){
			try {
				int count = (Integer)(element.getObjectValue(Integer.class));
				tManage.spawnResources(count);
			} catch (Exception e) {
				_logger.error(e);
			}
		}
	}

	public void shrinkResources(MessageElement element)
	throws ResourceException, ResourceUnknownFaultType
	{
		IBESResource resource = null;

		resource = 
			(IBESResource)ResourceManager.getCurrentResource().dereference();

		String besid = resource.getKey();
		CloudManager tManage = CloudMonitor.getManager(besid);
		if (tManage != null){
			try {
				int count = (Integer)(element.getObjectValue(Integer.class));
				tManage.killResources(count);
			} catch (Exception e) {
				_logger.error(e);
			}
		}
	}

	public MessageElement getStatus() throws Exception
	{

		IBESResource resource = null;
		resource = 
			(IBESResource)ResourceManager.getCurrentResource().dereference();
		String besid = resource.getKey();
		CloudManager tManage = CloudMonitor.getManager(besid);
		if (tManage != null)
			return tManage.getStatus().toMessageElement(STATUS_ATTR);
		else
			return null;

	}


}
