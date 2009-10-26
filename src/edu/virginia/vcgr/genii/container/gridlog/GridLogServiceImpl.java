package edu.virginia.vcgr.genii.container.gridlog;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.gridlog.GridLogConstants;
import edu.virginia.vcgr.genii.client.gridlog.GridLogUtils;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.common.notification.Notify;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.common.SByteIOFactory;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.gridlog.AppendRequestType;
import edu.virginia.vcgr.genii.gridlog.AppendResponseType;
import edu.virginia.vcgr.genii.gridlog.AppendToLogRequestType;
import edu.virginia.vcgr.genii.gridlog.AppendToLogResponseType;
import edu.virginia.vcgr.genii.gridlog.GridLogPortType;

public class GridLogServiceImpl extends GenesisIIBase
	implements GridLogPortType
{
	static final public String SERVICE_NAME = "GridLogPortType";
	
	public GridLogServiceImpl() throws RemoteException
	{
		super(SERVICE_NAME);
		
		addImplementedPortType(GridLogConstants.GRIDLOG_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.SBYTEIO_FACTORY_PORT_TYPE);
	}
	
	@Override
	public PortType getFinalWSResourceInterface()
	{
		return GridLogConstants.GRIDLOG_PORT_TYPE;
	}
	
	@Override
	@RWXMapping(RWXCategory.OPEN)
	public void notify(Notify request) throws RemoteException,
			ResourceUnknownFaultType
	{
		// Do nothing
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public AppendResponseType append(AppendRequestType request)
			throws RemoteException
	{
		GridLogResource resource = 
			(GridLogResource)ResourceManager.getCurrentResource(
				).dereference();
		
		try
		{
			LoggingEvent event = GridLogUtils.convert(request.getContent());
			resource.append(null, event, request.getHostname());
			return new AppendResponseType(true);
		}
		catch (IOException ioe)
		{
			throw new ResourceException(
				"Unable to append logging event to log.", ioe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public AppendToLogResponseType appendToLog(AppendToLogRequestType request)
			throws RemoteException
	{
		GridLogResource resource = 
			(GridLogResource)ResourceManager.getCurrentResource(
				).dereference();
		
		try
		{
			LoggingEvent event = GridLogUtils.convert(request.getContent());
			resource.append(request.getLogId(), event, request.getHostname());
			return new AppendToLogResponseType(true);
		}
		catch (IOException ioe)
		{
			throw new ResourceException(
				"Unable to append logging event to log.", ioe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public OpenStreamResponse openStream(Object request) throws RemoteException,
			ResourceUnknownFaultType, ResourceCreationFaultType
	{
		SByteIOFactory sFactory = null;
		
		try
		{
			sFactory = createStreamableByteIOResource();
			OutputStream out = sFactory.getCreationStream();
			PrintStream ps = new PrintStream(out);
			
			GridLogResource resource = 
				(GridLogResource)ResourceManager.getCurrentResource(
					).dereference();
			for (LogEventInformation info : resource.listEvents(null, true))
			{
				LoggingEvent event = info.event();
				ps.format("[%s: %tc] %s\n", info.hostname(),
					event.timeStamp, event.getRenderedMessage());
				ThrowableInformation tinfo = event.getThrowableInformation();
				if (tinfo != null)
				{
					ps.format("\tStack Trace:  %s\n", tinfo.getThrowable());
					for (String rep : tinfo.getThrowableStrRep())
						ps.format("\t\t%s\n", rep);
				}
			}
			
			ps.flush();
			
			return new OpenStreamResponse(sFactory.create());
		} catch (IOException e)
		{
			throw new RemoteException("Unable to create stream.", e);
		}
		finally
		{
			StreamUtils.close(sFactory);
		}
	}
}