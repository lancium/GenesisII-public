package edu.virginia.vcgr.genii.client.iterator;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.axis.types.UnsignedInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.iterator.IterateRequestType;
import edu.virginia.vcgr.genii.iterator.IteratorMemberType;
import edu.virginia.vcgr.genii.iterator.IteratorPortType;

class WSIterator<Type> implements Iterator<Type>, Closeable
{
	static private Log _logger = LogFactory.getLog(WSIterator.class);
	
	private WSIteratorTarget _target;
	private Class<Type> _class;
	private long _nextIndex;
	private int _batchSize;
	private Iterator<IteratorMemberType> _internalIter;
	
	private void fill()
		throws RemoteException
	{
		Collection<IteratorMemberType> tmp;
		_internalIter = null;
		
		if (_nextIndex < 0)
			return;
		
		_logger.debug("Making out call to fill iterator buffer.");
		IteratorMemberType []results = _target.getTarget().iterate(
			new IterateRequestType(new UnsignedInt(_nextIndex),
			new UnsignedInt(_batchSize)));
		
		if (results != null && results.length > 0)
		{
			tmp = new ArrayList<IteratorMemberType>(results.length);
			for (IteratorMemberType member : results)
			{
				tmp.add(member);
			}
			
			_internalIter = tmp.iterator();
		}
		
		_nextIndex = (results == null || results.length < _batchSize) ?
			-1 : _nextIndex + _batchSize;
		if (_nextIndex < 0)
		{
			synchronized(this)
			{
				if (_target != null)
				{
					_target.releaseReference();
					_target = null;
				}
			}
		}
	}
	
	WSIterator(Class<Type> cl, WSIteratorTarget target, int batchSize)
		throws RemoteException
	{
		boolean success = false;
		_target = target;
		_nextIndex = 0;
		_batchSize = batchSize;
		
		_class = cl;
		
		_target.addReference();
		
		try
		{
			fill();
			success = true;
		}
		finally
		{
			if (!success)
			{
				try
				{
					close();
				}
				catch (Throwable cause)
				{
				}
			}
		}
	}
	
	public WSIterator(Class<Type> cl, IteratorPortType target, int batchSize, 
		boolean mustDestroy) throws GenesisIISecurityException, ResourceException, 
			ConfigurationException, RemoteException
	{
		this(cl, new WSIteratorTarget(target, mustDestroy), batchSize);
	}
	
	public WSIterator(Class<Type> cl, EndpointReferenceType target, int batchSize, 
		boolean mustDestroy) throws GenesisIISecurityException, ResourceException, 
			ConfigurationException, RemoteException
	{
		this(cl, ClientUtils.createProxy(
			IteratorPortType.class, target), batchSize, mustDestroy);
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}
	
	synchronized public void close() throws IOException
	{
		if (_target != null)
		{
			_target.releaseReference();
			_target = null;
		}
	}
	
	@Override
	public boolean hasNext()
	{
		return _internalIter != null;
	}

	@Override
	public Type next()
	{
		Type ret;
		
		if (_internalIter == null)
			throw new NoSuchElementException(
				"No more elements left in iterator.");
				
		IteratorMemberType imt = _internalIter.next();
		
		try
		{
			ret = ObjectDeserializer.toObject(imt.get_any()[0], _class);
		
			if (!_internalIter.hasNext())
				fill();
			
			return ret;
		}
		catch (RemoteException re)
		{
			throw new RuntimeException(
				"Exception trying to iterate over WSIterator.", re);
		}
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException(
			"Remove not supported in WSIterators.");
	}
}