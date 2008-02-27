/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.container.bes;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.bes.resource.IBESResource;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.sysinfo.SystemInfoUtils;

public class BESAttributesHandler extends AbstractAttributeHandler
{
	static private final String _DESCRIPTION_PROPERTY = "attribute:description";
	static private final String _DEPLOYER_PROPERTY = "attribute:deployer";
	
	static public QName NAME_ATTR = new QName(
		WellKnownPortTypes.BES_SERVICE_PORT_TYPE.getNamespaceURI(), "Name");
	static public QName TOTAL_NUMBER_OF_ACTIVITIES_ATTR = new QName(
		WellKnownPortTypes.BES_SERVICE_PORT_TYPE.getNamespaceURI(), 
		"TotalNumberOfActivities");
	static public QName ACTIVITY_REFERENCE_ATTR = new QName(
		WellKnownPortTypes.BES_SERVICE_PORT_TYPE.getNamespaceURI(),
		"ActivityReference");
	static public QName DESCRIPTION_ATTR = new QName(
		WellKnownPortTypes.BES_SERVICE_PORT_TYPE.getNamespaceURI(), "Description");
	static public QName OPERATING_SYSTEM_ATTR = new QName(
		WellKnownPortTypes.BES_SERVICE_PORT_TYPE.getNamespaceURI(), 
		"OperatingSystem");
	static public QName CPU_ARCHITECTURE_ATTR = new QName(
		WellKnownPortTypes.BES_SERVICE_PORT_TYPE.getNamespaceURI(), 
		"CPUArchitecture");
	static public QName CPU_COUNT_ATTR = new QName(
		WellKnownPortTypes.BES_SERVICE_PORT_TYPE.getNamespaceURI(), "CPUCount");
	
	static public QName CPU_SPEED_ATTR = new QName(
		GenesisIIConstants.JSDL_NS, "IndividualCPUSpeed");
	static public QName PHYSICAL_MEMORY_ATTR = new QName(
		GenesisIIConstants.JSDL_NS, "PhysicalMemory");
	static public QName VIRTUAL_MEMORY_ATTR = new QName(
		GenesisIIConstants.JSDL_NS, "VirtualMemory");
	
	public BESAttributesHandler(AttributePackage pkg) throws NoSuchMethodException
	{
		super(pkg);
	}
	
	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(NAME_ATTR, "getNameAttr");
		addHandler(TOTAL_NUMBER_OF_ACTIVITIES_ATTR,
			"getTotalNumberOfActivitiesAttr");
		addHandler(ACTIVITY_REFERENCE_ATTR, "getActivityReferencesAttr");
		addHandler(DESCRIPTION_ATTR, "getDescriptionAttr", "setDescriptionAttr");
		addHandler(OPERATING_SYSTEM_ATTR, "getOperatingSystemAttr");
		addHandler(CPU_ARCHITECTURE_ATTR, "getCPUArchitectureAttr");
		addHandler(CPU_COUNT_ATTR, "getCPUCountAttr");
		
