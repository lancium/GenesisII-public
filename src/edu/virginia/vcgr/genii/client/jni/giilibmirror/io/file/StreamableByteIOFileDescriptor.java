package edu.virginia.vcgr.genii.client.jni.giilibmirror.io.file;

import java.io.IOException;
import java.rmi.RemoteException;

import org.ggf.sbyteio.StreamableByteIOPortType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;
import edu.virginia.vcgr.genii.client.byteio.StreamableByteIORP;
import edu.virginia.vcgr.genii.client.byteio.transfer.StreamableByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.StreamableByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;

public class StreamableByteIOFileDescriptor extends IFSFile		
{		
	private EndpointReferenceType _epr;
	private StreamableByteIOTransferer _transferer;
	
	public StreamableByteIOFileDescriptor(RNSPath path, EndpointReferenceType epr,
		boolean isReadable, boolean isWriteable, boolean isAppend) throws IOException
	{
		super(path, isReadable, isWriteable, isAppend);
		isAStream = true;
		
		_epr = epr;
		try
		{
			StreamableByteIOPortType stub =
				ClientUtils.createProxy(StreamableByteIOPortType.class, epr);
			
			_transferer = StreamableByteIOTransfererFactory.createStreamableByteIOTransferer(stub);
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
		catch (ResourcePropertyException rpe)
		{
			throw new IOException("Unable to seek.", rpe);
		}
		catch (RemoteException re)
		{
			throw new IOException("Unable to seek.", re);
		}
	}
	
	private long getPosition() 
		throws RemoteException, IOException, ResourcePropertyException
	{
		StreamableByteIORP rp = 
			(StreamableByteIORP)ResourcePropertyManager.createRPInterface(
				_epr, StreamableByteIORP.class);
		return rp.getPosition();
	}

	@Override
	public void doClose() throws IOException
	{
		StreamableByteIOPortType stub =
			ClientUtils.createProxy(StreamableByteIOPortType.class, _epr);
			
		stub.destroy(new Destroy());
	}
	
	@Override
	public int doTruncateAppend(long offset, byte[] data){
		System.out.println("Change in file size is not supported for streams");
		return -1;		
	}
}
