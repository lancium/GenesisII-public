package edu.virginia.vcgr.genii.container.rfork.sfd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.rfork.DefaultResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ReadOnlyRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

/**
 * StaticRNSResourceForks are forks that have static (in terms of contents) entries. In fact, they
 * can change any time the resource is constructed, but that behavior is incidental and should not
 * be relied upon.
 * 
 * @author mmm2a
 */
public abstract class StaticRNSResourceFork extends ReadOnlyRNSResourceFork
{
	private Map<String, ResourceForkInformation> _entries;

	protected StaticRNSResourceFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);

		_entries = new HashMap<String, ResourceForkInformation>();
		addEntries(_entries);
	}

	/**
	 * This is a convenience method which allows a sub-class to easily add an entry which uses the
	 * default resource fork information structure.
	 * 
	 * @param entryName
	 *            The new entry's name.
	 * @param forkClass
	 *            The resource fork's class.
	 */
	protected void addDefaultEntry(String entryName, Class<? extends ResourceFork> forkClass)
	{
		_entries.put(entryName, new DefaultResourceForkInformation(forkClass, getForkPath() + "/" + entryName));
	}

	/**
	 * This abstract method is called automatically to add the entries into this static resource
	 * fork.
	 * 
	 * @param entries
	 *            The map of entries the resource fork contains.
	 */
	protected abstract void addEntries(Map<String, ResourceForkInformation> entries);

	/** {@inheritDoc} */
	@Override
	@RWXMapping(RWXCategory.READ)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR, String entryName) throws IOException
	{
		Collection<InternalEntry> ret = new ArrayList<InternalEntry>(_entries.size());

		if (entryName == null) {
			for (String entry : _entries.keySet()) {
				ResourceForkInformation rif = _entries.get(entry);
				ret.add(createInternalEntry(exemplarEPR, entry, rif));
			}
		} else {
			ResourceForkInformation rif = _entries.get(entryName);
			if (rif != null)
				ret.add(createInternalEntry(exemplarEPR, entryName, rif));
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public ResourceForkInformation describe()
	{
		return new DefaultResourceForkInformation(getClass(), getForkPath());
	}
}