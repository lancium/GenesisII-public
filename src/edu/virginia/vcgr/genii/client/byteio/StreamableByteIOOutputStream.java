package edu.virginia.vcgr.genii.client.byteio;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;

import edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactory;
import edu.virginia.vcgr.genii.client.byteio.transfer.StreamableByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.StreamableByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class StreamableByteIOOutputStream extends OutputStream
{
	private StreamableByteIOTransferer _transferer;
	private StreamableByteIORP _rp;
	private EndpointReferenceType _targetByteIO = null;
	private EndpointReferenceType _createdSByteIO = null;
	private Boolean _destroyOnClose = null;
	
	public StreamableByteIOOutputStream(EndpointReferenceType epr,
		URI desiredTransferProtocol) 
			throws ConfigurationException, RemoteException
	{
		TypeInformation tInfo = new TypeInformation(epr);
		
		if (tInfo.isSByteIOFactory())
		{
			StreamableByteIOFactory sFactory = ClientUtils.createProxy(
				StreamableByteIOFactory.class, epr);
			_createdSByteIO = sFactory.openStream(null).getEndpoint();
			epr = _createdSByteIO;
		} else
		{
			discoverDestroyOnCloseFromEPR(epr.getMetadata());
		}
		
		try
		{
			_rp = (StreamableByteIORP)ResourcePropertyManager.createRPInterface(
				epr, StreamableByteIORP.class);
		}
		catch (ResourcePropertyException rpe)
		{
			throw new ConfigurationException("Unable to create RP interface.", rpe);
		}
		
		_targetByteIO = epr;
		StreamableByteIOPortType clientStub = ClientUtils.createProxy(
			StreamableByteIOPortType.class, epr);
		StreamableByteIOTransfererFactory factory = 
			new StreamableByteIOTransfererFactory(clientStub);
		_transferer = factory.createStreamableByteIOTransferer(
			desiredTransferProtocol);
	}
	
	public StreamableByteIOOutputStream(EndpointReferenceType epr)
		throws ConfigurationException, RemoteException
	{
		this(epr, null);
	}
	
	@Override
	public void write(int b) throws IOException
	{
		write(new byte[] { (byte)b });
	}
	
	@Override
	public void write(byte []b) throws IOException
	{
		_transferer.seekWrite(SeekOrigin.SEEK_CURRENT, 0, b);
	}
	
	@Override
	public void write(byte []b, int off, int len) throws IOException
	{
		byte []data = new byte[len];
		System.arraycopy(b, off, data, 0, len);
		write(data);
	}
	
	@Override
	synchronized public void close() throws IOException
	{
		try
		{
			if (_createdSByteIO != null || destroyOnClose())
			{
				GeniiCommon common = ClientUtils.createProxy(
					GeniiCommon.class, _targetByteIO);
				common.destroy(null);
				
				_createdSByteIO = null;
				_targetByteIO = null;
			}
		}
		catch (ConfigurationException ce)
		{
			throw new IOException("Unable to close streamble byteio.", ce);
		}
	}
	
	public BufferedOutputStream createPreferredBufferedStream()
	{
		return new BufferedOutputStream(this, 
			_transferer.getPreferredWriteSize());
	}
	
	synchronized private boolean destroyOnClose()
	{
		if (_destroyOnClose != null)
			return _destroyOnClose.booleanValue();
		
		_destroyOnClose = _rp.getDestroyOnClose();
		if (_destroyOnClose == null)
			_destroyOnClose = Boolean.FALSE;
		
		return _destroyOnClose.booleanValue();
	}
	
	private void discoverDestroyOnCloseFromEPR(MetadataType mdt)
	{
		if (mdt == null)
			return;
		
		MessageElement []any = mdt.get_any();
		if (any == null)
			return;
		
		for (MessageElement me : any)
		{
			QName name = me.getQName();
			if (name.equals(ByteIOConstants.SBYTEIO_DESTROY_ON_CLOSE_FLAG))
			{
				_destroyOnClose = Boolean.valueOf(me.getValue());
				return;
			}
		}
	}
}