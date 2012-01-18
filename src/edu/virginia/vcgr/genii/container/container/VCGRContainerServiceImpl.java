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
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.ser.AnyHelper;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.stats.ContainerStatistics;
import edu.virginia.vcgr.genii.container.AccountingRecordType;
import edu.virginia.vcgr.genii.container.CommitAccountingRecordsRequestType;
import edu.virginia.vcgr.genii.container.ContainerStatisticsResultType;
import edu.virginia.vcgr.genii.container.IterateAccountingRecordsResponseType;
import edu.virginia.vcgr.genii.container.VCGRContainerPortType;
import edu.virginia.vcgr.genii.container.container.forks.RootRNSFork;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.accounting.AccountingService;
import edu.virginia.vcgr.genii.container.iterator.IteratorBuilder;
import edu.virginia.vcgr.genii.container.rfork.ForkRoot;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkBaseService;
import edu.virginia.vcgr.genii.security.RWXCategory;

@ForkRoot(RootRNSFork.class)
public class VCGRContainerServiceImpl extends ResourceForkBaseService
	implements VCGRContainerPortType
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(
		VCGRContainerServiceImpl.class);

	static final private String SERVICE_NAME = "VCGRContainerPortType";
	
	@Override
	protected void setAttributeHandlers() throws ResourceException, 
		ResourceUnknownFaultType, NoSuchMethodException
	{
		super.setAttributeHandlers();
		
		new VCGRContainerAttributeHandlers(getAttributePackage());
	}
	
	public VCGRContainerServiceImpl() throws RemoteException
	{
		super(SERVICE_NAME);
		
		addImplementedPortType(
			WellKnownPortTypes.VCGR_CONTAINER_SERVICE_PORT_TYPE);
	}

	@Override
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.VCGR_CONTAINER_SERVICE_PORT_TYPE;
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public ContainerStatisticsResultType containerStatistics(Object arg0)
			throws RemoteException
	{
		ContainerStatistics stats = ContainerStatistics.instance();
		
		try
		{
			return new ContainerStatisticsResultType(stats.getStartTime(),
				DBSerializer.serialize(stats.getDatabaseStatistics().report(),
					Long.MAX_VALUE),
				DBSerializer.serialize(stats.getMethodStatistics().report(),
					Long.MAX_VALUE));
		}
		catch (IOException ioe)
		{
			throw new RemoteException(
				"An IO Exception occurred trying to serialize statistics.",
				ioe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public Object shutdown(Object arg0) throws RemoteException
	{
		throw new RemoteException(
			"Not allowed to shut down the container this way.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public IterateAccountingRecordsResponseType iterateAccountingRecords(
		Object arg0) throws RemoteException
	{
		Collection<MessageElement> col = new LinkedList<MessageElement>();
		
		AccountingService acctService = 
			ContainerServices.findService(
				AccountingService.class);
		
		try
		{
			if (acctService != null)
			{
				for (AccountingRecordType art : acctService.getAccountingRecords())
					col.add(AnyHelper.toAny(art));
			}
			
			IteratorBuilder<MessageElement> builder = iteratorBuilder();
			builder.preferredBatchSize(100);
			builder.addElements(col);
			return new IterateAccountingRecordsResponseType(
				builder.create());
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to create iterator.", ioe);
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to create iterator.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void commitAccountingRecords(CommitAccountingRecordsRequestType arg0)
			throws RemoteException
	{
		AccountingService acctService = 
			ContainerServices.findService(
				AccountingService.class);
		
		try
		{
			if (acctService != null)
				acctService.deleteAccountingRecords(
					arg0.getLastRecordIdToCommit());
		}
		catch (SQLException sqe)
		{
			throw new RemoteException(
				"Unable to commit accounting records.", sqe);
		}
	}
}