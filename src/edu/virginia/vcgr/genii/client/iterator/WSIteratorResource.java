package edu.virginia.vcgr.genii.client.iterator;

import java.rmi.RemoteException;

import org.apache.axis.types.UnsignedLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.iterator.IterateRequestType;
import edu.virginia.vcgr.genii.iterator.IterateResponseType;
import edu.virginia.vcgr.genii.iterator.WSIteratorPortType;

class WSIteratorResource
{
	static private Log _logger = LogFactory.getLog(WSIteratorResource.class);
	
	private EndpointReferenceType _iteratorEPR;
	private WSIteratorPortType _iterator;
	private int _count;
	
	@Override
	protected void finalize() throws Throwable
	{
		if (_iterator != null)
			_iterator.destroy(new Destroy());

		_iterator = null;
	}
	
	WSIteratorResource(EndpointReferenceType iteratorEPR,
		WSIteratorPortType iterator)
	{
		_iteratorEPR = iteratorEPR;
		_iterator = iterator;
		_count = 1;
	}
	
	synchronized void retain()
	{
		_count++;
	}
	
	synchronized void release()
	{
		_count--;
		if (_count <= 0 && _iterator != null)
		{
			try
			{
				_iterator.destroy(new Destroy());
				_iterator = null;
			}
			catch (Throwable cause)
			{
				_logger.warn("Error trying to destroy iterator!", cause);
			}
		}
	}
	
	EndpointReferenceType iteratorEPR()
	{
		return _iteratorEPR;
	}
	
	IterateResponseType iterate(int startOffset, int elementCount) 
		throws RemoteException
	{
		if (_iterator == null)
			return null;
		
		return _iterator.iterate(new IterateRequestType(
			new UnsignedLong(startOffset),
			new UnsignedLong(elementCount)));
	}
}