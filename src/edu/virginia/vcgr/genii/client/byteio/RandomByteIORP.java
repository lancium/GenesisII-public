package edu.virginia.vcgr.genii.client.byteio;

import java.util.Calendar;
import java.util.Collection;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.rp.ResourceProperty;

public interface RandomByteIORP
{
	static public final String RANDOM_BYTEIO_NS =
		"http://schemas.ggf.org/byteio/2005/10/random-access";
	
	@ResourceProperty(namespace = RANDOM_BYTEIO_NS, localname = "Size")
	public Long getSize();
	
	@ResourceProperty(namespace = RANDOM_BYTEIO_NS, localname = "Readable")
	public Boolean getReadable();
	
	@ResourceProperty(namespace = RANDOM_BYTEIO_NS, localname = "Writeable")
	public Boolean getWriteable();
	
	@ResourceProperty(namespace = RANDOM_BYTEIO_NS, localname = "TransferMechanism")
	public Collection<QName> getTransferMechanisms();
	
	@ResourceProperty(namespace = RANDOM_BYTEIO_NS, localname = "CreateTime")
	public Calendar getCreateTime();
	
	@ResourceProperty(namespace = RANDOM_BYTEIO_NS, localname = "ModificationTime")
	public Calendar getModificationTime();
	
	@ResourceProperty(namespace = RANDOM_BYTEIO_NS, localname = "AccessTime")
	public Calendar getAccessTime();
}