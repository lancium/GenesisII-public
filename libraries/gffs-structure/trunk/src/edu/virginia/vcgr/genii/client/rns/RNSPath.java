/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.virginia.vcgr.genii.client.rns;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSMetadataType;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.filters.FilePatternFilterFactory;
import edu.virginia.vcgr.genii.client.rns.filters.Filter;
import edu.virginia.vcgr.genii.client.rns.filters.FilterFactory;
import edu.virginia.vcgr.genii.client.rns.filters.RNSFilter;
import edu.virginia.vcgr.genii.client.rp.DefaultSingleResourcePropertyTranslator;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;

/**
 * The RNSPath class is the main client side interface between developers and the grid directory
 * structure. Nearly all directory related operations (including creating files and destroying
 * instances), should be handled through instances of this class. You will also notice that there
 * are very few public constructors for this class. This is intentional. As a general rule of thumb,
 * you should always get your "current" path instance from the
 * edu.virginia.vcgr.htc.client.context.ContextManager class.
 * 
 * @author Mark Morgan
 */
public class RNSPath implements Serializable, Cloneable
{
	static final long serialVersionUID = -5879165350773440573L;

	static private Log _logger = LogFactory.getLog(RNSPath.class);

	static public char SEPARATOR = '/';

	private RNSPath _parent;
	private String _nameFromParent;
	private EndpointReferenceType _cachedEPR;
	private boolean _attemptedResolve;

	// 2 properties to cope with short RNS responses
	private String _stringPortTypes;
	private URI _resourceURI;

	static public interface RNSPathApplyFunction
	{
		// if the apply iteration should stop, then the derived method should return false.
		public boolean applyToPath(RNSPath applyTo) throws RNSException, AuthZSecurityException;

		// this indicates if a an EPR is mandatory or not to apply the applyToPath function on an
		// RNSPath
		public boolean canWorkWithShortForm();
	}

	/**
	 * Construct a new RNS path based off of component information. While it is permitted for users
	 * to call this constructor directly, in general it is recommended that RNSPath instances be
	 * obtained through other mechanisms such as calls to RNSPath.getCurrent() and by looking up
	 * entries within other directories.
	 * 
	 * @param parent
	 *            The RNSPath for the parent under which this entry exists.
	 * @param nameFromParent
	 *            The name that this entry has inside of the parent directory.
	 * @param cachedEPR
	 *            The EPR of this entry (if it has one).
	 * @param attemptedResolve
	 *            Should we attempt to resolve the EPR of this entry if we don't have any EPR yet.
	 */
	public RNSPath(RNSPath parent, String nameFromParent, EndpointReferenceType cachedEPR, boolean attemptedResolve)
	{
		_parent = parent;
		_nameFromParent = nameFromParent;
		_cachedEPR = cachedEPR;
		_attemptedResolve = attemptedResolve;

		if ((_parent == null && _nameFromParent != null) || (_parent != null && _nameFromParent == null)) {
			throw new IllegalArgumentException("The parent and the nameFromParent parameters must either "
				+ "both be null, or both be non-null.");
		}

		if (_parent == null && _cachedEPR == null)
			throw new IllegalArgumentException("Cannot have a null EPR for the root.");

		if (_cachedEPR == null) {
			_cachedEPR = (EndpointReferenceType) CacheManager.getItemFromCache(pwd(), EndpointReferenceType.class);
		}
		if (_cachedEPR != null) {
			_attemptedResolve = true;
			storeResourceConfigInCache();
		}

		if (_logger.isTraceEnabled())
			_logger.debug("++ creating RNSPath for path: " + pwd());
	}

	/**
	 * Create a new RNSPath which represents a new rooted RNS namespace at the given EPR.
	 * 
	 * @param root
	 *            The EPR which represents the root of this new namespace.
	 */
	public RNSPath(EndpointReferenceType root)
	{
		this(null, null, root, true);
	}

	public void finalize()
	{
		if (_logger.isTraceEnabled())
			_logger.debug("-- cleaning RNSPath at path: " + pwd());
	}

	/**
	 * Returns the current grid namespace path. This is similar to getcwd in posix systems but
	 * refers only to grid paths here.
	 * 
	 * @return The grid client's current working directory in the grid namespace.
	 */
	static public RNSPath getCurrent()
	{
		try {
			RNSPath toReturn = ContextManager.getExistingContext().getCurrentPath();
			if (toReturn == null) {
				_logger.error("got a null current path for RNS.");
			} else {
				if (_logger.isTraceEnabled())
					_logger.trace("RNSPath: getCurrent = " + toReturn.toString());
			}
			return toReturn;
		} catch (IOException ioe) {
			throw new ConfigurationException("Unable to get current path.", ioe);
		}
	}

