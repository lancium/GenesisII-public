package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.RNSEntryDoesNotExistFaultType;
import org.ggf.rns.RNSEntryResponseType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.common.AttributesPreFetcherFactory;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.disk.DiskExportEntry;
import edu.virginia.vcgr.genii.container.iterator.FileOrDir;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorEntry;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorWrapper;
import edu.virginia.vcgr.genii.container.iterator.IterableSnapshot;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.AbstractRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.RForkUtils;
import edu.virginia.vcgr.genii.container.rfork.RNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rfork.iterator.InMemoryIterableFork;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.rns.Prefetcher;
import edu.virginia.vcgr.genii.container.serializer.MessageElementSerializer;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.security.RWXCategory;

public class LightWeightExportDirFork extends AbstractRNSResourceFork
	implements RNSResourceFork, InMemoryIterableFork
{
	
	
	final private VExportDir getTarget() throws IOException
	{
		return LightWeightExportUtils.getDirectory(getForkPath());
	}
	
	public LightWeightExportDirFork(ResourceForkService service,
			String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType add(EndpointReferenceType exemplarEPR,
			String entryName, EndpointReferenceType entry) throws IOException
	{
		throw new IOException(
			"Not allowed to add arbitrary endpoints to a " +
			"light-weight export.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType createFile(EndpointReferenceType exemplarEPR,
			String newFileName) throws IOException
	{
		VExportDir dir = getTarget();
		if (dir.createFile(newFileName))
		{
			String forkPath = formForkPath(newFileName);
			ResourceForkService service = getService();
			
			return service.createForkEPR(forkPath,
				new LightWeightExportFileFork(service, forkPath).describe());
		}
		
		throw new IOException("Unable to create new file.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR,
			String entryName) throws IOException
	{
		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();
		VExportDir dir = getTarget();
		
		for (VExportEntry dirEntry : dir.list(entryName))
		{
			String dName = dirEntry.getName();
			
			if (entryName == null || entryName.equals(dName))
			{
				ResourceForkInformation info;
				
				if (dirEntry.isDirectory())
					info = new LightWeightExportDirFork(getService(),
						formForkPath(dName)).describe();
				else
					info = new LightWeightExportFileFork(getService(),
						formForkPath(dName)).describe();
				
				entries.add(createInternalEntry(exemplarEPR, dName, info));
			}
		}
		
		if(entryName != null && entries.size() == 0)
			entries.add(new InternalEntry(entryName, null, null ,false));
		return entries;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType mkdir(EndpointReferenceType exemplarEPR,
			String newDirectoryName) throws IOException
	{
		VExportDir dir = getTarget();
		if (dir.mkdir(newDirectoryName))
		{
			String forkPath = formForkPath(newDirectoryName);
			ResourceForkService service = getService();
			
			return service.createForkEPR(forkPath,
				new LightWeightExportDirFork(service, forkPath).describe());
		}
		
		throw new IOException("Unable to create new directory.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public boolean remove(String entryName) throws IOException
	{
		VExportDir dir = getTarget();
		return dir.remove(entryName);
	}

	@Override
	public boolean isInMemoryIterable() throws IOException 
	{
		if(getTarget() instanceof DiskExportEntry)
			return true;
		
		return false;
	}
	
	
	@Override
	public InMemoryIterableFork getInMemoryIterableFork() throws IOException
	{
		if(!isInMemoryIterable())
			return null;
		
		return this;
		
	}

	@Override
	public IterableSnapshot splitAndList(EndpointReferenceType exemplarEPR,
			ResourceKey myKey) throws IOException 
	{
		
		if(!isInMemoryIterable())
			throw new IOException("Cannot support in-memory iteration!");
		
		int count = 0;
		
		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();
		List<InMemoryIteratorEntry> imieList = new LinkedList<InMemoryIteratorEntry>();
		
		VExportDir dir = getTarget();
		
		for (VExportEntry dirEntry : dir.list())
		{
			String dName = dirEntry.getName();
			
			if(count < RNSConstants.PREFERRED_BATCH_SIZE)
			{
				
				ResourceForkInformation info;
				
				if (dirEntry.isDirectory())
					info = new LightWeightExportDirFork(getService(),
						formForkPath(dName)).describe();
				else
					info = new LightWeightExportFileFork(getService(),
						formForkPath(dName)).describe();
				
				entries.add(createInternalEntry(exemplarEPR, dName, info));
				
			}
			
			else
			{
				InMemoryIteratorEntry imie;
				if (dirEntry.isDirectory())
					imie = new InMemoryIteratorEntry(dName, 
							getForkPath(), true, FileOrDir.DIRECTORY);
				
				else
					imie = new InMemoryIteratorEntry(dName, 
							getForkPath(), true, FileOrDir.FILE);
				
				imieList.add(imie);
			}
			
			count++;
		}		
		
		InMemoryIteratorWrapper imiw = null;
		
		if(imieList.size() > 0 )
		{
			imiw = new InMemoryIteratorWrapper(this.getClass().getName(),
					imieList, new Object[]{exemplarEPR, getService(), myKey});
		}
		
		return new IterableSnapshot(entries, imiw);
	}

	public static MessageElement getIndexedContent(Connection connection,
			InMemoryIteratorEntry entry, Object[] EprAndService) throws ResourceException
	{
		if(EprAndService == null)
			throw new ResourceException("Unable to list directory contents");
		
		if(EprAndService.length !=3 )
			throw new ResourceException("Unable to list directory contents");
		
		EndpointReferenceType exemplarEPR = (EndpointReferenceType)EprAndService[0];
		if(exemplarEPR == null)
			throw new ResourceException("Unable to list directory contents");
		
		ResourceForkService service = (ResourceForkService)EprAndService[1];
		if(service == null )
			throw new ResourceException("Unable to list directory contents");

		ResourceKey rKey = (ResourceKey)EprAndService[2];
		if(rKey == null)
			throw new ResourceException("Unable to list directory contents");
								
		String dName = entry.getEntryName();
		
		if(dName == null)
			throw new ResourceException("Unable to list directory contents");
		
		String forkPath = entry.getId();
		ResourceForkInformation info = null;
		FileOrDir fd = entry.getType();
		InternalEntry ie;
		RNSEntryResponseType resp = null;
		
		if(fd == FileOrDir.UNKNOWN)
		{
			//we identify if it is a file or dir!
			try
			{
				FileOrDir stat = statify(dName, forkPath, rKey);
				if(stat == FileOrDir.DIRECTORY)
				{
					LightWeightExportDirFork lwedf = new LightWeightExportDirFork(service,
						RForkUtils.formForkPathFromPath(forkPath, dName));
					info = lwedf.describe();
				}
			
				else if(stat == FileOrDir.FILE)
				{
					LightWeightExportFileFork lweff = new LightWeightExportFileFork(service,
						RForkUtils.formForkPathFromPath(forkPath, dName));
				
					info = lweff.describe();
				}
			
				else
				{
				
					resp = new RNSEntryResponseType(null, null, 
						FaultManipulator.fillInFault(
								new RNSEntryDoesNotExistFaultType(
								null, null, null, null, 
								new BaseFaultTypeDescription[] 
								{
										new BaseFaultTypeDescription(String.format("Entry" +
										" %s does not exist!", dName))
							    },null, dName)), dName);
				
					return(MessageElementSerializer.serialize(RNSEntryResponseType.getTypeDesc().
						getXmlType(), resp));
				}
			}
			
			catch(IOException ioe)
			{
				throw new ResourceException("Unable to list directory contents");
			}
		}
		
		else if(fd == FileOrDir.DIRECTORY)
		{
			LightWeightExportDirFork lwedf = new LightWeightExportDirFork(service,
					RForkUtils.formForkPathFromPath(forkPath, dName));
			info = lwedf.describe();
			
		}
			
		
		else if(fd == FileOrDir.FILE)
		{
			LightWeightExportFileFork lweff = new LightWeightExportFileFork(service,
					RForkUtils.formForkPathFromPath(forkPath, dName));
			
			info = lweff.describe();
		}
		
		try 
		{
			ie = new InternalEntry(dName,
				 service.createForkEPR(RForkUtils.formForkPathFromPath(forkPath, dName)
					,info),null);
			
		}
		catch (ResourceUnknownFaultType e) 
		{
			throw new ResourceException("Unable to list directory contents");
		}		
			
		EndpointReferenceType epr = ie.getEntryReference();
		AttributesPreFetcherFactory factory = 
			new LightWeightExportAttributePrefetcherFactoryImpl();
		
		resp = new RNSEntryResponseType(
    			epr, RNSUtilities.createMetadata(epr, 
    				Prefetcher.preFetch(epr, ie.getAttributes(), factory, rKey,
    						service)),
    			null, ie.getName());
		
		return(MessageElementSerializer.serialize(RNSEntryResponseType.getTypeDesc().
				getXmlType(), resp));
		
	}

	private static FileOrDir statify(String entryName, String forkPath, ResourceKey rKey) throws IOException
	{
			
		VExportDir dir = LightWeightExportUtils.getDirectory(forkPath, rKey);
		
		for (VExportEntry dirEntry : dir.list(entryName))
		{
			String dName = dirEntry.getName();
			
			if (entryName.equals(dName))
			{
								
				if (dirEntry.isDirectory())
					return FileOrDir.DIRECTORY;
				else
					return FileOrDir.FILE;								
			}
		}
		return FileOrDir.UNKNOWN;
	}

	@Override
	public IterableSnapshot splitAndList(String[] lookupRequest,
			EndpointReferenceType exemplarEPR, ResourceKey resourceKey) throws IOException
	{
		
		if(!isInMemoryIterable())
			throw new IOException("Cannot support in-memory iteration!");
				
		if(lookupRequest == null || lookupRequest.length == 0)
			return splitAndList(exemplarEPR, resourceKey);
				
		
		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();
		List<InMemoryIteratorEntry> imieList = new LinkedList<InMemoryIteratorEntry>();
					
		int min = (lookupRequest.length > RNSConstants.PREFERRED_BATCH_SIZE)?
					RNSConstants.PREFERRED_BATCH_SIZE:lookupRequest.length;
		
		for(int lcv=0; lcv< min; lcv++)
		{
			String request = lookupRequest[lcv];
			if((list(exemplarEPR, request)).iterator().hasNext())
				entries.add(list(exemplarEPR,request).iterator().next());			
		}
		
		if(lookupRequest.length <= RNSConstants.PREFERRED_BATCH_SIZE)
		{
			//WE WILL NOT BE DOING in-memory iteration
			return new IterableSnapshot(entries, null);
		}
		
		else
		{
			//we will be doing in-memory iteration
			InMemoryIteratorWrapper imiw = null;
			for(int lcv=min ; lcv<lookupRequest.length; lcv++)
			{
				String request = lookupRequest[lcv];
				
				/*We put a true for exists! It does not matter if it is a false.
				 * We will identify and throw a fault during iteration! */
				
				InMemoryIteratorEntry imie = new InMemoryIteratorEntry(request, 
						getForkPath(), true, FileOrDir.UNKNOWN);
				imieList.add(imie);
			}
			
			imiw = new InMemoryIteratorWrapper(this.getClass().getName(),
					imieList, new Object[]{exemplarEPR, getService(), resourceKey});
					
			return new IterableSnapshot(entries, imiw);
		}
		
	}			
	
}