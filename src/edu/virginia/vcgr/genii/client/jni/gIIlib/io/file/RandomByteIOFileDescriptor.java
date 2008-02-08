package edu.virginia.vcgr.genii.client.jni.gIIlib.io.file;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.xfer.IRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.TransferUtils;
import edu.virginia.vcgr.genii.client.byteio.xfer.dime.DimeRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.mtom.MtomRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.simple.SimpleRByteIOTransferer;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class RandomByteIOFileDescriptor extends WindowsIFSFile
{		
	private IRByteIOTransferer _transferer;
	private long _offset = 0;
	
	public RandomByteIOFileDescriptor(RNSPath path, 
			EndpointReferenceType epr, boolean isReadable, 
			boolean isWriteable, boolean isAppend, boolean isTruncate) 
		throws IOException
	{
		super(path, isReadable, isWriteable, isAppend);
	
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
			throw new IOException(cause.getMessage());
		}
	}
	
	@Override
	protected byte[] doRead(int length) throws IOException
	{
		try
		{
			byte []data = _transferer.read(_offset, length, 1, 0);
			_offset += data.length;
			return data;
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
			throw new IOException(re.getMessage());
		}
	}
	
	@Override
	public long lseek64(long offset)
	{
		_offset = offset;
		
		return _offset;
	}
	
	@Override
	public int doTruncateAppend(long offset, byte[] data){
		_offset = offset;
		try{
			_transferer.truncAppend(offset, data);
			_offset += data.length;
			return data.length;			
		}catch(RemoteException re){
			System.out.println("IOException: Remote Connection lost");
			return -1;
		}
	}
}