	static private <Type> Type createProxy(EndpointReferenceType epr, Class<Type> cl)
	{
		try {
			return ClientUtils.createProxy(cl, epr);
		} catch (GenesisIISecurityException gse) {
			throw new SecurityException("Unable to create Genesis II proxy.", gse);
		} catch (ResourceException re) {
			throw new RuntimeException("Unknown exception occurred trying to create proxy.", re);
		}
	}

	private EndpointReferenceType resolveRequired() throws RNSPathDoesNotExistException
	{
		EndpointReferenceType epr = resolveOptional();
		if (epr == null)
			throw new RNSPathDoesNotExistException(pwd());

		return epr;
	}

	private EndpointReferenceType resolveOptional()
	{
		try {
			if (_cachedEPR == null && !_attemptedResolve) {
				_attemptedResolve = true;
				EndpointReferenceType parent = _parent.resolveOptional();
				if (parent != null) {
					RNSLegacyProxy proxy = new RNSLegacyProxy(createProxy(parent, EnhancedRNSPortType.class));
					RNSEntryResponseType[] entries = proxy.lookup(getName());
					if (entries != null && entries.length == 1) {
						_cachedEPR = entries[0].getEndpoint();
						storeResourceConfigInCache();
					}
				}
			}
		} catch (RemoteException re) {
			if (_logger.isDebugEnabled())
				_logger.debug("Error looking up path component.", re);
		}

		return _cachedEPR;
	}

	/**
	 * Turn the current RNSPath into a sandbox. This essentially makes the current grid directory
	 * the root of a new namespace.
	 * 
	 * @return The RNSPath of a new sandbox namespace rooted at the represented grid directory.
	 * 
	 * @throws RNSPathDoesNotExistException
	 */
	public RNSPath createSandbox() throws RNSPathDoesNotExistException
	{
		EndpointReferenceType newRoot = resolveRequired();
		TypeInformation typeInfo = new TypeInformation(newRoot);
		if (!typeInfo.isRNS())
			throw new RNSPathDoesNotExistException("Path \"" + pwd() + "\" does not indicate a directory.");

		return new RNSPath(newRoot);
	}

	/**
	 * Retrieve the name of this entry as represented by the parent directory.
	 * 
	 * @return The name of this RNS entry.
	 */
	public String getName()
	{
		if (_nameFromParent == null)
			return "/";

		return _nameFromParent;
	}

	/**
	 * Get the EPR of the current entry if it exists. If the entry doesn't exist, this method throws
	 * an exception.
	 * 
	 * @return The EPR of this entry if it exists.
	 * 
	 * @throws RNSPathDoesNotExistException
	 */
	public EndpointReferenceType getEndpoint() throws RNSPathDoesNotExistException
	{
		return resolveRequired();
	}

	/**
	 * Get the EPR of the RNSPath if it exists in the cache. This is useful when a call is made from
	 * some cache management subroutine where we don't want to have any unaccounted outcall.
	 * 
	 * @return the EPR of this entry if it exists in the cache
	 * */
	public EndpointReferenceType getCachedEPR()
	{
		return _cachedEPR;
	}

	/**
	 * Return the full path to this entry starting at the root of the namespace.
	 * 
	 * @return A slash separated string representing the full grid path to this entry.
	 */
	private String _lastPwd = null;

	public String pwd()
	{
		if (_lastPwd != null)
			return _lastPwd;
		String tempCompare = _lastPwd;

		if (_parent == null) {
			_lastPwd = "/";
		} else {
			String parent = _parent.pwd();
			if (parent.equals("/"))
				_lastPwd = parent + getName();
			else
				_lastPwd = parent + "/" + getName();
		}

		if ((tempCompare != null) && !tempCompare.equals(_lastPwd)) {
			_logger.error("created a different last pwd !!!!  this approach cannot work right!!!");
		}

		return _lastPwd;
	}

	/**
	 * Test to see if two RNS paths are equal. This comparison is for String path representation
	 * only (no comparison of EPRs or other metadata is done).
	 * 
	 * @param other
	 *            The other path to compare against.
	 * @return True if the two paths are equal, false otherwise.
	 */
	public boolean equals(RNSPath other)
	{
		return pwd().equals(other.pwd());
	}

