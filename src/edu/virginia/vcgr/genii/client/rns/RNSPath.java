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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.Add;
import org.ggf.rns.CreateFile;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.RNSPortType;
import org.ggf.rns.Remove;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.iterator.WSIterable;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.filters.FilePatternFilterFactory;
import edu.virginia.vcgr.genii.client.rns.filters.Filter;
import edu.virginia.vcgr.genii.client.rns.filters.FilterFactory;
import edu.virginia.vcgr.genii.client.rns.filters.RNSFilter;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.enhancedrns.IterateListRequestType;

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
public class RNSPath implements Serializable
{
	static final long serialVersionUID = -5879165350773440573L;
	
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(RNSPath.class);
	
	static public RNSPath getCurrent()
		throws ConfigurationException
	{
		try
		{
			return ContextManager.getCurrentContext().getCurrentPath();
		}
		catch (IOException ioe)
		{
			throw new ConfigurationException("Unable to get current path.", 
				ioe);
		}
	}
	
	static private <Type> Type createProxy(
		EndpointReferenceType epr, Class<Type> cl)
	{
		try
		{
			return ClientUtils.createProxy(cl, epr);
		}
		catch (GenesisIISecurityException gse)
		{
			throw new SecurityException(
				"Unable to create Genesis II proxy.", gse);
		}
		catch (ConfigurationException ce)
		{
			throw new RuntimeException(
				"Genesis II appears to be misconfigured.  " +
				"Unable to create proxy.", ce);
		}
		catch (ResourceException re)
		{
			throw new RuntimeException(
				"Unknown exception occurred trying to create proxy.", re);
		}
	}
	
	private RNSPath _parent;
	private String _nameFromParent;
	private EndpointReferenceType _cachedEPR;
	private boolean _attemptedResolve;
	
	private EndpointReferenceType resolveRequired()
		throws RNSPathDoesNotExistException
	{
		EndpointReferenceType epr = resolveOptional();
		if (epr == null)
			throw new RNSPathDoesNotExistException(
				"Unable to resolve path \"" + pwd() + "\".");
		
		return epr;
	}
	
	private EndpointReferenceType resolveOptional()
	{
		try
		{
			if (_cachedEPR == null && !_attemptedResolve)
			{
				_attemptedResolve = true;
				EndpointReferenceType parent = _parent.resolveOptional();
				if (parent != null)
				{
					RNSPortType rpt = createProxy(parent, RNSPortType.class);
					ListResponse resp = rpt.list(new List(_nameFromParent));
					EntryType []entries = resp.getEntryList();
					if (entries != null && entries.length == 1)
					{
						EntryType entry = entries[0];
						_cachedEPR = entry.getEntry_reference();
					}
				}
			}
		}
		catch (RemoteException re)
		{
			_logger.debug("Error lookinup path component.", re);
		}
		
		return _cachedEPR;
	}
	
	public RNSPath(RNSPath parent, String nameFromParent, 
		EndpointReferenceType cachedEPR, boolean attemptedResolve)
	{
		_parent = parent;
		_nameFromParent = nameFromParent;
		_cachedEPR = cachedEPR;
		_attemptedResolve = attemptedResolve;
		
		if ((_parent == null && _nameFromParent != null) || 
			(_parent != null && _nameFromParent == null))
		{
			throw new IllegalArgumentException(
				"The parent and the nameFromParent parameters must either " +
				"both be null, or both be non-null.");
		}
		
		if (_parent == null && _cachedEPR == null)
			throw new IllegalArgumentException("Cannot have a null EPR for the root.");
		
		if (_cachedEPR != null)
			_attemptedResolve = true;
	}
	
	public RNSPath(EndpointReferenceType root)
	{
		this(null, null, root, true);
	}
	
	public RNSPath createSandbox()
		throws RNSPathDoesNotExistException
	{
		EndpointReferenceType newRoot = resolveRequired();
		TypeInformation typeInfo = new TypeInformation(newRoot);
		if (!typeInfo.isRNS())
			throw new RNSPathDoesNotExistException(
				"Path \"" + pwd() + 
				"\" does not indicate a directory.");
		
		return new RNSPath(newRoot);
	}
	
