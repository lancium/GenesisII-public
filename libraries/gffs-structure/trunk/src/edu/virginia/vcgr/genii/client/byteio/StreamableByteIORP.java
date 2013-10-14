package edu.virginia.vcgr.genii.client.byteio;

import java.util.Calendar;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.rp.ResourceProperty;

/**
 * An interface that represents the resource properties (or attributes) of a streamable byteio
 * resource. As per the resource property translators in Genesis II, this interface does not need to
 * be realized as a class implementation -- rather, a dynamically generated proxy is created at
 * runtime which has the ability to retrieve and translate the correct attributes.
 * 
 * @author mmm2a
 */
public interface StreamableByteIORP
{
	static public final String STREAMABLE_BYTEIO_NS = "http://schemas.ggf.org/byteio/2005/10/streamable-access";

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "Size")
	public Long getSize();

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "Readable")
	public Boolean getReadable();

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "Writeable")
	public Boolean getWriteable();

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "TransferMechanism", max = "unbounded")
	public Collection<QName> getTransferMechanisms();

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "CreateTime")
	public Calendar getCreateTime();

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "ModificationTime")
	public Calendar getModificationTime();

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "ModificationTime")
	public void setModificationTime(Calendar modTime);

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "AccessTime")
	public Calendar getAccessTime();

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "AccessTime")
	public void setAccessTime(Calendar accessTime);

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "Position")
	public Long getPosition();

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "Seekable")
	public Boolean getSeekable();

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "EndOfStream")
	public Boolean getEOF();

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "DataResource")
	public EndpointReferenceType getDataResource();

	@ResourceProperty(namespace = STREAMABLE_BYTEIO_NS, localname = "DestroyOnClose")
	public Boolean getDestroyOnClose();
}