	/**
	 * Test to see if two RNS paths are equal. This comparison is for String path representation
	 * only (no comparison of EPRs or other metadata is done).
	 * 
	 * @param other
	 *            The other path to compare against.
	 * @return True if the two paths are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof RNSPath)
			return equals((RNSPath) other);

		return false;
	}

	/**
	 * Calculate a hashcode for the path represented by this RNS path.
	 */
	@Override
	public int hashCode()
	{
		return pwd().hashCode();
	}

	/**
	 * Retrieve the root RNSPath entry for this namespace.
	 * 
	 * @return The root RNSPath entry for this namespace.
	 */
	public RNSPath getRoot()
	{
		if (_parent == null)
			return this;

		return _parent.getRoot();
	}

	/**
	 * Retrieve the parent RNSPath entry for this entry.
	 * 
	 * @return The parent RNSPath entry for this entry.
	 */
	public RNSPath getParent()
	{
		if (_parent == null)
			return this;

		return _parent;
	}

	/**
	 * Determines if this RNSPath entry represents the root of a namespace.
	 * 
	 * @return True if this entry is the root of a namespace, false otherwise.
	 */
	public boolean isRoot()
	{
		return _parent == null;
	}

	/**
	 * Determine if this entry represents a grid resource that exists (has an EPR) or doesn't.
	 * 
	 * @return True if this entry has an EPR, false otherwise.
	 */
	public boolean exists()
	{
		return resolveOptional() != null;
	}

	/**
	 * Assuming that this entry doesn't exist, this method creates a new directory at the indicated
	 * path.
	 * 
	 * @throws RNSException
	 * @throws RNSPathAlreadyExistsException
	 * @throws RNSPathDoesNotExistException
	 */
	public void mkdir() throws RNSException, RNSPathAlreadyExistsException, RNSPathDoesNotExistException
	{
		if (exists())
			throw new RNSPathAlreadyExistsException(pwd());

		if (_parent == null)
			throw new RNSException("Someone tried to create the root directory, " + "which can't be done.");

		EndpointReferenceType parentEndpoint = _parent.resolveRequired();

		RNSLegacyProxy proxy = new RNSLegacyProxy(createProxy(parentEndpoint, EnhancedRNSPortType.class));
		try {
			_cachedEPR = proxy.add(getName());
			_attemptedResolve = true;
			storeResourceConfigInCache();
		} catch (RemoteException re) {
			throw new RNSException("Unable to add new directory.", re);
		}
	}

	/**
	 * Similar to mkdir(), but this operation creates all directories that don't exist in the
	 * indicated path, including this one.
	 * 
	 * @throws RNSException
	 * @throws RNSPathAlreadyExistsException
	 * @throws RNSPathDoesNotExistException
	 */
	public void mkdirs() throws RNSException, RNSPathAlreadyExistsException, RNSPathDoesNotExistException
	{
		if (exists())
			throw new RNSPathAlreadyExistsException(pwd());

		if (_parent == null)
			throw new RNSException("Someone tried to create the root directory, " + "which can't be done.");

		if (!_parent.exists())
			_parent.mkdirs();

		mkdir();
	}

	/**
	 * Creates a new ByteIO file at this path. This operation assumes that the current path doesn't
	 * yet exist. The type of ByteIO created can be any valid ByteIO implementation that the parent
	 * directory decides to create.
	 * 
	 * @return The EPR of a newly created ByteIO file.
	 * 
	 * @throws RNSPathAlreadyExistsException
	 * @throws RNSPathDoesNotExistException
	 * @throws RNSException
	 */
	public EndpointReferenceType createNewFile() throws RNSPathAlreadyExistsException, RNSPathDoesNotExistException,
		RNSException
	{
		if (exists())
			throw new RNSPathAlreadyExistsException(pwd());

		if (_parent == null)
			throw new RNSException("Someone tried to create a file as the root directory.");

		EndpointReferenceType parentEPR = _parent.resolveRequired();

		RNSLegacyProxy proxy = new RNSLegacyProxy(createProxy(parentEPR, EnhancedRNSPortType.class));
		try {
			_cachedEPR = proxy.createFile(getName());
			_attemptedResolve = true;
			storeResourceConfigInCache();
			return _cachedEPR;
		} catch (RemoteException re) {
			throw new RNSException("Unable to create new file.", re);
		}
	}

	private void arrayify(ArrayList<RNSPath> rep)
	{
		if (_parent != null)
			_parent.arrayify(rep);
		rep.add(this);
	}

