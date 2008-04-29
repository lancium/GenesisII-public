package edu.virginia.vcgr.ogrsh.server.file;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.ogrsh.server.dir.StatBuffer;
import edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException;

public class RandomByteIOFileDescriptor extends AbstractFileDescriptor
{
	static private Log _logger = LogFactory.getLog(RandomByteIOFileDescriptor.class);
	
	private EndpointReferenceType _epr;
	private RandomByteIOTransferer _transferer;
	private long _offset = 0;
	
	public RandomByteIOFileDescriptor(EndpointReferenceType epr,
		boolean isReadable, boolean isWriteable, boolean isAppend,
		boolean isTruncate) throws OGRSHException
	{
		super(isReadable, isWriteable, isAppend);
	
		_epr = epr;
		try
		{
			RandomByteIOPortType stub =
				ClientUtils.createProxy(RandomByteIOPortType.class, epr);
			_transferer = 
				RandomByteIOTransfererFactory.createRandomByteIOTransferer(
					stub);
			
			if (isTruncate)
				_transferer.truncAppend(0, new byte[0]);
			
			_offset = 0;
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
			byte []data = _transferer.read(_offset, length, 1, 0);
			_offset += data.length;
			return data;
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
			if (isAppend())
			{
				_transferer.append(data);
				_offset += data.length;
				return data.length;
			} else
			{
				_transferer.write(_offset, data.length, 0, data);
				_offset += data.length;
				return data.length;
			}
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
	
	public long lseek64(long offset, int whence)
	{
		switch (whence)
		{
			case IFileDescriptor.SEEK_SET :
				_offset = offset;
				break;
			
			case IFileDescriptor.SEEK_CUR :
				_offset += offset;
				break;
				
			case IFileDescriptor.SEEK_END :
				TypeInformation ti = new TypeInformation(_epr);
				_offset = ti.getByteIOSize() + offset;
				break;
		}
		
		return _offset;
	}
	
	public void truncate(long offset) throws OGRSHException
	{
		try
		{
			_transferer.truncAppend(offset, new byte[0]);
		}
		catch (Throwable cause)
		{
			throw new OGRSHException(cause);
		}
	}
}
