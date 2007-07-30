package edu.virginia.vcgr.genii.container.counter;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.counter.Add;
import edu.virginia.vcgr.genii.counter.AddResponse;
import edu.virginia.vcgr.genii.counter.CounterPortType;

public class CounterServiceImpl extends GenesisIIBase implements
		CounterPortType
{
	public CounterServiceImpl()
		throws RemoteException
	{
		super("CounterPortType");
		
		addImplementedPortType(new QName(
			GenesisIIConstants.GENESISII_NS, "CounterPortType"));
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public AddResponse add(Add addRequest) throws RemoteException,
			ResourceUnknownFaultType
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		Integer value = (Integer)rKey.dereference().getProperty("value");
		if (value == null)
			value = new Integer(0);
		value = new Integer(value.intValue() + addRequest.getDelta());
		rKey.dereference().setProperty("value", value);
		rKey.dereference().commit();
		return new AddResponse(value.intValue());
	}
}
