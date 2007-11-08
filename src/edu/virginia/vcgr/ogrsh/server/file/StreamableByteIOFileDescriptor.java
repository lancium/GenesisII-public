package edu.virginia.vcgr.ogrsh.server.file;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;
import edu.virginia.vcgr.genii.client.byteio.xfer.ISByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.TransferUtils;
import edu.virginia.vcgr.genii.client.byteio.xfer.dime.DimeSByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.mtom.MTomSByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.simple.SimpleSByteIOTransferer;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.ogrsh.server.dir.StatBuffer;
import edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException;

public class StreamableByteIOFileDescriptor extends AbstractFileDescriptor
		implements IFileDescriptor 
{
	static private Log _logger = LogFactory.getLog(RandomByteIOFileDescriptor.class);
	
	static private QName _POSITION_ATTR_NAME = new QName(
		"http://schemas.ggf.org/byteio/2005/10/streamable-access",
		"Position");
	
	private EndpointReferenceType _epr;
	private ISByteIOTransferer _transferer;
	
	public StreamableByteIOFileDescriptor(EndpointReferenceType epr,
		boolean isReadable, boolean isWriteable, boolean isAppend) throws OGRSHException
	{
		super(isReadable, isWriteable, isAppend);
	
		_epr = epr;
		try
		{
			TransferUtils tu = new TransferUtils(epr);
			URI xferType = tu.getPreferredTransferType();
			StreamableByteIOPortType stub =
				ClientUtils.createProxy(StreamableByteIOPortType.class, epr);
			
			if (xferType.equals(ByteIOConstants.TRANSFER_TYPE_DIME_URI))
				_transferer = new DimeSByteIOTransferer(stub);
			else if (xferType.equals(ByteIOConstants.TRANSFER_TYPE_MTOM_URI))
				_transferer = new MTomSByteIOTransferer(stub);
			else
				_transferer = new SimpleSByteIOTransferer(stub);
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
			StreamableByteIOPortType stub =
				ClientUtils.createProxy(StreamableByteIOPortType.class, _epr);
			
			return Long.parseLong(stub.getAttributes(
				new QName[] {_POSITION_ATTR_NAME}).get_any()[0].getValue());
		}
		catch (ConfigurationException ce)
		{
			throw new OGRSHException(ce);
		}
	}

	public void close() throws IOException
	{
		try
		{
			StreamableByteIOPortType stub =
				ClientUtils.createProxy(StreamableByteIOPortType.class, _epr);
			
			stub.immediateTerminate(null);
		}
		catch (ConfigurationException ce)
		{
			throw new IOException("Unable to close streamable resource.", ce);
		}
	}
}
