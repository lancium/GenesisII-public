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
package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.axis.types.URI;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientConstructionParameters;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSMultiLookupResultException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;

public class ReplicatedFileTool extends BaseGridTool
{
	static final private String _CONTAINERS_DIR_RNS_PATH = "/containers";
	static final private String _CONTAINER_SERVICES_DIR_PATH = "/Services";
	static final private String _BYTEIO_FACTORY_SERVICE_NAME = "RandomByteIOPortType";
	
	static private final String _DESCRIPTION = "Copies file to multiple ByteIO files and places EPR for replicated file in specified target RNS path.";
	static private final String _USAGE = 
		"replicated-file [--local-src] <source-path> <target-rns-path> <container-name>+";
	
	static private final int _BLOCK_SIZE = 
		ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE;
	
	private boolean _localSrc = false;
	
	public ReplicatedFileTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	public void setLocal_src()
	{
		_localSrc = true;
	}
	
	@Override
	public int runCommand() throws Throwable
	{
		String [] containerNames = new String[numArguments()-2];
		for (int i = 2; i < numArguments(); i++)
		{
			containerNames[i-2] = getArgument(i);
		}
		makeReplicatedFile(getArgument(0), _localSrc,
				getArgument(1), containerNames);
		return 0;
	}


	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 3)
			throw new InvalidToolUsageException();
	}
	
	static private void copy(InputStream in, OutputStream out)
		throws IOException
	{
		byte []data = new byte[_BLOCK_SIZE];
		int r;
		
		while ( (r = in.read(data)) >= 0)
		{
			out.write(data, 0, r);
		}
	}

	static public void makeReplicatedFile(String sourcePath, boolean isLocalSource,
			String targetPath, String [] containerNames) 
		throws FileNotFoundException, IOException,
		RNSException, CreationException, RNSPathAlreadyExistsException
	{
		RNSPath currentPath = RNSPath.getCurrent();
		URI epi = WSName.generateNewEPI();
		
		// get target RNS path.
		RNSPath targetRNS = currentPath.lookup(targetPath, RNSPathQueryFlags.DONT_CARE);
		if (targetRNS.exists() && new TypeInformation(targetRNS.getEndpoint()).isRNS())
		{
			String sourceName = getSourceName(sourcePath, isLocalSource);
			targetRNS = targetRNS.lookup(sourceName, RNSPathQueryFlags.DONT_CARE);
		}
		
		if (targetRNS.exists())
			throw new RNSPathAlreadyExistsException(targetPath);
		
		// create directory to hold pointers to replicas
		String replicaDirRNSPath = targetRNS.pwd() + "-Replicas";
		RNSPath replicaDirRNS = currentPath.lookup(replicaDirRNSPath, RNSPathQueryFlags.DONT_CARE);
		if (replicaDirRNS.exists())
			throw new RNSPathAlreadyExistsException(targetPath);
		replicaDirRNS.mkdir();

		String targetFileRNSPath = targetRNS.pwd();
		
		// create copies
		EndpointReferenceType [] eprs = new EndpointReferenceType[containerNames.length];
		for (int i = 0; i < containerNames.length; i++)
		{
			eprs[i] = createCopy(currentPath, sourcePath, isLocalSource, containerNames[i], epi, targetFileRNSPath, i+1);
		}

		// gin up EPR for replicated file.
		WSName replicatedFileWSName = new WSName(eprs[0]);
		
		// remove resolver(s) for primary copy
		replicatedFileWSName.removeAllResolvers();
		
		for (int i = 1; i < containerNames.length; i++)
		{
			WSName copyWSName = new WSName(eprs[i]);
			List<ResolverDescription> resolvers = copyWSName.getResolvers();
			
			for (ResolverDescription nextResolver : resolvers)
			{
				replicatedFileWSName.addReferenceResolver(nextResolver.getEPR());
			}
		}
		EndpointReferenceType replicatedFileEPR = replicatedFileWSName.getEndpoint();
		
		// link in EPR for replicated file into target RNS entry - make sure RNS
		// does not make address unbound.
		ICallingContext origContext = ContextManager.getCurrentContext();
		ICallingContext linkContext = origContext.deriveNewContext();
		linkContext.setSingleValueProperty(
				RNSConstants.RESOLVED_ENTRY_UNBOUND_PROPERTY, 
				RNSConstants.RESOLVED_ENTRY_UNBOUND_FALSE);
		try
		{
			ContextManager.storeCurrentContext(linkContext);
			targetRNS.link(replicatedFileEPR);
		}
		finally
		{
			ContextManager.storeCurrentContext(origContext);
		}
	}
	
	static protected EndpointReferenceType createFile(RNSPath currentPath, URI epi, String replicaRNSPath, String containerName)
	throws ResourceException,
		ResourceCreationFaultType, RemoteException, RNSException,
		CreationException, FileNotFoundException, IOException
	{
		String ByteIOServiceLocation = _CONTAINERS_DIR_RNS_PATH + "/" + containerName + _CONTAINER_SERVICES_DIR_PATH + "/" + _BYTEIO_FACTORY_SERVICE_NAME;
		RNSPath byteIOServiceRNS = currentPath.lookup(ByteIOServiceLocation, RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType byteIOServiceEPR = byteIOServiceRNS.getEndpoint();
		EndpointReferenceType fileEPR = null;
		
		// Set creation properties to pass in EPI.
		MessageElement []any = new MessageElement[1];
		any[0] = ClientConstructionParameters.createEndpointIdentifierProperty(epi);

		ICallingContext origContext = ContextManager.getCurrentContext();
		ICallingContext createContext = origContext.deriveNewContext();
		createContext.setSingleValueProperty(
				RNSConstants.RESOLVED_ENTRY_UNBOUND_PROPERTY, 
				RNSConstants.RESOLVED_ENTRY_UNBOUND_FALSE);
		try
		{
			ContextManager.storeCurrentContext(createContext);
			fileEPR = CreateResourceTool.createInstance(byteIOServiceEPR, replicaRNSPath, any);
		}
		finally
		{
			ContextManager.storeCurrentContext(origContext);
		}

		
		
		return fileEPR;
	}

	static protected EndpointReferenceType createCopy(
			RNSPath currentPath, 
			String sourcePath, 
			boolean isLocalSource, 
			String containerName, 
			URI epi,
			String targetFileRNSPath,
			int replicaNumber)
	throws FileNotFoundException, IOException,
		RNSException, RNSPathDoesNotExistException, CreationException
	{
		OutputStream out = null;
		InputStream in = null;

		try
		{
			if (isLocalSource)
			{
				in = new FileInputStream(sourcePath);
			} else
			{
				RNSPath path = currentPath.lookup(sourcePath, RNSPathQueryFlags.MUST_EXIST);
				in = ByteIOStreamFactory.createInputStream(path);
			}

			// make path name for this replica...
			String replicaRNSPath = targetFileRNSPath + "-Replicas/Copy_" + replicaNumber + "_on_" + containerName;

			// create new file with proper EPI
			EndpointReferenceType epr = createFile(currentPath, epi, replicaRNSPath, containerName);

			// copy data to new file
			RNSPath path = currentPath.lookup(replicaRNSPath, RNSPathQueryFlags.DONT_CARE);
			out = ByteIOStreamFactory.createOutputStream(path);
			copy(in, out);
			return epr;
		}
		catch (RNSPathDoesNotExistException e)
		{
			throw new FileNotFoundException(e.getMessage());
		}
		catch (RNSMultiLookupResultException e)
		{
			throw new IOException(e.getMessage());
		}
		finally
		{
			StreamUtils.close(in);
			StreamUtils.close(out);
		}
	}

	static private String getSourceName(String sourcePath, boolean isLocalSource)
	{
		if (isLocalSource)
		{
			File sourceFile = new File(sourcePath);
			return (sourceFile.getName());
		}
		int index = sourcePath.lastIndexOf('/');
		if (index >= 0)
			return(sourcePath.substring(index + 1));
		else
			return (sourcePath);
	}
}