	/**
	 * Lookup an RNSPath based off of this path. This path can be relative or absolute compared to
	 * this path. The path does not have to exist either. If the indicated path does not exist, an
	 * RNSPath entry with no EPR will be returned.
	 * 
	 * @param path
	 *            The relative or absolute path to lookup.
	 * 
	 * @return An RNSPath entry representing the path looked up.
	 */
	public RNSPath lookup(String path)
	{
		try {
			if (_logger.isDebugEnabled())
				_logger.debug("looking up path: " + path);
			return lookup(path, RNSPathQueryFlags.DONT_CARE);
		} catch (RNSPathDoesNotExistException e) {
			_logger.error("unexpected exception: " + e.getLocalizedMessage(), e);
		} catch (RNSPathAlreadyExistsException e) {
			_logger.error("unexpected exception: " + e.getLocalizedMessage(), e);
		}
		return null;
	}

	/**
	 * the "real" lookup function, which also specifies how the path should be located.
	 */
	public RNSPath lookup(String path, RNSPathQueryFlags queryFlag) throws RNSPathDoesNotExistException,
		RNSPathAlreadyExistsException
	{
		if (path == null)
			throw new IllegalArgumentException("Cannot lookup a path which is null.");

		String[] pathElements = PathUtils.normalizePath(pwd(), path);
		ArrayList<RNSPath> arrayRep = new ArrayList<RNSPath>();
		arrayify(arrayRep);

		int lcv = 0;
		while (true) {
			if (lcv >= pathElements.length)
				break;
			if (lcv + 1 >= arrayRep.size())
				break;
			if (!pathElements[lcv].equals(arrayRep.get(lcv + 1).getName()))
				break;
			lcv++;
		}

		if (lcv >= pathElements.length) {
			// We completely matched a portion of the original path
			return arrayRep.get(lcv);
		}

		RNSPath next = arrayRep.get(lcv);
		for (; lcv < pathElements.length; lcv++) {
			next = new RNSPath(next, pathElements[lcv], null, false);
		}

		if (queryFlag.equals(RNSPathQueryFlags.MUST_EXIST)) {
			if (!next.exists())
				throw new RNSPathDoesNotExistException(next.pwd());
		} else if (queryFlag.equals(RNSPathQueryFlags.MUST_NOT_EXIST)) {
			if (next.exists())
				throw new RNSPathAlreadyExistsException(next.pwd());
		}

		return next;
	}

	private Collection<RNSPath> expand(RNSPath parent, String[] pathElements, int nextElement, FilterFactory filterType)
	{
		Collection<RNSPath> ret = new LinkedList<RNSPath>();
		Collection<RNSPath> tmp;

		if (nextElement >= pathElements.length)
			ret.add(parent);
		else {
			String element = pathElements[nextElement];

			try {
				TypeInformation typeInfo = new TypeInformation(parent.getEndpoint());
				if (typeInfo.isRNS()) {
					if (_logger.isDebugEnabled())
						_logger.debug("Attempting to list contents of \"" + parent + "\".");

					// we first check if we need to create a filter !
					boolean isFilterReqd = filterType.isFilterNeeded(element);

					if (isFilterReqd) {
						Filter filter = filterType.createFilter(element);
						for (RNSPath candidate : parent.listContents()) {
							if (filter.matches(candidate.getName())) {
								tmp = expand(candidate, pathElements, nextElement + 1, filterType);
								if (tmp != null)
									ret.addAll(tmp);
							}
						}
					} else {
						/*
						 * Even though this maybe just a single lookup-call with a single-element we
						 * should iterate. This is because the container can set its
						 * preferred-batch-size to 0 and also not return an initial-block, meaning
						 * we must do an iterate-call.
						 */

						for (RNSPath candidate : parent.listContents(element)) {
							if (element.equals(candidate.getName())) {
								tmp = expand(candidate, pathElements, nextElement + 1, filterType);
								if (tmp != null)
									ret.addAll(tmp);
							}
						}
					}
				}
			} catch (RNSException rne) {
				if (_logger.isDebugEnabled())
					_logger.debug("Skipping a directory in an RSNPath expansion which can't be expanded.", rne);
			}
		}

		if (ret.size() == 0)
			return null;

		return ret;
	}