	public String getName()
	{
		if (_nameFromParent == null)
			return "/";
		
		return _nameFromParent;
	}
	
	public EndpointReferenceType getEndpoint()
		throws RNSPathDoesNotExistException
	{
		return resolveRequired();
	}
	
	public String pwd()
	{
		if (_parent == null)
			return "/";
		
		String parent = _parent.pwd();
		if (parent == "/")
			return parent + _nameFromParent;
		
		return parent + "/" + _nameFromParent;
	}
	
	public RNSPath getRoot()
	{
		if (_parent == null)
			return this;
		
		return _parent.getRoot();
	}
	
	public RNSPath getParent()
	{
		if (_parent == null)
			return this;
		
		return _parent;
	}
	
	public boolean isRoot()
	{
		return _parent == null;
	}
	
	public boolean exists()
	{
		return resolveOptional() != null;
	}
	
	public void mkdir()
		throws RNSException, RNSPathAlreadyExistsException, 
			RNSPathDoesNotExistException
	{
		if (exists())
			throw new RNSPathAlreadyExistsException(pwd());
		
		if (_parent == null)
			throw new RNSException(
				"Someone tried to create the root directory, " +
				"which can't be done.");
		
		EndpointReferenceType parentEndpoint = _parent.resolveRequired();
		
		RNSPortType rpt = createProxy(parentEndpoint, RNSPortType.class);
		try
		{
			_cachedEPR = rpt.add(new Add(
				_nameFromParent, null, null)).getEntry_reference();
			_attemptedResolve = true;
		}
		catch (RemoteException re)
		{
			throw new RNSException("Unable to add new directory.", re);
		}
	}
	
	public void mkdirs()
		throws RNSException, RNSPathAlreadyExistsException, 
			RNSPathDoesNotExistException
	{
		if (exists())
			throw new RNSPathAlreadyExistsException(pwd());
		
		if (_parent == null)
			throw new RNSException(
				"Someone tried to create the root directory, " +
				"which can't be done.");
		
		if (!_parent.exists())
			_parent.mkdirs();
		
		mkdir();
	}
	
	public EndpointReferenceType createNewFile()
		throws RNSPathAlreadyExistsException, RNSPathDoesNotExistException,
			RNSException
	{
		if (exists())
			throw new RNSPathAlreadyExistsException(pwd());
		
		if (_parent == null)
			throw new RNSException(
				"Someone tried to create a file as the root directory.");
		
		EndpointReferenceType parentEPR = _parent.resolveRequired();
		
		RNSPortType rpt = createProxy(parentEPR, RNSPortType.class);
		try
		{
			_cachedEPR = rpt.createFile(
				new CreateFile(_nameFromParent)).getEntry_reference();
			_attemptedResolve = true;
			
			return _cachedEPR;
		}
		catch (RemoteException re)
		{
			throw new RNSException("Unable to create new file.", re);
		}
	}
	
	private void arrayify(ArrayList<RNSPath> rep)
	{
		if (_parent != null)
			_parent.arrayify(rep);
		rep.add(this);
	}
	
	public RNSPath lookup(String path)
	{
		try
		{
			return lookup(path, RNSPathQueryFlags.DONT_CARE);
		}
		catch (RNSPathDoesNotExistException rpdnee)
		{
			_logger.error("This exception shouldn't have happened.", rpdnee);
		}
		catch (RNSPathAlreadyExistsException rpdnee)
		{
			_logger.error("This exception shouldn't have happened.", rpdnee);
		}
		
		return null;
	}
	
