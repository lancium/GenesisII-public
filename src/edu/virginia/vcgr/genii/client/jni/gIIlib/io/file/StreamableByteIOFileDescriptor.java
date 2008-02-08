package edu.virginia.vcgr.genii.client.jni.gIIlib.io.file;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.types.URI;
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
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class StreamableByteIOFileDescriptor extends WindowsIFSFile		
{		
	static private QName _POSITION_ATTR_NAME = new QName(
		"http://schemas.ggf.org/byteio/2005/10/streamable-access",
		"Position");
	
	private EndpointReferenceType _epr;
	private ISByteIOTransferer _transferer;
	
	public StreamableByteIOFileDescriptor(RNSPath path, EndpointReferenceType epr,
		boolean isReadable, boolean isWriteable, boolean isAppend) throws IOException
	{
		super(path, isReadable, isWriteable, isAppend);
		
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
			throw new IOException(cause.getMessage());
		}
	}
	
	@Override
	protected byte[] doRead(int length) throws IOException
	{
		try
		{
			return _transferer.seekRead(SeekOrigin.SEEK_CURRENT, 0, length);
		}
		catch (RemoteException re)
		{
			throw new IOException(re.getMessage());
		}
	}

	@Override
	protected int doWrite(byte[] data) throws IOException
	{
		try
		{
			_transferer.seekWrite(SeekOrigin.SEEK_CURRENT, 0, data);
			return data.length;
		}
		catch (RemoteException re)
		{
			throw new IOException(re.getMessage());
		}
	}

	@Override
	public long lseek64(long offset) throws IOException
	{
		SeekOrigin origin = SeekOrigin.SEEK_BEGINNING;
		try
		{
			_transferer.seekRead(origin, offset, 0);
			return getPosition();
		}
		catch (RemoteException re)
		{
			throw new IOException(re.getMessage());
		}
	}
	
	private long getPosition() throws RemoteException, IOException
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
			throw new IOException(ce.getMessage());
		}
	}

	@Override
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
			throw new IOException("Unable to close streamable resource.");
		}
	}
	
	@Override
	public int doTruncateAppend(long offset, byte[] data){
		System.out.println("Change in file size is not supported for streams");
		return -1;		
	}
}