	/**
	 * A utility operation that looks up a path expression and returns exactly one matching path
	 * entry. If more then one path entry matching the pathExpression, an exception is thrown.
	 * 
	 * @param pathExpression
	 *            A path expression which is to be looked up. This path expression can contain
	 *            standard file system globbing patterns such as *.
	 * 
	 * @return The resultant RNSPath entry (if any).
	 * @throws RNSMultiLookupResultException
	 */
	public RNSPath expandSingleton(String pathExpression) throws RNSMultiLookupResultException
	{
		return expandSingleton(pathExpression, null);
	}

	/**
	 * This operation looks up pathExpressions and returns the exact matching entry that is found if
	 * any.
	 * 
	 * @param pathExpression
	 *            The path expression to lookup. This expression will be matched against the
	 *            filterType indicated.
	 * @param filterType
	 *            A filter which figures out how to expand the pathExpression language given. Two
	 *            pathExpressio filterTypes are available by default -- one parses file globbing
	 *            patterns, the other parses Regular Expressions.
	 * 
	 * @return The matched RNSPath entry.
	 * @throws RNSMultiLookupResultException
	 */
	public RNSPath expandSingleton(String pathExpression, FilterFactory filterType) throws RNSMultiLookupResultException
	{
		Collection<RNSPath> ret = expand(pathExpression, filterType);

		if (ret.size() < 1)
			return null;
		else if (ret.size() > 1)
			throw new RNSMultiLookupResultException(pathExpression);

		return ret.iterator().next();
	}

	/**
	 * Similar to expandSingleton above, but this version of the operation matches 0 or more
	 * entries.
	 * 
	 * @param pathExpression
	 *            The file pattern globbing path expression to lookup.
	 * 
	 * @return A collection of zero or more RNSPath entries that matched the query.
	 */
	public Collection<RNSPath> expand(String pathExpression)
	{
		return expand(pathExpression, new FilePatternFilterFactory());
	}

	/**
	 * Similar to the expandSingleton operation above except that this version of the operation can
	 * return 0 or more entries that match the path query.
	 * 
	 * @param pathExpression
	 *            The path expression to lookup.
	 * @param filterType
	 *            The file pattern matcher to use.
	 * 
	 * @return The RNSPath entries that matched the query.
	 */
	public Collection<RNSPath> expand(String pathExpression, FilterFactory filterType)
	{
		if (pathExpression == null)
			throw new IllegalArgumentException("Cannot lookup a path which is null.");

		if (filterType == null)
			filterType = new FilePatternFilterFactory();

		try {
			String[] pathElements = PathUtils.normalizePath(pwd(), pathExpression);

			Collection<RNSPath> ret = expand(getRoot(), pathElements, 0, filterType);
			if (ret == null) {
				ret = new ArrayList<RNSPath>(1);
				ret.add(lookup(pathExpression, RNSPathQueryFlags.DONT_CARE));
			}

			return ret;
		} catch (RNSException rne) {
			throw new RuntimeException("Unexpected RNS path expansion exception.", rne);
		}
	}

	/**
	 * List the contents of the current RNS directory that match the given filter.
	 * 
	 * @param filter
	 *            A filter that is used to select entries in the current RNS directory.
	 * 
	 * @return The set of all RNS entries in the current directory that matched the given pattern.
	 * 
	 * @throws RNSPathDoesNotExistException
	 * @throws RNSException
	 */
	public Collection<RNSPath> listContents(RNSFilter filter) throws RNSPathDoesNotExistException, RNSException
	{
		Collection<RNSPath> ret = new LinkedList<RNSPath>();

		for (RNSPath path : listContents()) {
			if (filter.matches(path))
				ret.add(path);
		}

		return ret;
	}