	public RNSPath lookup(String path, RNSPathQueryFlags queryFlag)
			throws RNSPathDoesNotExistException, RNSPathAlreadyExistsException
	{
		if (path == null)
			throw new IllegalArgumentException(
				"Cannot lookup a path which is null.");
		
		String[] pathElements = PathUtils.normalizePath(pwd(), path);
		ArrayList<RNSPath> arrayRep = new ArrayList<RNSPath>();
		arrayify(arrayRep);
		
		int lcv = 0;
		while (true)
		{
			if (lcv >= pathElements.length)
				break;
			if (lcv + 1 >= arrayRep.size())
				break;
			if (!pathElements[lcv].equals(arrayRep.get(lcv + 1)._nameFromParent))
				break;
			lcv++;
		}
		
		if (lcv >= pathElements.length)
		{
			// We completely matched a portion of the original path
			return arrayRep.get(lcv);
		}
		
		RNSPath next = arrayRep.get(lcv);
		for (;lcv < pathElements.length; lcv++)
		{
			next = new RNSPath(next, pathElements[lcv], null, false);
		}
		
		if (queryFlag.equals(RNSPathQueryFlags.MUST_EXIST))
		{
			if (!next.exists())
				throw new RNSPathDoesNotExistException(next.pwd());
		} else if (queryFlag.equals(RNSPathQueryFlags.MUST_NOT_EXIST))
		{
			if (next.exists())
				throw new RNSPathAlreadyExistsException(next.pwd());
		}
		
		return next;
	}
	
	private Collection<RNSPath> expand(
		RNSPath parent, String []pathElements, int nextElement,
		FilterFactory filterType)
	{
		Collection<RNSPath> ret = new LinkedList<RNSPath>();
		Collection<RNSPath> tmp;
			
		if (nextElement >= pathElements.length)
			ret.add(parent);
		else
		{
			Filter filter = filterType.createFilter(pathElements[nextElement]);
			try
			{
				TypeInformation typeInfo = new TypeInformation(
					parent.getEndpoint());
				if (typeInfo.isRNS())
				{
					_logger.debug("Attempting to list contents of \"" + parent + "\".");
					
					for (RNSPath candidate : parent.listContents())
					{
						if (filter.matches(candidate.getName()))
						{
							tmp = expand(candidate, pathElements, 
								nextElement + 1, filterType);
							if (tmp != null)
								ret.addAll(tmp);
						}
					}
				}
			}
			catch (RNSException rne)
			{
				_logger.debug(
					"Skipping a directory in an RSNPath expansion which " +
					"can't be expanded.", rne);
			}
		}
		
		if (ret.size() == 0)
			return null;
		
		return ret;
	}

	public Collection<RNSPath> expand(String pathExpression)
	{
		return expand(pathExpression, new FilePatternFilterFactory());
	}
	
	public Collection<RNSPath> expand(String pathExpression,
		FilterFactory filterType)
	{
		if (pathExpression == null)
			throw new IllegalArgumentException(
				"Cannot lookup a path which is null.");
		
		try
		{
			String[] pathElements = PathUtils.normalizePath(pwd(), 
				pathExpression);
			
			Collection<RNSPath> ret = expand(getRoot(), pathElements, 
				0, filterType);
			if (ret == null)
			{
				ret = new ArrayList<RNSPath>(1);
				ret.add(lookup(pathExpression, 
					RNSPathQueryFlags.DONT_CARE));
			}
			
			return ret;
		}
		catch (RNSException rne)
		{
			throw new RuntimeException(
				"Unexpected RNS path expansion exception.", rne);
		}
	}
	
	public Collection<RNSPath> listContents(RNSFilter filter)
		throws RNSPathDoesNotExistException, RNSException
	{
		Collection<RNSPath> ret = new LinkedList<RNSPath>();
		
		for (RNSPath path : listContents())
		{
			if (filter.matches(path))
				ret.add(path);
		}
		
		return ret;
	}
	
