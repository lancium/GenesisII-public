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
package edu.virginia.vcgr.genii.container.container;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.apache.axis.description.JavaServiceDesc;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.Move;
import org.ggf.rns.MoveResponse;
import org.ggf.rns.Query;
import org.ggf.rns.QueryResponse;
import org.ggf.rns.RNSDirectoryNotEmptyFaultType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.ggf.rns.Remove;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.stats.ContainerStatistics;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.ContainerStatisticsResultType;
import edu.virginia.vcgr.genii.container.VCGRContainerPortType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class VCGRContainerServiceImpl extends GenesisIIBase
	implements VCGRContainerPortType
{
	static private Log _logger = LogFactory.getLog(VCGRContainerServiceImpl.class);
	static public final String _WELLKNOWN_SERVICEDIR_KEY = 
		"edu.virginia.vcgr.htc.container.container.service-dir-key";
	
	@Override
	protected void setAttributeHandlers()
		throws NoSuchMethodException, ResourceException, 
			ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		
		new VCGRContainerAttributeHandlers(getAttributePackage());
	}
	
	public VCGRContainerServiceImpl() throws RemoteException
	{
		super("VCGRContainerPortType");
		
		addImplementedPortType(RNSConstants.RNS_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.VCGR_CONTAINER_SERVICE_PORT_TYPE);
	}
	
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.VCGR_CONTAINER_SERVICE_PORT_TYPE;
	}
	
	public Object shutdown(Object shutdownRequest) throws RemoteException
	{
		try
		{
			System.exit(0);
			return null;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e);
		}
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponse createFile(CreateFile createFile) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("\"createFile\" not applicable.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) throws RemoteException, RNSEntryExistsFaultType, ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("\"add\" not applicable.");
	}

	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List list) 
		throws RemoteException, ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		ArrayList<EntryType> results = null;
		String entryName = list.getEntryName();
		
		WorkingContext wcontext = WorkingContext.getCurrentWorkingContext();
		EndpointReferenceType epr = 
			(EndpointReferenceType)wcontext.getProperty(WorkingContext.EPR_PROPERTY_NAME);
		ReferenceParametersType refParams = epr.getReferenceParameters();

		// if there are no reference params or the reference param is to the service resource...
		if (refParams == null || refParams.get_any() == null ||
			(refParams.get_any().length == 0))
		{
			// top level
			if (entryName == null || entryName.equals("Services"))
			{
				return new ListResponse(
					new EntryType[] { 
						new EntryType("Services", null, getServicesDirEPR()) } );
			}
		} else
		{
			results = getServiceListing(entryName);
		}

		if (results == null)
			return new ListResponse(new EntryType[0]);
		
		EntryType []ret = new EntryType[results.size()];
		results.toArray(ret);
		return new ListResponse(ret);
	}

	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move move) throws RemoteException, ResourceUnknownFaultType, RNSFaultType
	{
		throw new RemoteException("\"move\" not applicable.");
	}

	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query q)
		throws RemoteException, ResourceUnknownFaultType, RNSFaultType
	{
		throw new RemoteException("\"query\" not applicable.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public String []remove(Remove remove)
		throws RemoteException, ResourceUnknownFaultType, RNSDirectoryNotEmptyFaultType, RNSFaultType
	{
		throw new RemoteException("\"remove\" not applicable.");
	}
	
	protected EndpointReferenceType getServicesDirEPR() 
		throws ResourceException, ResourceUnknownFaultType  
	{
		ResourceKey rKey = ResourceManager.getServiceResource(_serviceName);
		EndpointReferenceType myEPR = 
			(EndpointReferenceType)WorkingContext.getCurrentWorkingContext(
				).getProperty(WorkingContext.EPR_PROPERTY_NAME);
		
		EndpointReferenceType epr =
			EPRUtils.fromBytes((byte[])rKey.dereference().getProperty(
				_WELLKNOWN_SERVICEDIR_KEY));
		epr.setAddress(myEPR.getAddress());
		return epr;
	}
	
	private ArrayList<EntryType> getServiceListing(String entryName)
		throws ResourceException, ResourceUnknownFaultType
	{
		EndpointReferenceType myEPR = 
			(EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
				WorkingContext.EPR_PROPERTY_NAME);
		String shortenedURL = myEPR.getAddress().get_value().toString();
		int last = shortenedURL.lastIndexOf('/');
		if (last <= 0)
			throw new ResourceException("Couldn't parse target EPR Address.");
		shortenedURL = shortenedURL.substring(0, last + 1);
		
		ArrayList<EntryType> ret = new ArrayList<EntryType>();
		
		for (JavaServiceDesc desc : Container.getInstalledServices())
		{
			String serviceName = desc.getName();
			if (entryName == null || entryName.equals(serviceName))
			{
				ResourceKey targetKey = ResourceManager.getServiceResource(serviceName);
				
				EndpointReferenceType targetEPR = 
					ResourceManager.createEPR(targetKey, shortenedURL + serviceName,
						findImplementedPortTypes(desc.getImplClass()));
				
				ret.add(new EntryType(serviceName, null, targetEPR));
			}
		}
		
		return ret;
	}
	
	public boolean startup()
	{
		boolean retval = super.startup();
		
		ResourceKey rKey = null;
		
		try
		{
			WorkingContext ctxt = new WorkingContext();
			WorkingContext.setCurrentWorkingContext(ctxt);
			
			rKey = ResourceManager.getServiceResource(_serviceName);
			ctxt.setProperty(WorkingContext.CURRENT_RESOURCE_KEY, rKey);
			
			
			// check to see if the "Services" key property exists in the service resource
			byte []bytes = (byte[])rKey.dereference().getProperty(
				_WELLKNOWN_SERVICEDIR_KEY);
			if (bytes == null)
			{
				// it doesnt: create a resource for the "Services" dir
				rKey.dereference().setProperty(_WELLKNOWN_SERVICEDIR_KEY,
					EPRUtils.toBytes(
						vcgrCreate(new VcgrCreate(null)).getEndpoint()));
				rKey.dereference().commit();
			}
		}
		catch (BaseFaultType rcft)
		{
			_logger.error(rcft);
		}
		catch (RemoteException re)
		{
			_logger.error(re);
		}
		finally
		{
			WorkingContext.setCurrentWorkingContext(null);
		}
		
		return retval;
	}
	
	private PortType[] findImplementedPortTypes(Class<?> jClass)
		throws ResourceException
	{
		try
		{
			Constructor<?> cons = jClass.getConstructor(new Class[0]);
			Object obj = cons.newInstance(new Object[0]);
			GenesisIIBase base = (GenesisIIBase)obj;
			return base.getImplementedPortTypes(null);
		}
		catch (NoSuchMethodException nsme)
		{
			throw new ResourceException(nsme.getLocalizedMessage(), nsme);
		}
		catch (InvocationTargetException ite)
		{
			throw new ResourceException(ite.getLocalizedMessage(), ite);
		}
		catch (IllegalAccessException iae)
		{
			throw new ResourceException(iae.getLocalizedMessage(), iae);
		}
		catch (InstantiationException ia)
		{
			throw new ResourceException(ia.getLocalizedMessage(), ia);
		}
		catch (ResourceUnknownFaultType rue)
		{
			throw new ResourceException(rue.getLocalizedMessage(), rue);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public ContainerStatisticsResultType containerStatistics(
			Object containerStatistics) throws RemoteException
	{
		ContainerStatistics stats = ContainerStatistics.instance();
		
		try
		{
			return new ContainerStatisticsResultType(stats.getStartTime(),
				DBSerializer.serialize(stats.getDatabaseStatistics().report()),
				DBSerializer.serialize(stats.getMethodStatistics().report()));
		}
		catch (IOException ioe)
		{
			throw new RemoteException(
				"An IO exception occurred trying to serialize statistics.", ioe);
		}
	}
}