	/**
	 * List all of the entries in the given RNS directory.
	 * 
	 * This does not add the items to the cache, since the directories could potentially be huge,
	 * and this will just wash out entries we care about, like the root RNSPath.
	 * 
	 * @return The set of all RNSPath entries contained in this directory.
	 * 
	 * @throws RNSPathDoesNotExistException
	 * @throws RNSException
	 */
	public Collection<RNSPath> listContents(boolean shortForm) throws RNSPathDoesNotExistException, RNSException
	{
		EndpointReferenceType me = resolveRequired();

		// Note that calling context property for RNS-Short-Form in this case is set before creating
		// the proxy.
		// This is because when a call is coming from FUSE, we get a context resolver that cannot
		// propagate
		// property updates accurately across all the references of the calling context. For the
		// same reason
		// an explicit store is invoked after setting the property. As a general rule, doing context
		// update before
		// proxy creation is advisable to avoid similar unwanted problems.
		ICallingContext context = null;
		try {
			context = ContextManager.getCurrentContext();
			context.setSingleValueProperty("RNSShortForm", shortForm);
			ContextManager.storeCurrentContext(context);
			_logger.trace("RNS Short form set to true for listContents");
		} catch (Exception e) {
		}

		EnhancedRNSPortType rpt = createProxy(me, EnhancedRNSPortType.class);
		RNSLegacyProxy proxy = new RNSLegacyProxy(rpt, context);
		RNSIterable entries = null;

		try {
			entries = proxy.iterateList();
			LinkedList<RNSPath> ret = new LinkedList<RNSPath>();

			for (RNSEntryResponseType entry : entries) {
				RNSPath newEntry = new RNSPath(this, entry.getEntryName(), entry.getEndpoint(), !shortForm);
				ret.add(newEntry);
			}

			return ret;
		} catch (GenesisIISecurityException gse) {
			throw new RNSException("Unable to list contents -- " + "security exception.", gse);
		} catch (RemoteException re) {
			throw new RNSException("Unable to list contents.", re);
		}

		finally {
			try {
				StreamUtils.close(entries.getIterable());
			} catch (Exception e) {
				_logger.warn("exception during attempt to close stream", e);
			}
			// remove the calling context property for short form
			if (shortForm) {
				try {
					context = ContextManager.getCurrentContext();
					context.removeProperty("RNSShortForm");
					ContextManager.storeCurrentContext(context);
				} catch (Exception e) {
					_logger.error("Could not remove the short form request from the calling context", e);
				}
			}
		}
	}

	public Collection<RNSPath> listContents() throws RNSPathDoesNotExistException, RNSException
	{
		return listContents(false);
	}

	/**
	 * This method performs grouped/batch-mode operation on the RNS paths.
	 * 
	 * This does not add the items to the cache, since the directories could potentially be huge,
	 * and this will just wash out entries we care about, like the root RNSPath.
	 */
	public Collection<RNSPath> listContents(String... lookupPath) throws RNSPathDoesNotExistException, RNSException
	{
		EndpointReferenceType me = resolveRequired();
		EnhancedRNSPortType rpt = createProxy(me, EnhancedRNSPortType.class);
		RNSLegacyProxy proxy = new RNSLegacyProxy(rpt);
		RNSIterable entries = null;

		try {
			entries = proxy.iterateList(lookupPath);
			LinkedList<RNSPath> ret = new LinkedList<RNSPath>();
			for (RNSEntryResponseType entry : entries) {
				RNSPath newEntry = new RNSPath(this, entry.getEntryName(), entry.getEndpoint(), true);
				ret.add(newEntry);
			}
			return ret;
		} catch (GenesisIISecurityException gse) {
			throw new RNSException("Unable to list contents -- " + "security exception.", gse);
		} catch (RemoteException re) {
			throw new RNSException("Unable to list the contents.", re);
		} finally {
			if (entries != null) {
				StreamUtils.close(entries.getIterable());
			}
		}
	}

	/**
	 * A more economical way of traversing and acting on the contents of an RNSPath than the
	 * listContents methods. This applies the "applier" method to each object, which is just a way
	 * to pass each RNSPath in the contents a specialized function without needing to instantiate
	 * the whole list at once.
	 */
	public boolean applyToContents(RNSPathApplyFunction applier) throws RNSException
	{
		EndpointReferenceType me = resolveRequired();
		EnhancedRNSPortType rpt = createProxy(me, EnhancedRNSPortType.class);
		RNSLegacyProxy proxy = new RNSLegacyProxy(rpt);
		RNSIterable entries = null;
		SingleResourcePropertyTranslator translator = new DefaultSingleResourcePropertyTranslator();
		boolean getShortForm = applier.canWorkWithShortForm();

		try {
			if (getShortForm) {
				// setting the calling context property for short form
				try {
					ICallingContext context = ContextManager.getCurrentContext();
					context.setSingleValueProperty("RNSShortForm", true);
					ContextManager.storeCurrentContext(context);
					_logger.trace("Short RNS form requested from Grid Client.");
				} catch (Exception e) {
					_logger.trace("could not set the short form");
				}

			}

			entries = proxy.iterateList();
			for (RNSEntryResponseType entry : entries) {
				RNSPath newEntry = new RNSPath(this, entry.getEntryName(), entry.getEndpoint(), !getShortForm);
				newEntry.extractPortTypesAndURIFromMetadata(entry, translator);
				boolean funcRet = applier.applyToPath(newEntry);
				newEntry = null;
				entry = null;
				if (funcRet != true)
					return false;
			}
		} catch (GenesisIISecurityException gse) {
			throw new RNSException("Unable to list contents -- security exception.", gse);
		} catch (RemoteException re) {
			throw new RNSException("Unable to list contents.", re);
		} finally {
			try {
				StreamUtils.close(entries.getIterable());
			} catch (Exception e) {
				_logger.warn("exception during attempt to close stream", e);
			}

			if (getShortForm) {
				// remove the calling context property for short form
				ICallingContext context;
				try {
					context = ContextManager.getCurrentContext();
					context.removeProperty("RNSShortForm");
					ContextManager.storeCurrentContext(context);
				} catch (Exception e) {
					_logger.error("Could not remove the short form request from the calling context", e);
				}
			}
		}
		return true;
	}