		addHandler(CPU_SPEED_ATTR, "getCPUSpeedAttr");
		addHandler(PHYSICAL_MEMORY_ATTR, "getPhysicalMemoryAttr");
		addHandler(VIRTUAL_MEMORY_ATTR, "getVirtualMemoryAttr");
		addHandler(BESConstants.DEPLOYER_EPR_ATTR, 
			"getDeployersAttr", "setDeployersAttr");
	}
	
	static public String getName()
	{
		return Hostname.getLocalHostname().toString();
	}
	
	static public int getTotalNumberOfActivities() 
		throws ResourceUnknownFaultType, ResourceException
	{
		return getActivityReferences().length;
	}
	
	static public EndpointReferenceType[] getActivityReferences()
		throws ResourceUnknownFaultType, ResourceException
	{
		IBESResource resource = null;
		
		resource = (IBESResource)(ResourceManager.getCurrentResource().dereference());
		return resource.getContainedActivities();
	}
	
	static public String getDescription()
		throws ResourceException, ResourceUnknownFaultType
	{
		IBESResource resource = null;
		
		resource = (IBESResource)ResourceManager.getCurrentResource().dereference();
		return (String)resource.getProperty(_DESCRIPTION_PROPERTY);
	}
	
	static public OperatingSystem_Type getOperatingSystem()
	{
		return JSDLUtils.getLocalOperatingSystem();
	}
	
	static public CPUArchitecture_Type getCPUArchitecture()
	{
		return JSDLUtils.getLocalCPUArchitecture();
	}
	
	static public int getCPUCount()
	{
		return ManagementFactory.getOperatingSystemMXBean(
			).getAvailableProcessors();
	}
	
	public void setDescription(String description)
		throws ResourceException, ResourceUnknownFaultType
	{
		IBESResource resource = null;
		
		try
		{
			resource = (IBESResource)ResourceManager.getCurrentResource().dereference();
			resource.setProperty(_DESCRIPTION_PROPERTY, description);
			resource.commit();
		}
		finally
		{
			StreamUtils.close(resource);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Collection<EndpointReferenceType> getDeployers()
		throws ResourceException, ResourceUnknownFaultType
	{
		ArrayList<EndpointReferenceType> eprs 
			= new ArrayList<EndpointReferenceType>();
		IResource resource;
		resource = ResourceManager.getCurrentResource().dereference();
		for (byte[] bytes : (Collection<byte[]>)resource.getProperty(
			_DEPLOYER_PROPERTY))
		{
			eprs.add(EPRUtils.fromBytes(bytes));
		}
		
		return eprs;
	}
	
	public void setDeployers(Collection<EndpointReferenceType> deployers)
		throws ResourceException, ResourceUnknownFaultType
	{
		ArrayList<byte[]> toStore = new ArrayList<byte[]>();
		for (EndpointReferenceType deployer : deployers)
			toStore.add(EPRUtils.toBytes(deployer));
		
		IResource resource;
		resource = ResourceManager.getCurrentResource().dereference();
		resource.setProperty(_DEPLOYER_PROPERTY, toStore);
		resource.commit();
	}
	
	public MessageElement getNameAttr()
	{
		return new MessageElement(NAME_ATTR, getName());
	}
	
	public MessageElement getTotalNumberOfActivitiesAttr()
		throws ResourceException, ResourceUnknownFaultType
	{
		return new MessageElement(TOTAL_NUMBER_OF_ACTIVITIES_ATTR,
			getTotalNumberOfActivities());
	}
	
	public ArrayList<MessageElement> getActivityReferencesAttr()
		throws ResourceException, ResourceUnknownFaultType
	{
		EndpointReferenceType []eprs = getActivityReferences();
		ArrayList<MessageElement> ret = new ArrayList<MessageElement>(eprs.length);
		for (int lcv = 0; lcv < eprs.length; lcv++)
		{
			ret.add(new MessageElement(ACTIVITY_REFERENCE_ATTR, eprs[lcv]));
		}
		
		return ret;
	}
	
	public ArrayList<MessageElement> getDescriptionAttr()
		throws ResourceException, ResourceUnknownFaultType
	{
		ArrayList<MessageElement> result = new ArrayList<MessageElement>();
		String desc = getDescription();
		if (desc != null)
			result.add(new MessageElement(DESCRIPTION_ATTR, desc));
		
		return result;
	}
	
	public void setDescriptionAttr(MessageElement element)
		throws ResourceException, ResourceUnknownFaultType
	{
		setDescription(element.getValue());
	}
	
	public MessageElement getOperatingSystemAttr()
	{
		return new MessageElement(OPERATING_SYSTEM_ATTR, getOperatingSystem());
	}
	
	public MessageElement getCPUArchitectureAttr()
	{
		return new MessageElement(CPU_ARCHITECTURE_ATTR, getCPUArchitecture());
	}
	
	public MessageElement getCPUCountAttr()
	{
		return new MessageElement(CPU_COUNT_ATTR, getCPUCount());
	}
	
	public MessageElement getCPUSpeedAttr()
	{
		return new MessageElement(CPU_SPEED_ATTR,
			SystemInfoUtils.getIndividualCPUSpeed());
	}
	
	public MessageElement getPhysicalMemoryAttr()
	{
		return new MessageElement(PHYSICAL_MEMORY_ATTR, 
			SystemInfoUtils.getPhysicalMemory());
	}
	
	public MessageElement getVirtualMemoryAttr()
	{
		return new MessageElement(VIRTUAL_MEMORY_ATTR,
			SystemInfoUtils.getVirtualMemory());
	}
	
	public ArrayList<MessageElement> getDeployersAttr()
		throws ResourceException, ResourceUnknownFaultType
	{
		ArrayList<MessageElement> deployers = new ArrayList<MessageElement>();
		for (EndpointReferenceType deployer : getDeployers())
		{
			deployers.add(
				new MessageElement(BESConstants.DEPLOYER_EPR_ATTR, deployer));
		}
		
		return deployers;
	}
	
	public void setDeployersAttr(Collection<MessageElement> deployers)
		throws ResourceException, ResourceUnknownFaultType
	{
		Collection<EndpointReferenceType> deployerEPRs = 
			new ArrayList<EndpointReferenceType>();
		
		for (MessageElement deployerM : deployers)
		{
			EndpointReferenceType deployer = 
				ObjectDeserializer.toObject(
					deployerM, EndpointReferenceType.class);
			deployerEPRs.add(deployer);
		}
		
		setDeployers(deployerEPRs);
	}
}