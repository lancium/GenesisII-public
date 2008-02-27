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
package edu.virginia.vcgr.genii.client.rns;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.Add;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSPortType;
import org.ggf.rns.Remove;
import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.common.GeniiCommon;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;

/**
 * The RNSPath class is the main client side interface between developers and
 * the grid directory structure. Nearly all directory related operations
 * (including creating files and destroying instances), should be handled
 * through instances of this class. You will also notice that there are very few
 * public constructors for this class. This is intentional. As a general rule of
 * thumb, you should always get your "current" path instance from the
 * edu.virginia.vcgr.htc.client.context.ContextManager class.
 * 
 * @author Mark Morgan
 */
public class RNSPath implements Externalizable  {
	static public final long serialVersionUID = 42L;

	static private Log _logger = LogFactory.getLog(RNSPath.class);

	static private EntryType[] lookupContents(EndpointReferenceType epr,
			String entryExpression) throws ConfigurationException,
			ResourceException, RNSEntryNotDirectoryFaultType,
			ResourceUnknownFaultType, RemoteException
	{
		EntryType[] ret = null;

		RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class, epr);
		ret = rpt.list(new List(entryExpression)).getEntryList();

		return ret;
	}

	static private LinkedList<LinkedList<PathElement>> lookupRemainder(
			EndpointReferenceType parentEPR, String[] newPath, int nextIndex)
			throws RemoteException, ConfigurationException 
	{
		EntryType[] entries = lookupContents(parentEPR, newPath[nextIndex]);
		
		if (entries == null || entries.length == 0)
			return null;

		LinkedList<LinkedList<PathElement>> ret = new LinkedList<LinkedList<PathElement>>();

		if (nextIndex + 1 >= newPath.length) {
			for (EntryType entry : entries) {
				LinkedList<PathElement> tmp = new LinkedList<PathElement>();
				tmp.addLast(new PathElement(entry.getEntry_name(), entry
						.getEntry_reference()));
				ret.addLast(tmp);
			}
			
			return ret;
		}

		// We haven't reached the end, so we have to keep searching.
		for (EntryType entry : entries) {
			LinkedList<LinkedList<PathElement>> nextPart = lookupRemainder(
					entry.getEntry_reference(), newPath, nextIndex + 1);
			if (nextPart == null || nextPart.size() == 0)
				continue;

			for (LinkedList<PathElement> tailPath : nextPart) {
				tailPath.addFirst(new PathElement(entry.getEntry_name(), entry
						.getEntry_reference()));
				ret.addLast(tailPath);
			}
		}
		
		if (ret != null && ret.size() == 0)
			return null;

		return ret;
	}

	private String _cachedPWD = null;

	private LinkedList<PathElement> _path;

	private RNSPath(LinkedList<PathElement> path) {
		_path = path;
	}

	private RNSPath(PathElement root) {
		this(new LinkedList<PathElement>());
		_path.add(root);
	}

	/**
	 * Retrieve the current RNS path for this environment.
	 * 
	 * @return The RNSPath which represents the current working grid path of
	 *         this environment.
	 * 
	 * @throws ConfigurationException
	 *             If the current path can't be determined.
	 */
	static public RNSPath getCurrent() throws ConfigurationException {
		try {
			return ContextManager.getCurrentContext().getCurrentPath();
		} catch (IOException ioe) {
			_logger.error(ioe.getLocalizedMessage(), ioe);
			throw new ConfigurationException(ioe.getLocalizedMessage(), ioe);
		}
	}

	// zero-arg constructor for serialization
	public RNSPath() {
		_path = new LinkedList<PathElement>(); 		
	}
	
	RNSPath(EndpointReferenceType root) {
		this(new PathElement(null, root));
	}

	/**
	 * Create a new RNSPath which sandboxes the current path as a new root. The
	 * sandboxing is minimal here and won't prevent users from leaving a sandbox
	 * via. links which leave that path.
	 * 
	 * @return A new RNSPath which will view the current RNSPath as a root
	 *         (sandbox).
	 * @throws RNSException
	 */
	public RNSPath createSandbox() throws RNSException {
		return new RNSPath(getEndpoint());
	}

	/**
	 * Retrieve the name of the current path endpoint.
	 * 
	 * @return The name (not the full path) of the current path endpoint.
	 */
	public String getName() {
		if (_path.size() == 1)
			return "/";
		else
			return _path.getLast().getName();
	}

	/**
	 * Retrieve the current path endpoint (if it exists).
	 * 
	 * @return The current path's endpoint if it exists.
	 * @throws RNSPathDoesNotExistException
	 */
	public EndpointReferenceType getEndpoint()
			throws RNSPathDoesNotExistException {
		if (!exists())
			throw new RNSPathDoesNotExistException(pwd());

		return _path.getLast().getEndpoint();
	}

	/**
	 * Retrieve the current, absolute, RNSPath.
	 * 
	 * @return The current, absolute, RNSPath.
	 */
	public String pwd() {
		if (_cachedPWD == null) {
			if (_path.size() == 1)
				_cachedPWD = "/";
			else {
				StringBuilder builder = new StringBuilder();
				for (PathElement element : _path) {
					String name = element.getName();
					if (name != null)
						builder.append("/" + name);
				}

				_cachedPWD = builder.toString();
			}
		}

		return _cachedPWD;
	}

	/**
	 * Retrieve the RNSPath which represents the root of this current context
	 * space.
	 * 
	 * @return The current root of this context space.
	 */
	public RNSPath getRoot() {
		return new RNSPath(_path.getFirst());
	}

	/**
	 * Retrieve the parent RNSPath of this entry.
	 * 
	 * @return The parent context of this entry.
	 */
	public RNSPath getParent() {
		int size = _path.size();
		if (size == 1)
			return new RNSPath(new LinkedList<PathElement>(_path));

		return new RNSPath(new LinkedList<PathElement>(_path.subList(0,
				size - 1)));
	}

	/**
	 * Discover whether or not this current path indicates the root of the
	 * space.
	 * 
	 * @return True if the current RNSPath represents the root of this space,
	 *         false otherwise.
	 */
	public boolean isRoot() {
		return _path.size() == 1;
	}

	/**
	 * Indicates whether or not the current RNSPath indicates an entry which
	 * exists (in the path sense, not necesarily the grid sense) or not.
	 * 
	 * @return True if the current path indicates an entry which exists, false
	 *         otherwise.
	 */
	public boolean exists() {
		return _path.getLast().getEndpoint() != null;
	}

	/**
	 * Indicates whether the current entry is a file or not.
	 * 
	 * @return True if the current indicated entry is a file, false otherwise.
	 */
	public boolean isFile() {
		PathElement last = _path.getLast();
		EndpointReferenceType epr = last.getEndpoint();
		if (epr == null)
			return false;
		TypeInformation ti = new TypeInformation(epr);
		return ti.isByteIO();
	}
	
	/**
	 * Indicates whether the current entry is an idp or not.
	 * 
	 * @return True if the current indicated entry is an idp, false otherwise.
	 */
	public boolean isIDP() {
		PathElement last = _path.getLast();
		EndpointReferenceType epr = last.getEndpoint();
		if (epr == null)
			return false;
		TypeInformation ti = new TypeInformation(epr);
		return ti.isIDP();
	}	

	/**
	 * Indicates whether the current entry is an RNS Directory or not.
	 * 
	 * @return True if the current indicated entry is an RNS Directory, false
	 *         otherwise.
	 */
	public boolean isDirectory() {
		PathElement last = _path.getLast();
		EndpointReferenceType epr = last.getEndpoint();
		if (epr == null)
			return false;
		TypeInformation ti = new TypeInformation(epr);
		return ti.isRNS();
	}

	/**
	 * Create the current entry as a new RNS directory. For this operation to
	 * complete successfully, the parent element must exist and must be an
	 * RNSDirectory itself.
	 * 
	 * @throws RNSPathAlreadyExistsException
	 *             if the indicated entry already exists in this RNSSpace
	 * @throws RNSPathDoesNotExistException
	 *             if the parent element does not exist.
	 * @throws RNSException
	 *             If there is a random RNS exception.
	 */
	public void mkdir() throws RNSPathAlreadyExistsException,
			RNSPathDoesNotExistException, RNSException {
		if (exists())
			throw new RNSPathAlreadyExistsException(pwd());

		RNSPath parent = getParent();
		if (!parent.exists())
			throw new RNSPathDoesNotExistException(parent.pwd());

		if (!parent.isDirectory())
			throw new RNSException("Entry \"" + parent.pwd()
					+ "\" is not an RNS directory.");

		try {
			RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class,
					parent._path.getLast().getEndpoint());
			_path.getLast().setEndpoint(
					rpt.add(new Add(getName(), null, null))
							.getEntry_reference());
		} catch (BaseFaultType t) {
			throw new RNSException(t);
		} catch (Throwable t) {
			throw new RNSException(t);
		}
	}

	/**
	 * Similar to the mkdir command, this operation also creates the current
	 * path entry as a directory. However, this operation will also create all
	 * parent elements which don't exist as directories as well.
	 * 
	 * @throws RNSPathAlreadyExistsException
	 *             if the indicated entry already exists in this RNSSpace
	 * @throws RNSException
	 *             If there is a random RNS exception.
	 */
	public void mkdirs() throws RNSPathAlreadyExistsException, RNSException {
		if (exists())
			throw new RNSPathAlreadyExistsException(pwd());

		RNSPath parent = getParent();
		if (!parent.exists())
			parent.mkdirs();

		if (!parent.isDirectory())
			throw new RNSException("Entry \"" + parent.pwd()
					+ "\" is not an RNS directory.");

		try {
			RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class,
					parent._path.getLast().getEndpoint());
			_path.getLast().setEndpoint(
					rpt.add(new Add(getName(), null, null))
							.getEntry_reference());
		} catch (BaseFaultType t) {
			throw new RNSException(t);
		} catch (Throwable t) {
			throw new RNSException(t);
		}
	}

	/**
	 * Create the current entry as a new RandomByteIO file using the current web
	 * server as the hosting environment for the new file.
	 * 
	 * @throws RNSPathAlreadyExistsException
	 *             if the indicated entry already exists in this RNSSpace
	 * @throws RNSPathDoesNotExistException
	 *             if the parent element does not exist.
	 * @throws RNSException
	 *             If there is a random RNS exception.
	 */
	public void createFile() throws RNSPathAlreadyExistsException,
			RNSPathDoesNotExistException, RNSException {
		if (exists())
			throw new RNSPathAlreadyExistsException(pwd());

		RNSPath parent = getParent();
		if (!parent.exists())
			throw new RNSPathDoesNotExistException(parent.pwd());

		if (!parent.isDirectory())
			throw new RNSException("Entry \"" + parent.pwd()
					+ "\" is not an RNS directory.");

		try {
			RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class,
					parent._path.getLast().getEndpoint());
			CreateFile cf = new CreateFile(getName());
			CreateFileResponse resp = rpt.createFile(cf);
			_path.getLast().setEndpoint(resp.getEntry_reference());
		} catch (BaseFaultType t) {
			throw new RNSException(t);
		} catch (Throwable t) {
			throw new RNSException(t);
		}
	}

	/**
	 * Looks up a single element which matches the pathExpression given
	 * (relative to the curren path indicated in this instance.
	 * 
	 * @param pathExpression
	 *            The string path expression (regular expression) which is being
	 *            resolved.
	 * @param queryFlag
	 *            A flag indicating whether or not the caller requires that the
	 *            resultant entry exists or does not exist.
	 * @return The resultant RNSPath.
	 * 
	 * @throws RNSPathDoesNotExistException
	 *             If the resultant path does not exist and the caller indicated
	 *             that it must exist.
	 * @throws RNSPathAlreadyExistsException
	 *             If the resultant path already exists and the caller indicated
	 *             that it couldn't exist.
	 * @throws RNSMultiLookupResultException
	 *             If the results of looking up the given path expression
	 *             resulted in more then one entry being returned.
	 * @throws RNSException
	 *             If some other RNS exception occurs.
	 */
	public RNSPath lookup(String pathExpression, RNSPathQueryFlags queryFlag)
			throws RNSPathDoesNotExistException, RNSPathAlreadyExistsException,
			RNSMultiLookupResultException, RNSException {
		RNSPath[] ret = list(pathExpression, queryFlag);
		if (ret.length != 1)
			throw new RNSMultiLookupResultException(pathExpression);

		return ret[0];
	}

	/**
	 * This operation is identical to the lookup operation above accept that it
	 * permits more then 1 result for a given path expression (i.e., it allows
	 * for the path expression to be an expression matching more then 1 path).
	 * 
	 * @param pathExpression
	 *            The string path expression (regular expression) which is being
	 *            resolved.
	 * @param queryFlag
	 *            A flag indicating whether or not the caller requires that the
	 *            resultant entry exists or does not exist.
	 * @return The resultant RNSPaths.
	 * 
	 * @throws RNSPathDoesNotExistException
	 *             If the resultant path does not exist and the caller indicated
	 *             that it must exist.
	 * @throws RNSPathAlreadyExistsException
	 *             If the resultant path already exists and the caller indicated
	 *             that it couldn't exist.
	 * @throws RNSException
	 *             If some other RNS exception occurs.
	 */
	public RNSPath[] list(String pathExpression, RNSPathQueryFlags queryFlag)
			throws RNSPathDoesNotExistException, RNSPathAlreadyExistsException,
			RNSException {
		RNSPath[] ret = internalList(pathExpression);

		if (queryFlag == RNSPathQueryFlags.MUST_EXIST) {
			if (ret.length == 0 || (ret.length == 1 && !ret[0].exists()))
				throw new RNSPathDoesNotExistException(pathExpression);
		} else if (queryFlag == RNSPathQueryFlags.MUST_NOT_EXIST) {
			if (ret.length != 1 || ret[0].exists())
				throw new RNSPathAlreadyExistsException(pathExpression);
		}

		return ret;
	}

	private RNSPath[] internalList(String pathExpression) throws RNSException 
	{
		String[] newPath = PathUtils.normalizePath(pwd(), pathExpression);
		int newPathIndex = -1;
		LinkedList<PathElement> newPathList = new LinkedList<PathElement>();

		for (PathElement pe : _path) {
			if (newPathIndex >= newPath.length)
				return new RNSPath[] { new RNSPath(newPathList) };

			if (newPathIndex < 0)
				newPathList.addLast(pe);
			else {
				if (newPath[newPathIndex].equals(pe.getName()))
					newPathList.addLast(pe);
				else
					break;
			}

			newPathIndex++;
		}
		
		if (newPathIndex >= newPath.length)
			return new RNSPath[] { new RNSPath(newPathList) };

		EndpointReferenceType epr = newPathList.getLast().getEndpoint();
		if (epr != null) {
			LinkedList<LinkedList<PathElement>> subPaths;

			try {
				subPaths = lookupRemainder(epr, newPath, newPathIndex);
			} catch (BaseFaultType bft) {
				throw new RNSException(bft);
			} catch (Throwable t) {
				throw new RNSException(t);
			}

			if (subPaths != null && subPaths.size() != 0) {
				RNSPath[] ret = new RNSPath[subPaths.size()];
				int whichList = 0;
				for (LinkedList<PathElement> list : subPaths) {
					LinkedList<PathElement> tmp = new LinkedList<PathElement>(
							newPathList);

					for (PathElement pe : list) {
						tmp.addLast(pe);
					}

					ret[whichList++] = new RNSPath(tmp);
				}

				return ret;
			}
		}

		for (; newPathIndex < newPath.length; newPathIndex++) {
			EndpointReferenceType parent = newPathList.getLast().getEndpoint();
			if (parent == null)
				newPathList
						.addLast(new PathElement(newPath[newPathIndex], null));
			else {
				try {
					EntryType[] entries = lookupContents(parent,
							newPath[newPathIndex]);
					if (entries == null || entries.length != 1)
						newPathList.addLast(new PathElement(
								newPath[newPathIndex], null));
					else
						newPathList.addLast(new PathElement(entries[0]
								.getEntry_name(), entries[0]
								.getEntry_reference()));
				} catch (BaseFaultType bft) {
					throw new RNSException(bft);
				} catch (Throwable t) {
					throw new RNSException(t.getMessage(), t);
				}
			}
		}

		return new RNSPath[] { new RNSPath(newPathList) };
	}

	/**
	 * Link a given epr into the current RNSPath entry
	 * 
	 * @param epr
	 *            The epr to link to this path.
	 * 
	 * @throws RNSPathAlreadyExistsException
	 *             If the given path already exists.
	 * @throws RNSPathDoesNotExistException
	 *             If the parent path of this one does not exist.
	 * @throws RNSException
	 *             If some other RNS exception occurs.
	 */
	public void link(EndpointReferenceType epr)
			throws RNSPathAlreadyExistsException, RNSException {
		if (exists())
			throw new RNSPathAlreadyExistsException(pwd());

		RNSPath parent = getParent();
		if (!parent.exists())
			throw new RNSPathDoesNotExistException(parent.pwd());

		if (!parent.isDirectory())
			throw new RNSException("Path \"" + parent.pwd()
					+ "\" is not a directory.");

		try {
			RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class, parent
					.getEndpoint());
			rpt.add(new Add(getName(), epr, null));
			_path.getLast().setEndpoint(epr);
		} catch (BaseFaultType bft) {
			throw new RNSException(bft);
		} catch (Throwable t) {
			throw new RNSException(t);
		}
	}

	/**
	 * Unlink the current path from the RNSSpace. This operation does NOTHING
	 * about destroying the object...it merely unlinks it.
	 * 
	 * @throws RNSPathDoesNotExistException
	 *             If the current entry does not exist.
	 * @throws RNSException
	 *             If some other RNS exception occurs.
	 */
	public void unlink() throws RNSPathDoesNotExistException, RNSException {
		if (!exists())
			throw new RNSPathDoesNotExistException(pwd());

		RNSPath parent = getParent();
		try {
			RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class, parent
					.getEndpoint());
			rpt.remove(new Remove(getName()));
		} catch (BaseFaultType bft) {
			throw new RNSException(bft);
		} catch (Throwable t) {
			throw new RNSException(t);
		}
	}

	/**
	 * Similar to unlink above, but this operation also destroys the target
	 * object.
	 * 
	 * @throws RNSPathDoesNotExistException
	 *             If the current entry does not exist.
	 * @throws RNSException
	 *             If some other RNS exception occurs.
	 */
	public void delete() throws RNSPathDoesNotExistException, RNSException 
	{
		EndpointReferenceType epr = getEndpoint();
		
		try 
		{
			TypeInformation ti = new TypeInformation(epr);
			if (ti.isRNS())
			{
				RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class, epr);

				if (rpt.list(new List(".*")).getEntryList().length > 0)
					throw new RNSException("Path \"" + pwd() + "\" is not empty.");
			}

			if (EPRUtils.isCommunicable(epr))
			{
				GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, epr);
				common.destroy(new Destroy());
			}
			unlink();
		} catch (BaseFaultType bft) {
			throw new RNSException(bft);
		} catch (Throwable t) {
			throw new RNSException(t);
		}
	}

	/**
	 * Similar to unlink above, but this operation also destroys the target
	 * object.
	 * 
	 * @throws RNSPathDoesNotExistException
	 *             If the current entry does not exist.
	 * @throws RNSException
	 *             If some other RNS exception occurs.
	 */
	public void recursiveDelete() throws RNSPathDoesNotExistException,
			RNSException {
		createIterator(true, false, true).iterate(new RNSDeleteHandler());
	}

	public IRNSIterator createIterator(boolean depthFirst,
			boolean ignoreExceptions, boolean requireWSNames) {
		return (depthFirst ? new DepthFirstRNSIterator(this, ignoreExceptions,
				requireWSNames) : new BreadthFirstRNSIterator(this,
				ignoreExceptions, requireWSNames));
	}
	
    public void writeExternal(ObjectOutput out) throws IOException {
    	out.writeObject(_cachedPWD);
    	out.writeInt(_path.size());
    	for (PathElement pe : _path) {
    		out.writeObject(pe);
    	}
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	_cachedPWD = (String) in.readObject();
    	int numElements = in.readInt();
    	for (int i = 0; i < numElements; i++) {
    		_path.add((PathElement) in.readObject());
    	}
    }	
	
    public String toString()
    {
    	return pwd();
    }
}
