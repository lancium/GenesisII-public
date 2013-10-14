package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;

import org.ggf.sbyteio.StreamableByteIOPortType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;

class StreamableByteIOFactoryOpenFile extends GeniiOpenFile
{
	private StreamableByteIOPortType _target;
	private StreamableByteIOOpenFile _file;

	StreamableByteIOFactoryOpenFile(String[] path, EndpointReferenceType target, boolean canRead, boolean canWrite,
		boolean isAppend) throws ResourceException, GenesisIISecurityException, RemoteException, IOException
	{
		super(path, canRead, canWrite, isAppend);

		StreamableByteIOFactory factory = ClientUtils.createProxy(StreamableByteIOFactory.class, target);
		target = factory.openStream(null).getEndpoint();
		_target = ClientUtils.createProxy(StreamableByteIOPortType.class, target);
		_file = new StreamableByteIOOpenFile(path, false, _target, canRead, canWrite, isAppend);
	}

	@Override
	public void flush() throws FSException
	{
		_file.flush();
	}

	@Override
	protected void closeImpl() throws IOException
	{
		_file.closeImpl();
		_target.destroy(new Destroy());
	}

	@Override
	protected void appendImpl(ByteBuffer source) throws FSException
	{
		_file.appendImpl(source);
	}

	@Override
	protected void readImpl(long offset, ByteBuffer target) throws FSException
	{
		_file.readImpl(offset, target);
	}

	@Override
	protected void writeImpl(long offset, ByteBuffer source) throws FSException
	{
		_file.writeImpl(offset, source);
	}
}