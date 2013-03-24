package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import java.util.Calendar;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;

@XmlRootElement(namespace = ByteIOConstants.BYTEIO_NS, name = "ByteIOAttributesUpdateNotification")
public class ByteIOAttributesUpdateNotification extends NotificationMessageContents
{

	private static final long serialVersionUID = 0L;

	@XmlElement(namespace = ByteIOConstants.BYTEIO_NS, name = "Size", nillable = false, required = true)
	private long size;

	@XmlElement(namespace = ByteIOConstants.BYTEIO_NS, name = "CreateTime", nillable = false, required = true)
	private Calendar createTime;

	@XmlElement(namespace = ByteIOConstants.BYTEIO_NS, name = "ModificationTime", nillable = false, required = true)
	private Calendar modificationTime;

	@XmlElement(namespace = ByteIOConstants.BYTEIO_NS, name = "AccessTime", nillable = false, required = true)
	private Calendar accessTime;

	public ByteIOAttributesUpdateNotification()
	{
		useIndirectPublishers = true;
		indirectPublishersRetrieveQuery = "SELECT DISTINCT(resourceid) FROM entries WHERE endpoint_id = ?";
	}

	public ByteIOAttributesUpdateNotification(long size, Calendar createTime, Calendar modificationTime, Calendar accessTime)
	{
		this();
		this.size = size;
		this.createTime = createTime;
		this.modificationTime = modificationTime;
		this.accessTime = accessTime;
	}

	@XmlTransient
	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	@XmlTransient
	public Calendar getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Calendar createTime)
	{
		this.createTime = createTime;
	}

	@XmlTransient
	public Calendar getModificationTime()
	{
		return modificationTime;
	}

	public void setModificationTime(Calendar modificationTime)
	{
		this.modificationTime = modificationTime;
	}

	@XmlTransient
	public Calendar getAccessTime()
	{
		return accessTime;
	}

	public void setAccessTime(Calendar accessTime)
	{
		this.accessTime = accessTime;
	}

	@Override
	public boolean isIgnoreBlockedIndirectPublisher(long blockingTime)
	{
		long createTimeInMillis = createTime.getTimeInMillis();
		return (createTimeInMillis > blockingTime);
	}
}
