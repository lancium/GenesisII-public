package edu.virginia.vcgr.genii.client.iterator;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.UnsignedLong;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rl_2.ResourceNotDestroyedFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.iterator.IterableElementType;
import edu.virginia.vcgr.genii.iterator.IterateResponseType;
import edu.virginia.vcgr.genii.iterator.IteratorInitializationType;
import edu.virginia.vcgr.genii.iterator.WSIteratorPortType;

final public class WSIterable<Type> implements Iterable<Type>, Closeable
{
	static private void failedConstructionCleanup(ICallingContext callContext,
		IteratorInitializationType initializer) 
			throws ResourceUnknownFaultType, ResourceNotDestroyedFaultType, 
				ResourceUnavailableFaultType, RemoteException
	{
		if (initializer != null)
		{
			EndpointReferenceType epr = initializer.getIteratorEndpoint();
			if (epr != null)
			{
				GeniiCommon common;
				
				if (callContext == null)
					common = ClientUtils.createProxy(GeniiCommon.class, epr);
				else
					common = ClientUtils.createProxy(GeniiCommon.class, epr,
						callContext);
				
				common.destroy(new Destroy());
			}
		}
	}
	
	private Class<Type> _elementType;
	private Unmarshaller _jaxbUnmarshaller;
	
	private WSIteratorResource _iterator;
	
	private Object []_initialBlock = null;
	private int _blockSize;
	
	private Type extractType(MessageElement []any)
	{
		if (any == null || any.length != 1)
			throw new IllegalArgumentException(
				"Iterable element format not correct " +
				"(must have exactly 1 element in xsd:any)!");
		
		try
		{
			if (_jaxbUnmarshaller != null)
				return _jaxbUnmarshaller.unmarshal(
					any[0], _elementType).getValue();
			else
				return ObjectDeserializer.toObject(any[0], _elementType);
		}
		catch (ResourceException re)
		{
			throw new IllegalArgumentException(
				"Error deserializing iterable element!", re);
		}
		catch (JAXBException e)
		{
			throw new IllegalArgumentException(
				"Error deserializing iterable element!", e);
		}
	}
	
	private WSIterable(Class<Type> elementType, 
		Unmarshaller jaxbUnmarshaller, Type []initialElementOverride, 
		IteratorInitializationType initializer, ICallingContext callingContext,
		int blockSize)
			throws ResourceException, GenesisIISecurityException 
	{
		_elementType = elementType;
		_jaxbUnmarshaller = jaxbUnmarshaller;
		WSIteratorPortType iterator;
		
		EndpointReferenceType iteratorEndpoint = initializer.getIteratorEndpoint();
		if (iteratorEndpoint == null)
			iterator = null;
		else
		{
			if (callingContext == null)
				iterator = ClientUtils.createProxy(WSIteratorPortType.class,
					iteratorEndpoint);
			else
				iterator = ClientUtils.createProxy(WSIteratorPortType.class, 
					iteratorEndpoint, callingContext);
		}
		_iterator = new WSIteratorResource(iteratorEndpoint, iterator);
		
		if (initialElementOverride != null)
		{
			_initialBlock = new Object[initialElementOverride.length];
			for (int lcv = 0; lcv < initialElementOverride.length; lcv++)
				_initialBlock[lcv] = initialElementOverride[lcv];
		} else
		{
			IterableElementType []initialElements = initializer.getBatchElement();
			if (initialElements != null && initialElements.length > 0)
			{
				_initialBlock = new Object[initialElements.length];
				for (IterableElementType element : initialElements)
				{
					int index = element.getIndex().intValue();
					_initialBlock[index] = extractType(element.get_any());
				}
			}
		}
		
		_blockSize = (blockSize <= 0) ? ((_initialBlock != null) ? _initialBlock.length : 100) : blockSize;
	}
	
	@Override
	final protected void finalize() throws Throwable
	{
		close();
	}
	
	@Override
	synchronized final public void close() throws IOException
	{
		if (_iterator != null)
			_iterator.release();
		
		_iterator = null;
	}

	@Override
	synchronized final public WSIterator<Type> iterator()
	{
		if (_iterator == null)
			throw new IllegalStateException(
				"WS-Iterator already closed!");
		
		return new InternalWSIterator();
	}
	
	final private class InternalWSIterator implements WSIterator<Type>
	{
		private int _currentStart;
		private int _next;
		private Object []_currentBlock;
		private int _lastIteratorSize = -1;
		
		private void prefetch(int nextStart)
		{
			if (_lastIteratorSize >= 0 && nextStart >= _lastIteratorSize)
			{
				StreamUtils.close(this);
				return;
			}
			
			try
			{
				if (_initialBlock != null && nextStart <= (_initialBlock.length - 1))
				{
					_currentBlock = new Object[_initialBlock.length - nextStart];
					System.arraycopy(_initialBlock, nextStart, 
						_currentBlock, 0, _currentBlock.length);
				} else
				{
					IterateResponseType resp = _iterator.iterate(nextStart, _blockSize);
					if (resp == null)
					{
						StreamUtils.close(this);
						return;
					}
					UnsignedLong ul = resp.getIteratorSize();
					if (ul != null)
						_lastIteratorSize = ul.intValue();
					IterableElementType []elements = resp.getIterableElement();
					if (elements == null || elements.length == 0)
					{
						StreamUtils.close(this);
						return;
					} else
					{
						_currentBlock = new Object[elements.length];
						for (int lcv = 0; lcv < _currentBlock.length; lcv++)
						{
							int index = elements[lcv].getIndex(
								).intValue() - nextStart;
							
							_currentBlock[index] = extractType(elements[lcv].get_any());
						}
					}
				}
				
				_next = 0;
				_currentStart = nextStart;
			}
			catch (RemoteException re)
			{
				throw new RuntimeException(
					"Unable to use iterator!", re);
			}
		}
		
