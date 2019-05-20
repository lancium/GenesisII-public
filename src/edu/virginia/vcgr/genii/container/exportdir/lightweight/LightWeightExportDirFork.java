package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.RNSEntryDoesNotExistFaultType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSMetadataType;
import org.morgan.util.Pair;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.ExportProperties.ExportMechanisms;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.client.utils.StatsLogger;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.container.common.AttributesPreFetcherFactory;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.disk.DiskExportEntry;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.SudoDiskExportEntry;
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
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

public class LightWeightExportDirFork extends AbstractRNSResourceFork implements RNSResourceFork, InMemoryIterableFork
{
	static private Log _logger = LogFactory.getLog(LightWeightExportDirFork.class);

	final private VExportDir getTarget() throws IOException
	{
		return LightWeightExportUtils.getDirectory(getForkPath());
	}

	public LightWeightExportDirFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType add(EndpointReferenceType exemplarEPR, String entryName, EndpointReferenceType entry) throws IOException
	{
		VExportDir dir = getTarget();
		// 2018-02-07 ASG - adding support for links to exports. Log it first though.
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
		StatsLogger.logStats("LightWeightExport:  Create ln \"" + entryName + "\" in \"" + dir.getName() + "\" from " + caller);
		// End logging

		if (dir.createFile(entryName + ".gffs_ln")) {
			String forkPath = formForkPath(entryName  + ".gffs_ln");
			ResourceForkService service = getService();
			// Ok, we created the file. No we need to put the EPR into it.
			BufferedWriter out = null;
			try  
			{
				//System.err.println(dir.getPath() +forkPath);
			    FileWriter fstream = new FileWriter(dir.getPath() +forkPath, false); //true tells to append data.
			    out = new BufferedWriter(fstream);
				ObjectSerializer.serialize(out, entry, new QName(GenesisIIConstants.GENESISII_NS, "endpoint"));
				out.flush();
				out.close();
			}
			catch (IOException e)
			{
			    System.err.println("Error: " + e.getMessage());
			}
			finally
			{
			    if(out != null) {
			        out.close();
			    }
			}

			return service.createForkEPR(forkPath, new LightWeightExportFileFork(service, forkPath).describe());
		}
		throw new IOException("Not allowed to add arbitrary endpoints to a " + "light-weight export.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType createFile(EndpointReferenceType exemplarEPR, String newFileName) throws IOException
	{
		VExportDir dir = getTarget();
		// 2014-11-05 ASG - adding logging
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
		StatsLogger.logStats("LightWeightExport: File Create \"" + newFileName + "\" in \"" + dir.getName() + "\" from " + caller);
		// End logging

		if (dir.createFile(newFileName)) {
			String forkPath = formForkPath(newFileName);
			ResourceForkService service = getService();
			return service.createForkEPR(forkPath, new LightWeightExportFileFork(service, forkPath).describe());
		}

		throw new IOException("Unable to create new file.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR, String entryName) throws IOException
	{
		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();
		VExportDir dir = getTarget();
		// 2014-11-05 ASG - adding logging
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
		StatsLogger.logStats("LightWeightExport: Dir List \"" + dir.getName() + "/" + entryName + "\" from " + caller);
		// End logging
		for (VExportEntry dirEntry : dir.list(entryName)) {
			String dName = dirEntry.getName();
			// Aded 2018-02-08 by ASG to handle links
			String sName="";
			if (dName.endsWith(".gffs_ln")) {
				// First let's get the file name without the suffix
				int index=dName.lastIndexOf(".gffs_ln");
				sName=dName.substring(0, index);
				//System.out.println("sName is " + sName);
			}
			//System.out.println(dName);
			if (entryName == null || entryName.equals(dName) || entryName.equals(sName)) {
				ResourceForkInformation info;
				if (dName.endsWith(".gffs_ln")) {
					entries.add(new InternalEntry(sName,getLnEPR(dName,dir)));
				}	
				else {
					if (dirEntry.isDirectory())
						info = new LightWeightExportDirFork(getService(), formForkPath(dName)).describe();
					else
						info = new LightWeightExportFileFork(getService(), formForkPath(dName)).describe();
					entries.add(createInternalEntry(exemplarEPR, dName, info));
				}
				// ASG 2018-02-09; seems to me that if we found the file we should exit the loop
				if (entryName!=null) break;
			}
		}

		if (entryName != null && entries.size() == 0)
			entries.add(new InternalEntry(entryName, null, null, false));
		return entries;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType mkdir(EndpointReferenceType exemplarEPR, String newDirectoryName) throws IOException
	{
		VExportDir dir = getTarget();
		// 2014-11-05 ASG - adding logging
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
		StatsLogger.logStats("LightWeightExport: Dir Create \"" + dir.getName() + "/" + newDirectoryName + "\" from " + caller);
		// End logging
		if (dir.mkdir(newDirectoryName)) {
			String forkPath = formForkPath(newDirectoryName);
			ResourceForkService service = getService();

			return service.createForkEPR(forkPath, new LightWeightExportDirFork(service, forkPath).describe());
		}

		throw new IOException("Unable to create new directory.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public boolean remove(String entryName) throws IOException
	{
		VExportDir dir = getTarget();
		// 2014-11-05 ASG - adding logging
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
		StatsLogger.logStats("LightWeightExport: Dir Delete " + dir.getName() + "/" + entryName + " from " + caller);
		// End logging
		return dir.remove(entryName);
	}

	@Override
	public boolean isInMemoryIterable() throws IOException
	{
		VExportDir target = getTarget();
		if (target instanceof DiskExportEntry || target instanceof SudoDiskExportEntry)
			return true;

		return false;
	}

	@Override
	public InMemoryIterableFork getInMemoryIterableFork() throws IOException
	{
		if (!isInMemoryIterable())
			return null;

		return this;

	}

	private EndpointReferenceType getLnEPR(String dName, VExportDir dir) {
		//System.out.println("dName is " + dName + ", forkpath is "+getForkPath() + ", getPath() is " + dir.getPath());
		String fPath=dir.getPath()+"/"+dName;
		//System.out.println(fPath);
		ResourceForkService service = getService();
		InputStream in = null;
		EndpointReferenceType epr=null;
		try {
			FileInputStream inp = new FileInputStream(fPath);
			// Ok, we created the file. No we need to put the EPR into it.	
			epr= (EndpointReferenceType)ObjectDeserializer.deserialize(new InputSource(inp), EndpointReferenceType.class);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			StreamUtils.close(in);
		}
		return epr;
	}
	
	@Override
	public IterableSnapshot splitAndList(EndpointReferenceType exemplarEPR, ResourceKey myKey) throws IOException
	{

		if (!isInMemoryIterable())
			throw new IOException("Cannot support in-memory iteration!");

		int count = 0;

		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();
		List<InMemoryIteratorEntry> imieList = new LinkedList<InMemoryIteratorEntry>();

		VExportDir dir = getTarget();
		String dirPath = null;
		if (dir instanceof DiskExportEntry) {
			DiskExportEntry f = (DiskExportEntry) dir;
			dirPath = f.getFileTarget().getAbsolutePath();
		} else if (dir instanceof SudoDiskExportEntry) {
			SudoDiskExportEntry f = (SudoDiskExportEntry) dir;
			dirPath = f.getFileTarget().getAbsolutePath();
		}

		for (VExportEntry dirEntry : dir.list()) {
			String dName = dirEntry.getName();

			if (count < RNSConstants.PREFERRED_BATCH_SIZE) {
				ResourceForkInformation info;
				if (dName.endsWith(".gffs_ln")) {
					String sName="";
					
						// First let's get the file name without the suffix
						int index=dName.lastIndexOf(".gffs_ln");
						sName=dName.substring(0, index);
						//System.out.println("sName is " + sName);
					
						entries.add(new InternalEntry(sName,getLnEPR(dName,dir)));
				}	
				else {
					if (dirEntry.isDirectory())
						info = new LightWeightExportDirFork(getService(), formForkPath(dName)).describe();
					else
						info = new LightWeightExportFileFork(getService(), formForkPath(dName)).describe();entries.add(createInternalEntry(exemplarEPR, dName, info));	
				}

			}

			else {
				InMemoryIteratorEntry imie;
				if (dirEntry.isDirectory())
					imie = new InMemoryIteratorEntry(dName, getForkPath(), true, FileOrDir.DIRECTORY);

				else
					imie = new InMemoryIteratorEntry(dName, getForkPath(), true, FileOrDir.FILE);

				imieList.add(imie);
			}

			count++;
		}

		InMemoryIteratorWrapper imiw = null;

		if (imieList.size() > 0) {
			imiw = new InMemoryIteratorWrapper(this.getClass().getName(), imieList, new Object[] { exemplarEPR, getService(), myKey });
		}

		return new IterableSnapshot(entries, imiw, dirPath);
	}

	static final private Pattern EPI_PATTERN = Pattern.compile("^(.+):fork-path:.+$");

	// move this to a more general location, since we use rns entry responses in several places.
	static public Comparator<RNSEntryResponseType> getComparator()
	{
		return new Comparator<RNSEntryResponseType>()
		{
			@Override
			public int compare(RNSEntryResponseType c1, RNSEntryResponseType c2)
			{
				if ((c1 == null) && (c2 == null))
					return 0;
				if (c1 == null)
					return -1;
				if (c2 == null)
					return 1;
				return c1.getEntryName().compareTo(c2.getEntryName());
			}
		};
	}

	public static Collection<RNSEntryResponseType> getEntries(Iterable<InternalEntry> entries, ResourceKey rKey, boolean requestedShortForm,
		String dirPath) throws ResourceException
	{
		LinkedList<RNSEntryResponseType> resultEntries = new LinkedList<RNSEntryResponseType>();
		RNSEntryResponseType lastF = null;
		RNSEntryResponseType lastD = null;
		QName MODTIME = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.MODTIME_ATTR_NAME);
		QName CREATTIME = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.CREATTIME_ATTR_NAME);
		QName ACCESSTIME = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.ACCESSTIME_ATTR_NAME);
		QName SIZE = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.SIZE_ATTR_NAME);

		if (dirPath == null || rKey == null || entries == null)
			throw new ResourceException("Unable to list directory contents - null arguments to getEntries");
		for (InternalEntry internalEntry : entries) {
			if (internalEntry.isExistent()) {
				boolean isDir = false;
				EndpointReferenceType epr = internalEntry.getEntryReference();

				String dName = internalEntry.getName();

				if (dName == null)
					throw new ResourceException("Unable to list directory contents");

				TypeInformation type = new TypeInformation(epr);
				if (type.isRNS()) {
					isDir = true;
				}
				// Now the code where we hoist out and reuse the values from the first time around
				// We also want to do this the old way if shortForm==false
				if ((!isDir && lastF == null) || (isDir && lastD == null) || requestedShortForm == false) {
					AttributesPreFetcherFactory factory = new LightWeightExportAttributePrefetcherFactoryImpl();
					RNSEntryResponseType entry = new RNSEntryResponseType(requestedShortForm ? null : epr,
						RNSUtilities.createMetadata(epr,
							Prefetcher.preFetch(epr, internalEntry.getAttributes(), factory, rKey, null, requestedShortForm)),
						null, internalEntry.getName());
					// ---------------------------------------------------------------------------------------------
					// Removing EPR from entry when short form is requested
					if (requestedShortForm)
						entry.setEndpoint(null);
					// ---------------------------------------------------------------------------------------------
					resultEntries.add(entry);
					if (!isDir) {
						lastF = entry;
					} else if (isDir) {
						lastD = entry;
					}

				} else {
					// Now we do the rest .. we reuse almost everything from the lastR
					// The first thing we need to is do a semi deep copy
					RNSEntryResponseType ent = null;
					// First we create the new response and set the name
					RNSEntryResponseType next = new RNSEntryResponseType(null, null, null, dName);
					if (!isDir)
						ent = lastF;
					else
						ent = lastD;

					// We need too get the EPI for the current entry, set the epr EPI to the new
					// EPI, and updated the
					// entryName, as well as the size, createTime, modify time, and size attributes
					File forkFile = new File(RForkUtils.formForkPathFromPath(dirPath, dName));
					MessageElement[] me = new MessageElement[ent.getMetadata().get_any().length];

					// What we replace depends on the type
					if (!isDir) {
						if (_logger.isTraceEnabled())
							_logger.debug("handling as file: " + internalEntry.getName());

						MessageElement sz = new MessageElement(SIZE, forkFile.length());
						Calendar c = Calendar.getInstance();
						c.setTimeInMillis(forkFile.lastModified());
						// We have a problem, cannot figure out how to get the right values from
						// Java Files
						MessageElement modtime = new MessageElement(MODTIME, c);
						// c.setTimeInMillis(Files.getLastModifiedTime(forkFile.toPath(),
						// LinkOption.NOFOLLOW_LINKS).toMillis());
						MessageElement createtime = new MessageElement(CREATTIME, c);
						c.setTimeInMillis(forkFile.lastModified());
						MessageElement accesstime = new MessageElement(ACCESSTIME, c);

						// Now replace the message elements
						for (int pos = 0; pos < me.length; pos++) {
							MessageElement element = ent.getMetadata().get_any()[pos];
							QName name = element.getQName();
							// 5/13/2015 ASG
							if (name.equals(GenesisIIConstants.RESOURCE_URI_QNAME)) {
								String epi = element.getValue();
								Matcher matcher = EPI_PATTERN.matcher(epi);
								if (matcher.matches())
									epi = matcher.group(1);
								java.net.URI forkFileURI = forkFile.toURI();
								epi += ":fork-path:" + forkFileURI.getRawPath();
								me[pos] = new MessageElement(GenesisIIConstants.RESOURCE_URI_QNAME, epi);
							} else if (name.equals(WSName.ENDPOINT_IDENTIFIER_QNAME)) {
								// Found the EPI
								String epi = element.getValue();
								Matcher matcher = EPI_PATTERN.matcher(epi);
								if (matcher.matches())
									epi = matcher.group(1);

								java.net.URI forkFileURI = forkFile.toURI();
								epi += ":fork-path:" + forkFileURI.getRawPath();
								me[pos] = new MessageElement(WSName.ENDPOINT_IDENTIFIER_QNAME, epi);
								// Ok, we have updated the EPI, not lets fill in the other things
								// that need filling
							} else if (name.equals(MODTIME)) {
								me[pos] = modtime;
							} else if (name.equals(CREATTIME)) {
								me[pos] = createtime;
							} else if (name.equals(ACCESSTIME)) {
								me[pos] = accesstime;
							} else if (name.equals(SIZE)) {
								me[pos] = sz;
							} else {
								// keep what was there before
								me[pos] = ent.getMetadata().get_any()[pos];
							}
						}
						next.setMetadata(new RNSMetadataType(ent.getMetadata().getSupportsRns(), me));
						resultEntries.add(next);

					} else if (isDir) {
						if (_logger.isTraceEnabled())
							_logger.debug("handling as dir: " + internalEntry.getName());

						int elements = -1;
						ExportMechanisms expMech =
							ExportMechanisms.parse((String) rKey.dereference().getProperty(LightWeightExportConstants.EXPORT_MECHANISM));
						if ((expMech == null) || expMech.equals(ExportMechanisms.EXPORT_MECH_ACL)
							|| expMech.equals(ExportMechanisms.EXPORT_MECH_ACLANDCHOWN)) {
							// normal exports just go directly to a File.
							/* 2018-010-0 by ASG
							 * Fixed the following bug. If the container does not have read/execute permissions on the 
							 * directory about to be read to compute the number of elements, the code faults. what we are going to do instead
							 * is return 0 entries if we have no permission. The old code is:
							 * elements = forkFile.list().length;
							 */	
							elements=0;
							String theElements[] = forkFile.list();
							if (theElements!=null) elements = theElements.length;
							// End changes.
						} else if (expMech.equals(ExportMechanisms.EXPORT_MECH_PROXYIO)) {
							// this export is in proxyio mode which uses sudo and a co-process.
							String user = (String) rKey.dereference().getProperty(LightWeightExportConstants.EXPORT_OWNER_UNIX_NAME);
							if (_logger.isTraceEnabled())
								_logger.debug("trying short-circuit to proxyio, user=" + user + " path='" + forkFile + "'");
							SudoDiskExportEntry sdee;
							try {
								sdee = new SudoDiskExportEntry(forkFile, user);
								elements = sdee.list().size();
							} catch (IOException e) {
								throw new ResourceException("could not translate proxyio export info to get element count", e);
							}
						} else {
							throw new ResourceException("unknown or unimplemented type of export mechanism: " + expMech);
						}

						// Now replace the message elements
						for (int pos = 0; pos < me.length; pos++) {
							MessageElement element = ent.getMetadata().get_any()[pos];
							QName name = element.getQName();
							// 5/13/2015 ASG if (name.equals(WSName.ENDPOINT_IDENTIFIER_QNAME)) {
							if (name.equals(GenesisIIConstants.RESOURCE_URI_QNAME)) {
								// Found the EPI
								String epi = element.getValue();
								Matcher matcher = EPI_PATTERN.matcher(epi);
								if (matcher.matches())
									epi = matcher.group(1);

								java.net.URI forkFileURI = forkFile.toURI();
								epi += ":fork-path:" + forkFileURI.getRawPath();
								// 5/13/2015 ASG me[pos] = new MessageElement(WSName.ENDPOINT_IDENTIFIER_QNAME, epi);
								me[pos] = new MessageElement(GenesisIIConstants.RESOURCE_URI_QNAME, epi);
								// Ok, we have updated the EPI, not lets fill in the other things
								// that need filling
							} else if (name.equals(RNSConstants.ELEMENT_COUNT_QNAME)) {
								me[pos] = new MessageElement(RNSConstants.ELEMENT_COUNT_QNAME, elements);
							} else {
								// keep what was there before
								me[pos] = ent.getMetadata().get_any()[pos];
							}
						}
						next.setMetadata(new RNSMetadataType(ent.getMetadata().getSupportsRns(), me));
						resultEntries.add(next);
					}
				}
			}
		}

		Collections.sort(resultEntries, getComparator());

		return resultEntries;
	}

	public static Collection<Pair<Long, MessageElement>> getEntries(List<InMemoryIteratorEntry> imieList, int firstElement, int numElements,
		Object[] EprAndService, boolean shortForm) throws IOException
	{
		/*
		 * ASG May, 2014. New code to hoist common operations out of getIndexedContent and ws iterator iterate. We will exploit the fact that
		 * this is a light weight export ... in other words that the only things that change from one RNSEntryResponse to another is the entry
		 * name, the size, and the times. Most importantly, the permissions do not change, and those take 50% of the time required to build an
		 * iterate response. The other major factor is CreateForkEPR that takes 7% of the 80% used by iterate. Since only one field of the EPR
		 * changes, we will change only that field.
		 */

		Collection<Pair<Long, MessageElement>> ret = new ArrayList<Pair<Long, MessageElement>>(numElements);
		firstElement = Math.max(firstElement, 0);
		numElements = Math.max(numElements, 0);
		if (EprAndService == null)
			throw new ResourceException("Unable to list directory contents");

		if (EprAndService.length != 3)
			throw new ResourceException("Unable to list directory contents");

		EndpointReferenceType exemplarEPR = (EndpointReferenceType) EprAndService[0];
		if (exemplarEPR == null)
			throw new ResourceException("Unable to list directory contents");

		ResourceForkService service = (ResourceForkService) EprAndService[1];
		if (service == null)
			throw new ResourceException("Unable to list directory contents");

		ResourceKey rKey = (ResourceKey) EprAndService[2];
		if (rKey == null)
			throw new ResourceException("Unable to list directory contents");
		int lastElement = Math.min(firstElement + numElements - 1, imieList.size() - 1);
		//

		RNSEntryResponseType lastF = null;
		RNSEntryResponseType lastD = null;

		QName MODTIME = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.MODTIME_ATTR_NAME);
		QName CREATTIME = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.CREATTIME_ATTR_NAME);
		QName ACCESSTIME = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.ACCESSTIME_ATTR_NAME);
		QName SIZE = new QName(ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.SIZE_ATTR_NAME);

		for (int lcv = firstElement; lcv <= lastElement; lcv++) {
			InMemoryIteratorEntry entry = imieList.get(lcv);
			if (entry != null) {
				String dName = entry.getEntryName();
				if (dName == null)
					throw new ResourceException("Unable to list directory contents");
				String forkPath = entry.getId();
				ResourceForkInformation info = null;
				FileOrDir fd = entry.getType();
				InternalEntry ie;
				RNSEntryResponseType resp = null;

				if (fd == FileOrDir.UNKNOWN) {
					// we identify if it is a file or dir!
					try {
						FileOrDir stat = statify(dName, forkPath, rKey);
						fd = stat;
						if (stat == FileOrDir.DIRECTORY) {
							LightWeightExportDirFork lwedf =
								new LightWeightExportDirFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));
							info = lwedf.describe();
						} else if (stat == FileOrDir.FILE) {
							LightWeightExportFileFork lweff =
								new LightWeightExportFileFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));
							info = lweff.describe();
						} else {
							resp = new RNSEntryResponseType(null, null,
								FaultManipulator.fillInFault(new RNSEntryDoesNotExistFaultType(null, null, null, null,
									new BaseFaultTypeDescription[] {
										new BaseFaultTypeDescription(String.format("Entry" + " %s does not exist!", dName)) },
									null, dName)),
								dName);
							ret.add(new Pair<Long, MessageElement>((long) lcv,
								MessageElementSerializer.serialize(RNSEntryResponseType.getTypeDesc().getXmlType(), resp)));
							continue;
						}
					}

