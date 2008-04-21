package edu.virginia.vcgr.genii.client.byteio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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

/**
 * An implementation of the standard Java Input stream that reads
 * from remote Streamable ByteIO resources.
 * 
 * @author mmm2a
 */
public class StreamableByteIOInputStream extends InputStream
{
	static final private long MAX_SLEEP = 1000 * 8;
	
	private StreamableByteIOTransferer _transferer;
	private StreamableByteIORP _rp;
	private EndpointReferenceType _targetByteIO = null;
	private EndpointReferenceType _createdSByteIO = null;
	private long _nextSeek = 0L;
	private boolean _endOfStream = false;
	private Boolean _destroyOnClose = null;
	
	/**
	 * Create a new StreamableByteIO input stream for a given endpoint and
	 * transfer protocol.
	 * 
	 * @param epr The source ByteIO to read bytes from.
	 * @param desiredTransferProtocol The desired transfer protocol to use when
	 * reading bytes.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	public StreamableByteIOInputStream(EndpointReferenceType epr,
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
	
	/**
	 * Create a new StreamableByteIO input stream for a given endpoint.
	 * 
	 * @param epr The source ByteIO to read bytes from.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	public StreamableByteIOInputStream(EndpointReferenceType epr)
		throws ConfigurationException, RemoteException
	{
		this(epr, null);
	}
	
	/**
	 * Determine whether or not the EOF has been reached.
	 * 
	 * @return true if the stream is at EOF, false otherwise.
	 */
	private boolean determineEndOfStream()
	{
		Boolean eof = _rp.getEOF();
		if (eof == null)
			return false;
		
		return eof.booleanValue();
	}
	
	/**
	 * A convenience method for reading a block of data.
	 * 
	 * @param length The number of bytes to read.
	 * 
	 * @return The block of data read.
	 * 
	 * @throws IOException
	 */
	private byte[] read(int length) throws IOException
	{
		long sleep = 1000L;
		
		try
		{
			while (!_endOfStream)
			{
				byte []data = _transferer.seekRead(SeekOrigin.SEEK_CURRENT, 
					_nextSeek, length);
				_nextSeek = 0L;
				
				if (data.length > 0)
					return data;
				
				if (data.length == 0)
					_endOfStream = determineEndOfStream();
				
				if (!_endOfStream)
				{
					Thread.sleep(sleep);
					sleep = Math.min(sleep << 2, MAX_SLEEP);
				}
			}
			
			return new byte[0];
		}
		catch (InterruptedException ie)
		{
			throw new IOException("I/O Operation interrupted.", ie);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException
	{
		byte []data = read(1);
		if (data.length == 0)
			return -1;
		return data[0];
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte []b) throws IOException
	{
		byte []data = read(b.length);
		if (data.length == 0)
			return -1;
		System.arraycopy(data, 0, b, 0, data.length);
		return data.length;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte []b, int off, int len) throws IOException
	{
		byte []data = read(len);
		if (data.length == 0)
			return -1;
		System.arraycopy(data, 0, b, off, data.length);
		return data.length;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long skip(long n) throws IOException
	{
		Long position = _rp.getPosition();
		_nextSeek += n;
		if (position == null)
			return -1L;
		
		return position.longValue() + _nextSeek;
	}
	
	/**
	 * {@inheritDoc}
	 */
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
	
	/**
	 * Create a new buffered input stream based off of this input stream
	 * using the target transferer's preferred transfer size.
	 * 
	 * @return The newly created buffered input stream.
	 */
	public BufferedInputStream createPreferredBufferedStream()
	{
		return new BufferedInputStream(this, 
			_transferer.getPreferredReadSize());
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