package edu.virginia.vcgr.genii.container.byteio;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.container.resource.IResource;

public abstract class DefaultByteIOAttributePreFetcher<Type extends IResource>
	extends ByteIOAttributePreFetcher<Type>
{
	protected DefaultByteIOAttributePreFetcher(Type resource)
	{
		super(resource);
	}
	
	@Override
	protected URI[] getTransferMechanisms() throws Throwable
	{
		return new URI[] {
			ByteIOConstants.TRANSFER_TYPE_DIME_URI,
			ByteIOConstants.TRANSFER_TYPE_MTOM_URI,
			ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI
		};
	}
}