		private InternalWSIterator()
		{
			_currentBlock = _initialBlock;
			_iterator.retain();
			prefetch(0);
		}
		
		@Override
		final protected void finalize() throws Throwable
		{
			close();
		}
		
		@Override
		final public boolean hasNext()
		{
			return _next >= 0;
		}

		@Override
		final public Type next()
		{
			Type ret = _elementType.cast(_currentBlock[_next++]);
			
			if (_next >= _currentBlock.length)
				prefetch(_currentStart + _next);
			
			return ret;
		}

		@Override
		final public void remove()
		{
			throw new UnsupportedOperationException(
				"Remove not supported for WS-Iterators!");
		}

		@Override
		final synchronized public void close() throws IOException
		{
			if (_next >= 0)
			{
				_next = -1;
				_iterator.release();
			}
		}
	}
	
	static public <Type> WSIterable<Type> jaxbIterable(Class<Type> elementType, 
		IteratorInitializationType iteratorInitializer, int blockSize) 
			throws ResourceUnknownFaultType,
				ResourceNotDestroyedFaultType, ResourceUnavailableFaultType, 
				RemoteException, JAXBException
	{
		return jaxbIterable(elementType, iteratorInitializer, 
			(ICallingContext)null, blockSize);
	}
	
	static public <Type> WSIterable<Type> jaxbIterable(Class<Type> elementType, 
		IteratorInitializationType iteratorInitializer, 
		ICallingContext callContext, int blockSize) 
			throws JAXBException, ResourceUnknownFaultType,
				ResourceNotDestroyedFaultType, ResourceUnavailableFaultType, 
				RemoteException
	{
		try
		{
			WSIterable<Type> ret = jaxbIterable(elementType,
				iteratorInitializer, JAXBContext.newInstance(elementType), 
				callContext, blockSize);
			iteratorInitializer = null;
			return ret;
		}
		finally
		{
			failedConstructionCleanup(callContext, iteratorInitializer);
		}
	}
		
	static public <Type> WSIterable<Type> jaxbIterable(Class<Type> elementType, 
		IteratorInitializationType iteratorInitializer, JAXBContext context,
		int blockSize) 
			throws ResourceException, GenesisIISecurityException, JAXBException
	{
		return jaxbIterable(elementType, iteratorInitializer, context, null, 
			blockSize);
	}
	
	static public <Type> WSIterable<Type> jaxbIterable(Class<Type> elementType, 
		IteratorInitializationType iteratorInitializer, JAXBContext context,
		ICallingContext callContext, int blockSize) 
			throws JAXBException, ResourceException, GenesisIISecurityException
	{
		return jaxbIterable(elementType, iteratorInitializer, 
			context.createUnmarshaller(), callContext, blockSize);
	}
	
	static public <Type> WSIterable<Type> jaxbIterable(Class<Type> elementType, 
		IteratorInitializationType iteratorInitializer,
		Unmarshaller unmarshaller, int blockSize) 
			throws ResourceException, GenesisIISecurityException
	{
		return jaxbIterable(elementType, iteratorInitializer,
			unmarshaller, null, blockSize);
	}
	
	static public <Type> WSIterable<Type> jaxbIterable(Class<Type> elementType, 
		IteratorInitializationType iteratorInitializer,
		Unmarshaller unmarshaller, ICallingContext callContext, int blockSize)
			throws ResourceException, GenesisIISecurityException
	{
		return new WSIterable<Type>(elementType, unmarshaller,
			null, iteratorInitializer, callContext, blockSize);
	}
	
	static public <Type> WSIterable<Type> axisIterable(Class<Type> elementType,
		IteratorInitializationType iteratorInitializationType, int blockSize)
			throws ResourceException, GenesisIISecurityException
	{
		return axisIterable(elementType, iteratorInitializationType, null,
			blockSize);
	}
	
	static public <Type> WSIterable<Type> axisIterable(Class<Type> elementType,
		IteratorInitializationType iteratorInitializationType,
		ICallingContext callContext, int blockSize) 
			throws ResourceException, GenesisIISecurityException
	{
		return new WSIterable<Type>(elementType, null,
			null, iteratorInitializationType, callContext, blockSize);
	}
	
	static public <Type> WSIterable<Type> axisIterable(Class<Type> elementType,
		Type []initialElementOverride,
		IteratorInitializationType iteratorInitializationType,
		ICallingContext callContext, int blockSize) 
			throws ResourceException, GenesisIISecurityException
	{
		return new WSIterable<Type>(elementType, null,
			initialElementOverride,
			iteratorInitializationType, callContext, blockSize);
	}
}