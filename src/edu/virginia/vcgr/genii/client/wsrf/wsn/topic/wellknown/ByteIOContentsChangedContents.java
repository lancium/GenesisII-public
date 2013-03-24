package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOOperations;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.container.sync.VersionVector;

@XmlRootElement(namespace = ByteIOConstants.BYTEIO_NS, name = "ByteIOContentsChangedContents")
public class ByteIOContentsChangedContents extends NotificationMessageContents
{
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = ByteIOConstants.BYTEIO_NS, name = "operation", nillable = false, required = true)
	private ByteIOOperations _operation;

	@XmlElement(namespace = ByteIOConstants.BYTEIO_NS, name = "offset")
	private long _offset;

	@XmlElement(namespace = ByteIOConstants.BYTEIO_NS, name = "bytesPerBlock")
	private int _bytesPerBlock;

	@XmlElement(namespace = ByteIOConstants.BYTEIO_NS, name = "stride")
	private long _stride;

	@XmlElement(namespace = ByteIOConstants.BYTEIO_NS, name = "size")
	private int _size;

	@XmlElement(namespace = ByteIOConstants.BYTEIO_NS, name = "versionVector")
	private VersionVector _versionVector;

	protected ByteIOContentsChangedContents()
	{
	}

	public ByteIOContentsChangedContents(ByteIOOperations operation, long offset, int bytesPerBlock, long stride, int size)
	{
		_operation = operation;
		_offset = offset;
		_bytesPerBlock = bytesPerBlock;
		_stride = stride;
		_size = size;
	}

	public ByteIOContentsChangedContents(ByteIOOperations operation, long offset, int bytesPerBlock, long stride, int size,
		VersionVector versionVector)
	{
		_operation = operation;
		_offset = offset;
		_bytesPerBlock = bytesPerBlock;
		_stride = stride;
		_size = size;
		_versionVector = versionVector;
	}

	final public ByteIOOperations operation()
	{
		return _operation;
	}

	final public long offset()
	{
		return _offset;
	}

	final public int bytesPerBlock()
	{
		return _bytesPerBlock;
	}

	final public long stride()
	{
		return _stride;
	}

	final public int size()
	{
		return _size;
	}

	final public VersionVector versionVector()
	{
		return _versionVector;
	}
}