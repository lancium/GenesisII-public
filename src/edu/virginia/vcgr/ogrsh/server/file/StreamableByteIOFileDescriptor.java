package edu.virginia.vcgr.ogrsh.server.file;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;
import edu.virginia.vcgr.genii.client.byteio.StreamableByteIORP;
import edu.virginia.vcgr.genii.client.byteio.transfer.StreamableByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.StreamableByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.ogrsh.server.dir.StatBuffer;
import edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException;

public class StreamableByteIOFileDescriptor extends AbstractFileDescriptor
		implements IFileDescriptor 
{
	static private Log _logger = LogFactory.getLog(RandomByteIOFileDescriptor.class);
	
	private EndpointReferenceType _epr;
	private StreamableByteIOTransferer _transferer;
	
	public StreamableByteIOFileDescriptor(EndpointReferenceType epr,
		boolean isReadable, boolean isWriteable, boolean isAppend) throws OGRSHException
	{
		super(isReadable, isWriteable, isAppend);
	
		_epr = epr;
		try
		{
			StreamableByteIOPortType stub =
				ClientUtils.createProxy(StreamableByteIOPortType.class, epr);
			_transferer = StreamableByteIOTransfererFactory.createStreamableByteIOTransferer(stub);
		}
		catch (Throwable cause)
		{
			throw new OGRSHException(cause);
		}
	}
	
	@Override
	protected byte[] doRead(int length) throws OGRSHException
	{
		try
		{
			return _transferer.seekRead(SeekOrigin.SEEK_CURRENT, 0, length);
		}
		catch (RemoteException re)
		{
			throw new OGRSHException(re);
		}
	}

	@Override
	protected int doWrite(byte[] data) throws OGRSHException
	{
		try
		{
			_transferer.seekWrite(SeekOrigin.SEEK_CURRENT, 0, data);
			return data.length;
		}
		catch (RemoteException re)
		{
			throw new OGRSHException(re);
		}
	}

	@Override
	public StatBuffer fxstat() throws OGRSHException
	{
		_logger.debug("fxstat'ing file descriptor.");
		
		try
		{
			TypeInformation ti = new TypeInformation(_epr);
			return StatBuffer.fromTypeInformation(ti);
		}
		catch (Throwable cause)
		{
			throw new OGRSHException(cause);
		}
	}

	public long lseek64(long offset, int whence) throws OGRSHException
	{
		SeekOrigin origin = null;
		
		switch (whence)
		{
			case IFileDescriptor.SEEK_CUR :
				origin = SeekOrigin.SEEK_CURRENT;
				break;
			case IFileDescriptor.SEEK_END :
				origin = SeekOrigin.SEEK_END;
				break;
			case IFileDescriptor.SEEK_SET :
				origin = SeekOrigin.SEEK_BEGINNING;
				break;
		}
		
		try
		{
			_transferer.seekRead(origin, offset, 0);
			return getPosition();
		}
		catch (RemoteException re)
		{
			throw new OGRSHException(re);
		}
	}
	
	private long getPosition() throws RemoteException, OGRSHException
	{
		try
		{
			StreamableByteIORP rp = 
				(StreamableByteIORP)ResourcePropertyManager.createRPInterface(
					_epr, StreamableByteIORP.class);
			return rp.getPosition();
		}
		catch (ResourcePropertyException re)
		{
			throw new OGRSHException(re);
		}
	}

	public void close() throws IOException
	{
		try
		{
			StreamableByteIOPortType stub =
				ClientUtils.createProxy(StreamableByteIOPortType.class, _epr);
			
			stub.destroy(new Destroy());
		}
		catch (ConfigurationException ce)
		{
			throw new IOException("Unable to close streamable resource.", ce);
		}
	}
	
	public void truncate(long offset) throws OGRSHException
	{
		throw new OGRSHException("Operation not supported.", OGRSHException.EROFS);
	}
}