	public Collection<RNSPath> listContents()
		throws RNSPathDoesNotExistException, RNSException
	{
		EndpointReferenceType me = resolveRequired();
		TypeInformation typeInfo = new TypeInformation(me);
		if (typeInfo.isEnhancedRNS())
		{
			// We have an iterator
			EnhancedRNSPortType rpt = createProxy(
				me, EnhancedRNSPortType.class);
			WSIterable<EntryType> entries = null;
			
			try
			{
				entries = new WSIterable<EntryType>(
					EntryType.class, rpt.iterateList(
						new IterateListRequestType()).getResult(),
						50, true);
				LinkedList<RNSPath> ret = new LinkedList<RNSPath>();
				for (EntryType entry : entries)
				{
					ret.add(new RNSPath(this, entry.getEntry_name(), 
						entry.getEntry_reference(), true));
				}
				
				return ret;
			}
			catch (GenesisIISecurityException gse)
			{
				throw new RNSException("Unable to list contents -- " +
					"security exception.", gse);
			}
			catch (ConfigurationException ce)
			{
				throw new RNSException("Unable to list contents.", ce);
			}
			catch (RemoteException re)
			{
				throw new RNSException("Unable to list contents.", re);
			}
			finally
			{
				StreamUtils.close(entries);
			}
				
		} else
		{
			// We don't have an iterator -- do it the old fashioned way.
			RNSPortType rpt = createProxy(me, RNSPortType.class);
			LinkedList<RNSPath> ret = new LinkedList<RNSPath>();
			
			try
			{
				for (EntryType entry : rpt.list(new List(null)).getEntryList())
				{
					ret.add(new RNSPath(this, entry.getEntry_name(),
						entry.getEntry_reference(), true));
				}
				
				return ret;
			}
			catch (RemoteException re)
			{
				throw new RNSException("Unable to list contents.", re);
			}
		}
	}
	
	public void link(EndpointReferenceType epr)
		throws RNSPathAlreadyExistsException, RNSPathDoesNotExistException,
			RNSException
	{
		if (exists())
			throw new RNSPathAlreadyExistsException(pwd());
		
		if (_parent == null)
			throw new RNSException(
				"Someone tried to link the root directory, " +
				"which can't be done.");
		
		EndpointReferenceType parentEPR = _parent.resolveRequired();
		
		RNSPortType rpt = createProxy(parentEPR, RNSPortType.class);
		try
		{
			_cachedEPR = rpt.add(new Add(
				_nameFromParent, epr, null)).getEntry_reference();
			_attemptedResolve = true;
		}
		catch (RemoteException re)
		{
			throw new RNSException("Unable to link in new EPR.", re);
		}
	}
	
	public void unlink()
		throws RNSPathDoesNotExistException, RNSException
	{
		if (!exists())
			throw new RNSPathDoesNotExistException(pwd());
		
		if (_parent == null)
			throw new RNSException("Attempt to unlink root not allowed.");
		
		EndpointReferenceType parentEPR = _parent.resolveRequired();
		
		RNSPortType rpt = createProxy(parentEPR, RNSPortType.class);
		try
		{
			rpt.remove(new Remove(_nameFromParent));
			_cachedEPR = null;
			_attemptedResolve = true;
		}
		catch (RemoteException re)
		{
			throw new RNSException("Unable to unlink entry.", re);
		}
	}
	
	public void delete()
		throws RNSPathDoesNotExistException, RNSException
	{
		if (!exists())
			throw new RNSPathDoesNotExistException(pwd());
		
		if (_parent == null)
			throw new RNSException("Attempt to unlink root not allowed.");
		
		EndpointReferenceType parentEPR = _parent.resolveRequired();
		try
		{
			if (EPRUtils.isCommunicable(_cachedEPR))
			{
				GeniiCommon common = createProxy(_cachedEPR, GeniiCommon.class);
				common.destroy(new Destroy());
			}
			
			RNSPortType rpt = createProxy(parentEPR, RNSPortType.class);
			rpt.remove(new Remove(_nameFromParent));
			_cachedEPR = null;
			_attemptedResolve = true;
		}
		catch (RemoteException re)
		{
			throw new RNSException("Unable to delete entry.", re);
		}
	}
	
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
			String []split = _path.split("/");
			
			RNSPath last = new RNSPath(_root);
			for (String element : split)
			{
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
		
		private void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException
		{
			_path = (String)in.readObject();
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