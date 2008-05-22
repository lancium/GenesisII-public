package edu.virginia.vcgr.genii.container.exportdir;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.exportdir.ExportedFileUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.container.byteio.RandomByteIOServiceImpl;
import edu.virginia.vcgr.genii.container.common.SByteIOFactory;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.exportdir.ExportedFilePortType;

public class ExportedFileServiceImpl extends RandomByteIOServiceImpl
	implements ExportedFilePortType
{
	static private Log _logger = LogFactory.getLog(ExportedFileServiceImpl.class);
	
	public ExportedFileServiceImpl() throws RemoteException
	{
		super("ExportedFilePortType");
		
		addImplementedPortType(WellKnownPortTypes.EXPORTED_FILE_SERVICE_PORT_TYPE);
	}
	
	protected ResourceKey createResource(HashMap<QName, Object> constructionParameters)
		throws ResourceException, BaseFaultType
	{
		_logger.debug("Creating new ExportedFile Resource.");
		
		if (constructionParameters == null)
		{
			ResourceCreationFaultType rcft =
				new ResourceCreationFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription(
							"Could not create ExportedFile resource without cerationProperties")
						}, null);
			throw FaultManipulator.fillInFault(rcft);
		}
		
		ExportedFileUtils.ExportedFileInitInfo initInfo = null;
		initInfo = ExportedFileUtils.extractCreationProperties(constructionParameters);

		constructionParameters.put(
			IExportedFileResource.PATH_CONSTRUCTION_PARAM, initInfo.getPath());
		constructionParameters.put(
			IExportedFileResource.PARENT_IDS_CONSTRUCTION_PARAM, initInfo.getParentIds());
		constructionParameters.put(
			IExportedDirResource.REPLICATION_INDICATOR, initInfo.getReplicationState());
		
		return super.createResource(constructionParameters);
	}
	
	protected void fillIn(ResourceKey rKey, EndpointReferenceType newEPR,
		HashMap<QName, Object> creationParameters,
		Collection<MessageElement> resolverCreationParams) 
			throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, newEPR, creationParameters, resolverCreationParams);
		
		Date d = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		
		IExportedFileResource resource = (IExportedFileResource)rKey.dereference();
		resource.setCreateTime(c);
		resource.setModTime(c);
		resource.setAccessTime(c);
	}
	
	public boolean startup()
	{
		return super.startup();
	}
	
	@RWXMapping(RWXCategory.READ)
	public OpenStreamResponse openStream(Object openStreamRequest) 
		throws RemoteException, ResourceCreationFaultType, ResourceUnknownFaultType
	{
		SByteIOFactory factory = null;
		InputStream inLocal = null;
		String primaryLocalPath = (String) openStreamRequest;

		try
		{
			factory = createStreamableByteIOResource();
			OutputStream outputStream = factory.getCreationStream();
			
			inLocal = new FileInputStream(primaryLocalPath);
			copy(inLocal, outputStream);
			
			outputStream.flush();

			return new OpenStreamResponse(factory.create());
		}
		catch (IOException ioe){
			throw FaultManipulator.fillInFault(
				new ResourceCreationFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription(ioe.getLocalizedMessage()) },
					null));
		}
		finally{
			StreamUtils.close(factory);
		}
	}
	
	static private final int _BLOCK_SIZE = ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE;
	
	static private void copy(InputStream in, OutputStream out)
		throws IOException
	{
		byte []data = new byte[_BLOCK_SIZE];
		int r;
		
		while ( (r = in.read(data)) >= 0){
			out.write(data, 0, r);
		}
	}
	
}