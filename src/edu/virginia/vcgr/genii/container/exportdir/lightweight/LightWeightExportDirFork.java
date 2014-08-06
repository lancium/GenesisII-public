package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.RNSEntryDoesNotExistFaultType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSMetadataType;
import org.morgan.util.Pair;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
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
// , GeniiNoOutCalls
{
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
	public EndpointReferenceType add(EndpointReferenceType exemplarEPR, String entryName, EndpointReferenceType entry)
		throws IOException
	{
		throw new IOException("Not allowed to add arbitrary endpoints to a " + "light-weight export.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType createFile(EndpointReferenceType exemplarEPR, String newFileName) throws IOException
	{
		VExportDir dir = getTarget();
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

		for (VExportEntry dirEntry : dir.list(entryName)) {
			String dName = dirEntry.getName();

			if (entryName == null || entryName.equals(dName)) {
				ResourceForkInformation info;

				if (dirEntry.isDirectory())
					info = new LightWeightExportDirFork(getService(), formForkPath(dName)).describe();
				else
					info = new LightWeightExportFileFork(getService(), formForkPath(dName)).describe();

				entries.add(createInternalEntry(exemplarEPR, dName, info));
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
		}

		for (VExportEntry dirEntry : dir.list()) {
			String dName = dirEntry.getName();

			if (count < RNSConstants.PREFERRED_BATCH_SIZE) {

				ResourceForkInformation info;

				if (dirEntry.isDirectory())
					info = new LightWeightExportDirFork(getService(), formForkPath(dName)).describe();
				else
					info = new LightWeightExportFileFork(getService(), formForkPath(dName)).describe();

				entries.add(createInternalEntry(exemplarEPR, dName, info));

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
			imiw =
				new InMemoryIteratorWrapper(this.getClass().getName(), imieList, new Object[] { exemplarEPR, getService(),
					myKey });
		}

		return new IterableSnapshot(entries, imiw, dirPath);
	}

	static final private Pattern EPI_PATTERN = Pattern.compile("^(.+):fork-path:.+$");

	public static Collection<RNSEntryResponseType> getEntries(Iterable<InternalEntry> entries, ResourceKey rKey,
		boolean requestedShortForm, String dirPath) throws ResourceException
	{
		Collection<RNSEntryResponseType> resultEntries = new LinkedList<RNSEntryResponseType>();
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
					RNSEntryResponseType entry =
						new RNSEntryResponseType(requestedShortForm ? null : epr, RNSUtilities.createMetadata(epr,
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
						int elements = forkFile.list().length;

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
						resultEntries.add(next);
					}
				}
			}
		}
		return resultEntries;
	}

	public static Collection<Pair<Long, MessageElement>> getEntries(List<InMemoryIteratorEntry> imieList, int firstElement,
		int numElements, Object[] EprAndService, boolean shortForm) throws IOException
	{
		// ASG May, 2014. New code to hoist common operations out of getIndexedContent and ws
		// iterator iterate. We will exploit the fact
		// that this is a light weight export ... in other words that the only things that change
		// from one RNSEntryResponse to another is the
		// entry name, the size, and the times. Most importantly, the permissions do not change, and
		// those take 50% of the time required to build
		// an iterate response.
		// The other major factor is CreateForkEPR that takes 7% of the 80% used by iterate. Since
		// only one field of the EPR changes, we will
		// change only that field.

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
							resp =
								new RNSEntryResponseType(null, null,
									FaultManipulator.fillInFault(new RNSEntryDoesNotExistFaultType(null, null, null, null,
										new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(String.format("Entry"
											+ " %s does not exist!", dName)) }, null, dName)), dName);
							ret.add(new Pair<Long, MessageElement>((long) lcv, MessageElementSerializer.serialize(
								RNSEntryResponseType.getTypeDesc().getXmlType(), resp)));
							continue;
						}
					}

					catch (IOException ioe) {
						throw new ResourceException("Unable to list directory contents");
					}
				}

				else if (fd == FileOrDir.DIRECTORY) {
					LightWeightExportDirFork lwedf =
						new LightWeightExportDirFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));
					info = lwedf.describe();
				} else if (fd == FileOrDir.FILE) {
					LightWeightExportFileFork lweff =
						new LightWeightExportFileFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));
					info = lweff.describe();
				}
				// Now the code where we hoist out and reuse the values from the first time around
				// We also want to do this the old way if shortForm==false
				if ((fd == FileOrDir.FILE && lastF == null) || (fd == FileOrDir.DIRECTORY && lastD == null)
					|| shortForm == false) {
					try {
						ie =
							new InternalEntry(dName, service.createForkEPR(RForkUtils.formForkPathFromPath(forkPath, dName),
								info), null);

					} catch (ResourceUnknownFaultType e) {
						throw new ResourceException("Unable to list directory contents");
					}

					EndpointReferenceType epr = ie.getEntryReference();
					AttributesPreFetcherFactory factory = new LightWeightExportAttributePrefetcherFactoryImpl();

					resp =
						new RNSEntryResponseType(shortForm ? null : epr, RNSUtilities.createMetadata(epr,
							Prefetcher.preFetch(epr, ie.getAttributes(), factory, rKey, service, shortForm)), null,
							ie.getName());
					ret.add(new Pair<Long, MessageElement>((long) lcv, MessageElementSerializer.serialize(RNSEntryResponseType
						.getTypeDesc().getXmlType(), resp)));

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

						ret.add(new Pair<Long, MessageElement>((long) lcv, MessageElementSerializer.serialize(
							RNSEntryResponseType.getTypeDesc().getXmlType(), next)));

					} else if (fd == FileOrDir.DIRECTORY) {
						int elements = forkFile.list().length;

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

						ret.add(new Pair<Long, MessageElement>((long) lcv, MessageElementSerializer.serialize(
							RNSEntryResponseType.getTypeDesc().getXmlType(), ent)));

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
					LightWeightExportDirFork lwedf =
						new LightWeightExportDirFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));
					info = lwedf.describe();
				}

				else if (stat == FileOrDir.FILE) {
					LightWeightExportFileFork lweff =
						new LightWeightExportFileFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));

					info = lweff.describe();
				}

				else {

					resp =
						new RNSEntryResponseType(null, null, FaultManipulator.fillInFault(new RNSEntryDoesNotExistFaultType(
							null, null, null, null, new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(String
								.format("Entry" + " %s does not exist!", dName)) }, null, dName)), dName);

					return MessageElementSerializer.serialize(RNSEntryResponseType.getTypeDesc().getXmlType(), resp);
				}
			}

			catch (IOException ioe) {
				throw new ResourceException("Unable to list directory contents");
			}
		}

		else if (fd == FileOrDir.DIRECTORY) {
			LightWeightExportDirFork lwedf =
				new LightWeightExportDirFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));
			info = lwedf.describe();

		}

		else if (fd == FileOrDir.FILE) {
			LightWeightExportFileFork lweff =
				new LightWeightExportFileFork(service, RForkUtils.formForkPathFromPath(forkPath, dName));

			info = lweff.describe();
		}

		try {
			ie = new InternalEntry(dName, service.createForkEPR(RForkUtils.formForkPathFromPath(forkPath, dName), info), null);

		} catch (ResourceUnknownFaultType e) {
			throw new ResourceException("Unable to list directory contents");
		}

		EndpointReferenceType epr = ie.getEntryReference();
		AttributesPreFetcherFactory factory = new LightWeightExportAttributePrefetcherFactoryImpl();

		resp =
			new RNSEntryResponseType(shortForm ? null : epr, RNSUtilities.createMetadata(epr,
				Prefetcher.preFetch(epr, ie.getAttributes(), factory, rKey, service, shortForm)), null, ie.getName());

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

		int min =
			(lookupRequest.length > RNSConstants.PREFERRED_BATCH_SIZE) ? RNSConstants.PREFERRED_BATCH_SIZE
				: lookupRequest.length;

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
				 * We put a true for exists! It does not matter if it is a false. We will identify
				 * and throw a fault during iteration!
				 */

				InMemoryIteratorEntry imie = new InMemoryIteratorEntry(request, getForkPath(), true, FileOrDir.UNKNOWN);
				imieList.add(imie);
			}

			imiw =
				new InMemoryIteratorWrapper(this.getClass().getName(), imieList, new Object[] { exemplarEPR, getService(),
					resourceKey });

			return new IterableSnapshot(entries, imiw, getForkPath());
		}

	}

}
