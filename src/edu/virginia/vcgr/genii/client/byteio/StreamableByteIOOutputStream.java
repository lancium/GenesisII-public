package edu.virginia.vcgr.genii.client.byteio;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.ggf.sbyteio.StreamableByteIOPortType;
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

/**
 * An implementation of the standard Java Output stream that writes
 * to remote Streamable ByteIO resources.
 * 
 * @author mmm2a
 */
public class StreamableByteIOOutputStream extends OutputStream
{
	private StreamableByteIOTransferer _transferer;
	private StreamableByteIORP _rp;
	private EndpointReferenceType _targetByteIO = null;
	private EndpointReferenceType _createdSByteIO = null;
	private Boolean _destroyOnClose = null;
	
	/**
	 * Create a new StreamableByteIO output stream for a given endpoint and
	 * transfer protocol.
	 * 
	 * @param epr The target ByteIO to write bytes to.
	 * @param desiredTransferProtocol The desired transfer protocol to use when
	 * reading bytes.
	 * 
	 * @throws ConfigurationExceptionMOOCH
	 * @throws RemoteException
	 */
	public StreamableByteIOOutputStream(EndpointReferenceType epr,
		URI desiredTransferProtocol) 
			throws IOException, RemoteException
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
			throw new IOException("Unable to create RP interface.", rpe);
		}
		
		_targetByteIO = epr;
		StreamableByteIOPortType clientStub = ClientUtils.createProxy(
			StreamableByteIOPortType.class, epr);
		StreamableByteIOTransfererFactory factory = 
			new StreamableByteIOTransfererFactory(clientStub);
		_transferer = factory.createStreamableByteIOTransferer(
			desiredTransferProtocol);
	}
	
	/**
	 * Create a new StreamableByteIO output stream for a given endpoint.
	 * 
	 * @param epr The target ByteIO to write bytes to.
	 * 
	 * @throws ConfigurationExceptionMOOCH
	 * @throws RemoteException
	 */
	public StreamableByteIOOutputStream(EndpointReferenceType epr)
		throws IOException, RemoteException
	{
		this(epr, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(int b) throws IOException
	{
		write(new byte[] { (byte)b });
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte []b) throws IOException
	{
		_transferer.seekWrite(SeekOrigin.SEEK_CURRENT, 0, b);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte []b, int off, int len) throws IOException
	{
		byte []data = new byte[len];
		System.arraycopy(b, off, data, 0, len);
		write(data);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	synchronized public void close() throws IOException
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
	
	/**
	 * Create a new buffered output stream based off of this output stream
	 * using the target transferer's preferred transfer size.
	 * 
	 * @return The newly created buffered output stream.
	 */
	public BufferedOutputStream createPreferredBufferedStream()
	{
		return new BufferedOutputStream(this, 
			_transferer.getPreferredWriteSize());
	}
	
	/**
	 * Determines whether or not this streamable byteIO should be
	 * destroyed (the target resource that is) when closed.  This
	 * depends on a number of factors including whether or not the
	 * target resource was actually a streamable ByteIO when it started,
	 * or merely a factory that could create then (as snapshots).
	 * 
	 * @return true if the target should be destroyed when closed, false
	 * otherwise.
	 */
	synchronized private boolean destroyOnClose()
	{
		if (_destroyOnClose != null)
			return _destroyOnClose.booleanValue();
		
		_destroyOnClose = _rp.getDestroyOnClose();
		if (_destroyOnClose == null)
			_destroyOnClose = Boolean.FALSE;
		
		return _destroyOnClose.booleanValue();
	}
	
	/**
	 * Determines whether or not this streamable byteIO should be
	 * destroyed (the target resource that is) when closed.  This
	 * depends on a number of factors including whether or not the
	 * target resource was actually a streamable ByteIO when it started,
	 * or merely a factory that could create then (as snapshots).
	 */
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