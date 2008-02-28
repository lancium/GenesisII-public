package edu.virginia.vcgr.genii.client.byteio.xfer;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.axis.message.MessageElement;
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;

public abstract class AbstractSByteIOTransferer 
	extends AbstractByteIOTransferer implements ISByteIOTransferer
{
	protected StreamableByteIOPortType _target;
	
	protected AbstractSByteIOTransferer(StreamableByteIOPortType target)
	{
		_target = target;
	}
	
	public boolean endOfStream() throws RemoteException, IOException
	{
		GetResourcePropertyResponse resp = _target.getResourceProperty(
			ByteIOConstants.END_OF_STREAM_ATTR_NAME);
		MessageElement []any = resp.get_any();
		if (any == null || any.length != 1)
			throw new IOException("Couldn't figure out whether or not we " +
				"are at the end of the stream.");
		try
		{
			return Boolean.valueOf(any[0].getValue()).booleanValue();
		}
		catch (Exception e)
		{
			throw new IOException(e.getLocalizedMessage());
		}
	}

	public long position() throws RemoteException, IOException
	{
		GetResourcePropertyResponse resp = _target.getResourceProperty(
			ByteIOConstants.POSITION_ATTR_NAME);
		MessageElement []any = resp.get_any();
		if (any == null || any.length != 1)
			throw new IOException(
				"Couldn't figure out what our position in the stream is.");
		try
		{
			return Long.valueOf(any[0].getAsString()).longValue();
		}
		catch (Exception e)
		{
			throw new IOException(e.getLocalizedMessage());
		}
	}

	public void close() throws IOException
	{
		_target.destroy(new Destroy());
	}
}