	/*
	 * This method is used with RNS short entry responses to retrieve port-type and URI from
	 * meta-data instead of from EPR. This is important as port-type information is used to control
	 * recursive lookup and printing format in LS calls, and URI is needed to cache access.
	 */
	private void extractPortTypesAndURIFromMetadata(RNSEntryResponseType entry, SingleResourcePropertyTranslator translator)
	{
		RNSMetadataType metadataType = entry.getMetadata();

		if (metadataType != null && metadataType.get_any() != null) {
			for (MessageElement element : metadataType.get_any()) {
				QName qName = element.getQName();

				if (GenesisIIConstants.HUMAN_READABLE_PORT_TYPES_QNAME.equals(qName)) {
					try {
						_stringPortTypes = translator.deserialize(String.class, element);
					} catch (ResourcePropertyException e) {
						_logger.debug("Property translation error in string port-type: " + e.getMessage());
					}
				} else if (GenesisIIConstants.RESOURCE_URI_QNAME.equals(qName)) {
					try {
						_resourceURI = translator.deserialize(URI.class, element);
					} catch (ResourcePropertyException e) {
						_logger.debug("Property translation error in URI: " + e.getMessage());
					}
				}
			}
		}
	}

	// A method for bypassing access to EPR for port-type lookup whenever possible. This and the
	// subsequent method
	// is particularly useful for managing short RNS responses.
	public boolean isRNS()
	{
		if (_stringPortTypes != null) {
			return _stringPortTypes.contains("-RNS-");
		} else {
			resolveOptional();
			return new TypeInformation(_cachedEPR).isRNS();
		}
	}

	// A method for bypassing access to EPR for EPI retrieval whenever possible.
	public URI getWSIdentifier()
	{
		if (_resourceURI != null)
			return _resourceURI;
		else {
			resolveOptional();
			return new WSName(_cachedEPR).getEndpointIdentifier();
		}
	}

	/**
	 * Assuming that the indicated RNSPath entry does not yet exist (but that it's parent does),
	 * links the given EPR into this named entry.
	 * 
	 * @param epr
	 *            The EPR to link to this indicated name.
	 * 
	 * @throws RNSPathAlreadyExistsException
	 * @throws RNSPathDoesNotExistException
	 * @throws RNSException
	 */
	public void link(EndpointReferenceType epr) throws RNSPathAlreadyExistsException, RNSPathDoesNotExistException,
		RNSException
	{
		if (exists())
			throw new RNSPathAlreadyExistsException(pwd());

		if (_parent == null)
			throw new RNSException("Someone tried to link the root directory, " + "which can't be done.");

		EndpointReferenceType parentEPR = _parent.resolveRequired();

		RNSLegacyProxy proxy = new RNSLegacyProxy(createProxy(parentEPR, EnhancedRNSPortType.class));
		try {
			_cachedEPR = proxy.add(getName(), epr);
			_attemptedResolve = true;
			storeResourceConfigInCache();
		} catch (RemoteException re) {
			throw new RNSException("Unable to link in new EPR.", re);
		}
	}

	/**
	 * Unlink this RNSPath entry from the namespace (this will never destroy the target resource --
	 * it merely unlinks it from the filesystem).
	 * 
	 * @throws RNSPathDoesNotExistException
	 * @throws RNSException
	 */
	public void unlink() throws RNSPathDoesNotExistException, RNSException
	{
		if (!exists())
			throw new RNSPathDoesNotExistException(pwd());

		if (_parent == null)
			throw new RNSException("Attempt to unlink root not allowed.");

		EndpointReferenceType parentEPR = _parent.resolveRequired();

		EnhancedRNSPortType rpt = createProxy(parentEPR, EnhancedRNSPortType.class);
		removeEPRFromCache();
		RNSLegacyProxy proxy = new RNSLegacyProxy(rpt);
		try {
			proxy.remove(getName());
			_cachedEPR = null;
			_attemptedResolve = true;
		} catch (RemoteException re) {
			throw new RNSException("Unable to unlink entry.", re);
		}
	}