					catch (IOException ioe) {
						throw new ResourceException("Unable to list directory contents");
					}
				}

				else if (fd == FileOrDir.DIRECTORY) {
					LightWeightExportDirFork lwedf = new LightWeightExportDirFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));
					info = lwedf.describe();
				} else if (fd == FileOrDir.FILE) {
					LightWeightExportFileFork lweff =
						new LightWeightExportFileFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));
					info = lweff.describe();
				}
				// Now the code where we hoist out and reuse the values from the first time around
				// We also want to do this the old way if shortForm==false
				if ((fd == FileOrDir.FILE && lastF == null) || (fd == FileOrDir.DIRECTORY && lastD == null) || shortForm == false) {
					try {
						ie = new InternalEntry(dName, service.createForkEPR(RForkUtils.formForkPathFromPath(forkPath, dName), info), null);

					} catch (ResourceUnknownFaultType e) {
						throw new ResourceException("Unable to list directory contents");
					}

					EndpointReferenceType epr = ie.getEntryReference();
					AttributesPreFetcherFactory factory = new LightWeightExportAttributePrefetcherFactoryImpl();

					resp = new RNSEntryResponseType(shortForm ? null : epr,
						RNSUtilities.createMetadata(epr, Prefetcher.preFetch(epr, ie.getAttributes(), factory, rKey, service, shortForm)),
						null, ie.getName());
					ret.add(new Pair<Long, MessageElement>((long) lcv,
						MessageElementSerializer.serialize(RNSEntryResponseType.getTypeDesc().getXmlType(), resp)));

					if (fd == FileOrDir.FILE) {
						lastF = resp;
					} else if (fd == FileOrDir.DIRECTORY) {
						lastD = resp;
					}
					continue;
				} else {
					// Now we do the rest .. we reuse almost everything from the lastR
					// The first thing we need to is do a semi deep copy
					RNSEntryResponseType ent = null;
					// First we create the new response and set the name
					RNSEntryResponseType next = new RNSEntryResponseType(null, null, null, dName);
					if (fd == FileOrDir.FILE)
						ent = lastF;
					else
						ent = lastD;

					// We need too get the EPI for the current entry, set the epr EPI to the new
					// EPI, and updated the
					// entryName, as well as the size, createTime, modify time, and size attributes
					File forkFile = new File(RForkUtils.formForkPathFromPath(forkPath, dName));
					MessageElement[] me = new MessageElement[ent.getMetadata().get_any().length];

					// What we replace depends on the type
					if (fd == FileOrDir.FILE) {
						MessageElement sz = new MessageElement(SIZE, forkFile.length());
						Calendar c = Calendar.getInstance();
						c.setTimeInMillis(forkFile.lastModified());
						// We have a problem, cannot figure out how to get the right values from
						// Java Files
						MessageElement modtime = new MessageElement(MODTIME, c);
						// c.setTimeInMillis(Files.getLastModifiedTime(forkFile.toPath(),
						// LinkOption.NOFOLLOW_LINKS).toMillis());
						MessageElement createtime = new MessageElement(CREATTIME, c);
						c.setTimeInMillis(forkFile.lastModified());
						MessageElement accesstime = new MessageElement(ACCESSTIME, c);

						// Now replace the message elements
						for (int pos = 0; pos < me.length; pos++) {
							MessageElement element = ent.getMetadata().get_any()[pos];
							QName name = element.getQName();
							// 5/13/2015 ASG
							if (name.equals(GenesisIIConstants.RESOURCE_URI_QNAME)) {
								String epi = element.getValue();
								Matcher matcher = EPI_PATTERN.matcher(epi);
								if (matcher.matches())
									epi = matcher.group(1);
								java.net.URI forkFileURI = forkFile.toURI();
								epi += ":fork-path:" + forkFileURI.getRawPath();
								me[pos] = new MessageElement(GenesisIIConstants.RESOURCE_URI_QNAME, epi);
							}
							// End update
							else if (name.equals(WSName.ENDPOINT_IDENTIFIER_QNAME)) {
								// Found the EPI
								String epi = element.getValue();
								Matcher matcher = EPI_PATTERN.matcher(epi);
								if (matcher.matches())
									epi = matcher.group(1);

								java.net.URI forkFileURI = forkFile.toURI();
								epi += ":fork-path:" + forkFileURI.getRawPath();
								// me[pos] = new MessageElement(GenesisIIConstants.RESOURCE_URI_QNAME, epi);
								me[pos] = new MessageElement(WSName.ENDPOINT_IDENTIFIER_QNAME, epi);
								// Ok, we have updated the EPI, not lets fill in the other things
								// that need filling
							} else if (name.equals(MODTIME)) {
								me[pos] = modtime;
							} else if (name.equals(CREATTIME)) {
								me[pos] = createtime;
							} else if (name.equals(ACCESSTIME)) {
								me[pos] = accesstime;
							} else if (name.equals(SIZE)) {
								me[pos] = sz;
							} else {
								// keep what was there before
								me[pos] = ent.getMetadata().get_any()[pos];
							}
						}
						next.setMetadata(new RNSMetadataType(ent.getMetadata().getSupportsRns(), me));

						ret.add(new Pair<Long, MessageElement>((long) lcv,
							MessageElementSerializer.serialize(RNSEntryResponseType.getTypeDesc().getXmlType(), next)));

					} else if (fd == FileOrDir.DIRECTORY) {
						int elements = 0;  //forkFile.list().length;
						/* 2018-010-0 by ASG
						 * Fixed the following bug. If the container does not have read/execute permissions on the 
						 * directory about to be read to compute the number of elements, the code faults. what we are going to do instead
						 * is return 0 entries if we have no permission. The old code is:
						 * elements = forkFile.list().length;
						 */	
						String theElements[] = forkFile.list();
						if (theElements!=null) elements = theElements.length;
						// End changes.
						// Now replace the message elements
						for (int pos = 0; pos < me.length; pos++) {
							MessageElement element = ent.getMetadata().get_any()[pos];
							QName name = element.getQName();
							if (name.equals(WSName.ENDPOINT_IDENTIFIER_QNAME)) {
								// Found the EPI
								String epi = element.getValue();
								Matcher matcher = EPI_PATTERN.matcher(epi);
								if (matcher.matches())
									epi = matcher.group(1);

								java.net.URI forkFileURI = forkFile.toURI();
								epi += ":fork-path:" + forkFileURI.getRawPath();
								me[pos] = new MessageElement(WSName.ENDPOINT_IDENTIFIER_QNAME, epi);
								// Ok, we have updated the EPI, not lets fill in the other things
								// that need filling
							} else if (name.equals(RNSConstants.ELEMENT_COUNT_QNAME)) {
								me[pos] = new MessageElement(RNSConstants.ELEMENT_COUNT_QNAME, elements);
							} else {
								// keep what was there before
								me[pos] = ent.getMetadata().get_any()[pos];
							}
						}
						next.setMetadata(new RNSMetadataType(ent.getMetadata().getSupportsRns(), me));

						ret.add(new Pair<Long, MessageElement>((long) lcv,
							MessageElementSerializer.serialize(RNSEntryResponseType.getTypeDesc().getXmlType(), ent)));

					}
				}
			}
		}
		return ret;
	}

	public static MessageElement getIndexedContent(Connection connection, InMemoryIteratorEntry entry, Object[] EprAndService,
		boolean shortForm) throws ResourceException
	{
		if (EprAndService == null)
			throw new ResourceException("Unable to list directory contents");

		if (EprAndService.length != 3)
			throw new ResourceException("Unable to list directory contents");

		EndpointReferenceType exemplarEPR = (EndpointReferenceType) EprAndService[0];
		if (exemplarEPR == null)
			throw new ResourceException("Unable to list directory contents");

		ResourceForkService service = (ResourceForkService) EprAndService[1];
		if (service == null)
			throw new ResourceException("Unable to list directory contents");

		ResourceKey rKey = (ResourceKey) EprAndService[2];
		if (rKey == null)
			throw new ResourceException("Unable to list directory contents");

		String dName = entry.getEntryName();

		if (dName == null)
			throw new ResourceException("Unable to list directory contents");

		String forkPath = entry.getId();
		ResourceForkInformation info = null;
		FileOrDir fd = entry.getType();
		InternalEntry ie;
		RNSEntryResponseType resp = null;

		if (fd == FileOrDir.UNKNOWN) {
			// we identify if it is a file or dir!
			try {
				FileOrDir stat = statify(dName, forkPath, rKey);
				if (stat == FileOrDir.DIRECTORY) {
					LightWeightExportDirFork lwedf = new LightWeightExportDirFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));
					info = lwedf.describe();
				}

				else if (stat == FileOrDir.FILE) {
					LightWeightExportFileFork lweff =
						new LightWeightExportFileFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));

					info = lweff.describe();
				}

				else {

					resp =
						new RNSEntryResponseType(null, null,
							FaultManipulator.fillInFault(new RNSEntryDoesNotExistFaultType(null, null, null, null,
								new BaseFaultTypeDescription[] {
									new BaseFaultTypeDescription(String.format("Entry" + " %s does not exist!", dName)) },
								null, dName)),
							dName);

					return MessageElementSerializer.serialize(RNSEntryResponseType.getTypeDesc().getXmlType(), resp);
				}
			}

			catch (IOException ioe) {
				throw new ResourceException("Unable to list directory contents");
			}
		}

		else if (fd == FileOrDir.DIRECTORY) {
			LightWeightExportDirFork lwedf = new LightWeightExportDirFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));
			info = lwedf.describe();

		}

		else if (fd == FileOrDir.FILE) {
			LightWeightExportFileFork lweff = new LightWeightExportFileFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));

			info = lweff.describe();
		}

		try {
			ie = new InternalEntry(dName, service.createForkEPR(RForkUtils.formForkPathFromPath(forkPath, dName), info), null);

		} catch (ResourceUnknownFaultType e) {
			throw new ResourceException("Unable to list directory contents");
		}

		EndpointReferenceType epr = ie.getEntryReference();
		AttributesPreFetcherFactory factory = new LightWeightExportAttributePrefetcherFactoryImpl();

		resp = new RNSEntryResponseType(shortForm ? null : epr,
			RNSUtilities.createMetadata(epr, Prefetcher.preFetch(epr, ie.getAttributes(), factory, rKey, service, shortForm)), null,
			ie.getName());

		return MessageElementSerializer.serialize(RNSEntryResponseType.getTypeDesc().getXmlType(), resp);

	}

	private static FileOrDir statify(String entryName, String forkPath, ResourceKey rKey) throws IOException
	{
		VExportDir dir = LightWeightExportUtils.getDirectory(forkPath, rKey);

		for (VExportEntry dirEntry : dir.list(entryName)) {
			String dName = dirEntry.getName();

			if (entryName.equals(dName)) {

				if (dirEntry.isDirectory())
					return FileOrDir.DIRECTORY;
				else
					return FileOrDir.FILE;
			}
		}
		return FileOrDir.UNKNOWN;
	}

	@Override
	public IterableSnapshot splitAndList(String[] lookupRequest, EndpointReferenceType exemplarEPR, ResourceKey resourceKey)
		throws IOException
	{

		if (!isInMemoryIterable())
			throw new IOException("Cannot support in-memory iteration!");

		if (lookupRequest == null || lookupRequest.length == 0)
			return splitAndList(exemplarEPR, resourceKey);

		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();
		List<InMemoryIteratorEntry> imieList = new LinkedList<InMemoryIteratorEntry>();

		int min = (lookupRequest.length > RNSConstants.PREFERRED_BATCH_SIZE) ? RNSConstants.PREFERRED_BATCH_SIZE : lookupRequest.length;

		for (int lcv = 0; lcv < min; lcv++) {
			String request = lookupRequest[lcv];
			if ((list(exemplarEPR, request)).iterator().hasNext())
				entries.add(list(exemplarEPR, request).iterator().next());
		}

		if (lookupRequest.length <= RNSConstants.PREFERRED_BATCH_SIZE) {
			// WE WILL NOT BE DOING in-memory iteration
			return new IterableSnapshot(entries, null, getForkPath());
		}

		else {
			// we will be doing in-memory iteration
			InMemoryIteratorWrapper imiw = null;
			for (int lcv = min; lcv < lookupRequest.length; lcv++) {
				String request = lookupRequest[lcv];

				/*
				 * We put a true for exists! It does not matter if it is a false. We will identify and throw a fault during iteration!
				 */

				InMemoryIteratorEntry imie = new InMemoryIteratorEntry(request, getForkPath(), true, FileOrDir.UNKNOWN);
				imieList.add(imie);
			}

			imiw = new InMemoryIteratorWrapper(this.getClass().getName(), imieList, new Object[] { exemplarEPR, getService(), resourceKey });

			return new IterableSnapshot(entries, imiw, getForkPath());
		}

	}

}
