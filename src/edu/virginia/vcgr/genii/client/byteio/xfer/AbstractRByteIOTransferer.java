package edu.virginia.vcgr.genii.client.byteio.xfer;

import java.io.IOException;

import org.ggf.rbyteio.RandomByteIOPortType;

public abstract class AbstractRByteIOTransferer 
	extends AbstractByteIOTransferer implements IRByteIOTransferer
{
	protected RandomByteIOPortType _target;
	
	protected AbstractRByteIOTransferer(RandomByteIOPortType target)
	{
		_target = target;
	}
	
	public void close() throws IOException
	{
	}
}