	/**
	 * Similar to unlink above, but this operation also destroy the target resource if at all
	 * possible.
	 * 
	 * @throws RNSPathDoesNotExistException
	 * @throws RNSException
	 */
	public void delete() throws RNSPathDoesNotExistException, RNSException
	{
		if (!exists())
			throw new RNSPathDoesNotExistException(pwd());

		if (_parent == null)
			throw new RNSException("Attempt to unlink root not allowed.");

		removeEPRFromCache();

		EndpointReferenceType parentEPR = _parent.resolveRequired();
		try {
			if (EPRUtils.isCommunicable(_cachedEPR)) {
				GeniiCommon common = createProxy(_cachedEPR, GeniiCommon.class);
				common.destroy(new Destroy());
			}

			EnhancedRNSPortType rpt = createProxy(parentEPR, EnhancedRNSPortType.class);
			RNSLegacyProxy proxy = new RNSLegacyProxy(rpt);
			proxy.remove(getName());
			_cachedEPR = null;
			_attemptedResolve = true;
		} catch (RemoteException re) {
			throw new RNSException("Unable to delete entry.", re);
		}
	}

	private void removeEPRFromCache()
	{
		CacheManager.removeItemFromCache(pwd(), EndpointReferenceType.class);
	}

	/*
	 * RNSPath is the class that links between the client-side code and the web services endPoints
	 * residing on the containers. So when we resolve an EPR for RNSPath or creating an RNSPath from
	 * some already cached EPR, we should store the rnsPath to endPointIdentifier mapping of the
	 * concerned EPR in a resource configuration instance. This configuration will subsequently
	 * bridge/govern cache related settings of any information related to the EPR. This method also
	 * puts the EPR in the cache in case it is already not there.
	 */
	private void storeResourceConfigInCache()
	{
		if (_cachedEPR == null)
			return;
		WSName wsName = new WSName(_cachedEPR);
		// In future, we have to support EPRs that do not have any valid endPointIdentifier
		// to make our caching strategy robust.
		if (wsName.isValidWSName()) {
			WSResourceConfig resourceConfig = new WSResourceConfig(wsName, pwd());
			CacheManager.putItemInCache(wsName.getEndpointIdentifier(), resourceConfig);
		}
		CacheManager.putItemInCache(pwd(), _cachedEPR);
	}

	/**
	 * Returns the pwd() for this RNSPath entry.
	 */
	public String toString()
	{
		return pwd();
	}

	private Object writeReplace() throws ObjectStreamException
	{
		return new RNSPathSerializedRepresentation(this);
	}

	static private class RNSPathSerializedRepresentation implements Serializable
	{
		static final long serialVersionUID = 2134908123740L;

		private String _path;
		private EndpointReferenceType _root;
		private EndpointReferenceType _cachedTarget;

		public RNSPathSerializedRepresentation(RNSPath path)
		{
			RNSPath root = path;

			while (root._parent != null)
				root = root._parent;

			_path = path.pwd();
			_root = root._cachedEPR;
			_cachedTarget = path._cachedEPR;
		}

		public Object readResolve() throws ObjectStreamException
		{
			String[] split = _path.split("/");

			RNSPath last = new RNSPath(_root);
			for (String element : split) {
				if (element == null || element.length() == 0)
					continue;

				last = new RNSPath(last, element, null, false);
			}

			last._cachedEPR = _cachedTarget;
			last._attemptedResolve = (_cachedTarget != null);

			return last;
		}

		private void writeObject(ObjectOutputStream out) throws IOException
		{
			out.writeObject(_path);
			EPRUtils.serializeEPR(out, _root);
			EPRUtils.serializeEPR(out, _cachedTarget);
		}

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
		{
			_path = (String) in.readObject();
			_root = EPRUtils.deserializeEPR(in);
			_cachedTarget = EPRUtils.deserializeEPR(in);
		}

		@SuppressWarnings("unused")
		private void readObjectNoData() throws ObjectStreamException
		{
			throw new StreamCorruptedException();
		}
	}
}
