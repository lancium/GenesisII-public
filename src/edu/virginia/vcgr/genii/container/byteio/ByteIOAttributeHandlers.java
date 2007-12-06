/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.morgan.util.io.StreamUtils;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.io.ChecksumStream;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public abstract class ByteIOAttributeHandlers
	extends AbstractAttributeHandler
{
	public ByteIOAttributeHandlers(AttributePackage pkg)
		throws NoSuchMethodException
	{
		super(pkg);
	}
	
	static private long calculateChecksum(File f)
		throws IOException
	{
		byte []fileData = new byte[1024 * 8];
		ChecksumStream in = null;
		try
		{
			in = new ChecksumStream(new FileInputStream(f));
			while (in.read(fileData) >= 0)
			{
				// noop
			}
			return in.getChecksum();
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	private long getChecksum() 
		throws ResourceException, ResourceUnknownFaultType
	{
		IRByteIOResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRByteIOResource)rKey.dereference();
		if (resource.isServiceResource()) {
			return 0;
		}
		
		File f = resource.getCurrentFile();
		if (f.exists())
		{
			Long storedModified = (Long)resource.getProperty(
				"genii.last-modified.property");
			if (storedModified == null || 
				(storedModified.longValue() != f.lastModified()))
			{
				synchronized(rKey.getLockObject())
				{
					try
					{
						long checksum = calculateChecksum(f);
						resource.setProperty("genii.last-modified.property",
							new Long(f.lastModified()));
						resource.setProperty("genii.stored-checksum.property",
							new Long(checksum));
						return checksum;
					}
					catch (IOException ioe)
					{
						throw new ResourceException(
							ioe.getLocalizedMessage(), ioe);
					}
				}
			} else
			{
				return ((Long)resource.getProperty(
					"genii.stored-checksum.property")).longValue();
			}
		}
			
		return 0;
	}
	
	private long getSize() 
		throws ResourceException, ResourceUnknownFaultType
	{
		IRByteIOResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRByteIOResource)rKey.dereference();
		if (resource.isServiceResource()) {
			return 0;
		}
		File f = resource.getCurrentFile();
		if (f.exists())
			return f.length();
			
		return 0;
	}
	
	private boolean getReadable()
		throws ResourceException, ResourceUnknownFaultType
	{
		IRByteIOResource resource = null;
		resource = (IRByteIOResource)(ResourceManager.getCurrentResource().dereference());
		if (resource.isServiceResource()) {
			return true;
		}
		File f = resource.getCurrentFile();
		if (f.exists())
			return f.canRead();
		
		return true;
	}
	
	private boolean getWriteable()
		throws ResourceException, ResourceUnknownFaultType
	{
		IRByteIOResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRByteIOResource)rKey.dereference();
		if (resource.isServiceResource()) {
			return true;
		}
		File f = resource.getCurrentFile();
		if (f.exists())
			return f.canWrite();
		
		return true;
	}
	
	private Calendar getCreateTime()
		throws ResourceException, ResourceUnknownFaultType
	{
		IRByteIOResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRByteIOResource)rKey.dereference();
		if (resource.isServiceResource()) {
			return null;
		}
		return resource.getCreateTime();
	}
	
	private Calendar getModificationTime()
		throws ResourceException, ResourceUnknownFaultType
	{
		IRByteIOResource resource = null;
		resource = (IRByteIOResource)(ResourceManager.getCurrentResource().dereference());
		if (resource.isServiceResource()) {
			return null;
		}
		File f = resource.getCurrentFile();
		if (f.exists())
		{
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(f.lastModified());
			return c;
		}
		
		return null;
	}
	
	private void setModificationTime(Calendar c)
		throws ResourceException, ResourceUnknownFaultType
	{
		IRByteIOResource resource = null;
		resource = (IRByteIOResource)(ResourceManager.getCurrentResource().dereference());
		if (resource.isServiceResource())
			return;
		
		File f = resource.getCurrentFile();
		if (f.exists())
			f.setLastModified(c.getTimeInMillis());
	}
	
	private Calendar getAccessTime()
		throws ResourceException, ResourceUnknownFaultType
	{
		IRByteIOResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRByteIOResource)rKey.dereference();
		if (resource.isServiceResource()) {
			return null;
		}
		return resource.getAccessTime();
	}
	
	private void setAccessTime(Calendar c)
		throws ResourceException, ResourceUnknownFaultType
	{
		IRByteIOResource resource = null;
		resource = (IRByteIOResource)(ResourceManager.getCurrentResource().dereference());
		if (resource.isServiceResource())
			return;
		
		resource.setAccessTime(c);
	}
	
	public MessageElement getChecksumAttr() 
		throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(ByteIOConstants.FILE_CHECKSUM_ATTR_NAME,
			getChecksum());
	}
	
	public MessageElement getSizeAttr() 
		throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetSizeNamespace(), getSize());
	}
	
	public MessageElement getReadableAttr()
		throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetReadableNamespace(), getReadable());
	}
	
	public MessageElement getWriteableAttr()
		throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetWriteableNamespace(), getWriteable());
	}
	
	public Collection<MessageElement> getTransferMechsAttr()
	{
		ArrayList<MessageElement> ret = new ArrayList<MessageElement>();
		
		ret.add(new MessageElement(GetTransferMechanismNamespace(),
			ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		ret.add(new MessageElement(GetTransferMechanismNamespace(),
			ByteIOConstants.TRANSFER_TYPE_DIME_URI));
		ret.add(new MessageElement(GetTransferMechanismNamespace(),
			ByteIOConstants.TRANSFER_TYPE_MTOM_URI));
		
		return ret;
	}
	
	public MessageElement getCreateTimeAttr()
		throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetCreateTimeNamespace(), 
			getCreateTime());
	}
	
	public MessageElement getModificationTimeAttr()
		throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetModificationTimeNamespace(),
			getModificationTime());
	}
	
	public void setModificationTimeAttr(MessageElement element)
		throws ResourceException, ResourceUnknownFaultType
	{
		setModificationTime(ObjectDeserializer.toObject(
			element, Calendar.class));
	}
	
	public MessageElement getAccessTimeAttr()
		throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetAccessTimeNamespace(),
			getAccessTime());
	}
	
	public void setAccessTimeAttr(MessageElement element)
		throws ResourceException, ResourceUnknownFaultType
	{
		setAccessTime(ObjectDeserializer.toObject(element, Calendar.class));
	}
	
	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		//addHandler(ByteIOConstants.FILE_CHECKSUM_ATTR_NAME, "getChecksumAttr");
        /*	
		addHandler(ByteIOConstants.SIZE_ATTR_NAME, "getSizeAttr");
		addHandler(ByteIOConstants.READABLE_ATTR_NAME, "getReadableAttr");
		addHandler(ByteIOConstants.WRITEABLE_ATTR_NAME, "getWriteableAttr");
		addHandler(ByteIOConstants.XFER_MECHS_ATTR_NAME, "getTransferMechsAttr");
		addHandler(ByteIOConstants.CREATTIME_ATTR_NAME, "getCreateTimeAttr");
		addHandler(ByteIOConstants.MODTIME_ATTR_NAME, "getModificationTimeAttr");
		addHandler(ByteIOConstants.ACCESSTIME_ATTR_NAME, "getAccessTimeAttr");
		*/
		addHandler(GetSizeNamespace(), "getSizeAttr");
		addHandler(GetReadableNamespace(), "getReadableAttr");
		addHandler(GetWriteableNamespace(), "getWriteableAttr");
		addHandler(GetTransferMechanismNamespace(), "getTransferMechsAttr");
		addHandler(GetCreateTimeNamespace(), "getCreateTimeAttr");
		addHandler(GetModificationTimeNamespace(), "getModificationTimeAttr", "setModificationTimeAttr");
		addHandler(GetAccessTimeNamespace(), "getAccessTimeAttr", "setAccessTimeAttr");
	}

	protected abstract QName GetSizeNamespace();
	protected abstract QName GetReadableNamespace();
	protected abstract QName GetWriteableNamespace();
	protected abstract QName GetTransferMechanismNamespace();
	protected abstract QName GetCreateTimeNamespace();
	protected abstract QName GetModificationTimeNamespace();
	protected abstract QName GetAccessTimeNamespace();
	
}