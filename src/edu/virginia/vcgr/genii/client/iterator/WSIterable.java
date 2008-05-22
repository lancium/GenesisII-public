package edu.virginia.vcgr.genii.client.iterator;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.iterator.IteratorInitializationType;
import edu.virginia.vcgr.genii.iterator.IteratorPortType;

public class WSIterable<Type> implements Iterable<Type>, Closeable
{
	private Class<Type> _class;
	private WSIteratorTarget _target;
	private int _batchSize;
	
	public WSIterable(Class<Type> cl,
		EndpointReferenceType target,
		int batchSize, boolean mustDestroy)
		throws GenesisIISecurityException,
			RemoteException
	{
		this(cl, new IteratorInitializationType(target, null),
			batchSize, mustDestroy);
	}
	
	public WSIterable(Class<Type> cl,
		IteratorInitializationType iterator,
		int batchSize, boolean mustDestroy)
		throws GenesisIISecurityException,
			RemoteException
	{
		_class = cl;
		_batchSize = batchSize;
		_target = new WSIteratorTarget(iterator, mustDestroy);
		_target.addReference();
	}
	
	public WSIterable(Class<Type> cl, IteratorPortType iterator,
		int batchSize, boolean mustDestroy)
	{
		_class = cl;
		_batchSize = batchSize;
		_target = new WSIteratorTarget(iterator, mustDestroy);
		_target.addReference();
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}
	
	public Iterator<Type> iterator(int batchSize)
	{
		try
		{
			return new WSIterator<Type>(_class, _target, batchSize);
		}
		catch (RemoteException re)
		{
			throw new RuntimeException("Unable to create iterator.", re);
		}
	}
	
	@Override
	public Iterator<Type> iterator()
	{
		return iterator(_batchSize);
	}
	
	synchronized public void close() throws IOException
	{
		if (_target != null)
		{
			_target.releaseReference();
			_target = null;
		}
	}
}