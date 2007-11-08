package edu.virginia.vcgr.ogrsh.server.file;

import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.xfer.IRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.TransferUtils;
import edu.virginia.vcgr.genii.client.byteio.xfer.dime.DimeRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.mtom.MtomRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.simple.SimpleRByteIOTransferer;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.ogrsh.server.dir.StatBuffer;
import edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException;

public class RandomByteIOFileDescriptor extends AbstractFileDescriptor
{
	static private Log _logger = LogFactory.getLog(RandomByteIOFileDescriptor.class);
	
	private EndpointReferenceType _epr;
	private IRByteIOTransferer _transferer;
	private long _offset = 0;
	
	public RandomByteIOFileDescriptor(EndpointReferenceType epr,
		boolean isReadable, boolean isWriteable, boolean isAppend,
		boolean isTruncate) throws OGRSHException
	{
		super(isReadable, isWriteable, isAppend);
	
		_epr = epr;
		try
		{
			TransferUtils tu = new TransferUtils(epr);
			URI xferType = tu.getPreferredTransferType();
			RandomByteIOPortType stub =
				ClientUtils.createProxy(RandomByteIOPortType.class, epr);
			
			if (xferType.equals(ByteIOConstants.TRANSFER_TYPE_DIME_URI))
				_transferer = new DimeRByteIOTransferer(stub);
			else if (xferType.equals(ByteIOConstants.TRANSFER_TYPE_MTOM_URI))
				_transferer = new MtomRByteIOTransferer(stub);
			else
				_transferer = new SimpleRByteIOTransferer(stub);
			
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
}
