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
package edu.virginia.vcgr.genii.container.rfork;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import org.apache.axis.message.MessageElement;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;

public abstract class ByteIOAttributeHandlers extends AbstractAttributeHandler
{
	protected ByteIOResourceFork _fork;

	public ByteIOAttributeHandlers(ByteIOResourceFork fork, AttributePackage pkg) throws NoSuchMethodException
	{
		super(pkg);

		_fork = fork;
	}

	private long getSize() throws ResourceException, ResourceUnknownFaultType
	{
		return _fork.size();
	}

	private boolean getReadable() throws ResourceException, ResourceUnknownFaultType
	{
		return _fork.readable();
	}

	private boolean getWriteable() throws ResourceException, ResourceUnknownFaultType
	{
		return _fork.writable();
	}

	private Calendar getCreateTime() throws ResourceException, ResourceUnknownFaultType
	{
		return _fork.createTime();
	}

	private Calendar getModificationTime() throws ResourceException, ResourceUnknownFaultType
	{
		return _fork.modificationTime();
	}

	private void setModificationTime(Calendar c) throws ResourceException, ResourceUnknownFaultType
	{
		_fork.modificationTime(c);
	}

	private Calendar getAccessTime() throws ResourceException, ResourceUnknownFaultType
	{
		return _fork.accessTime();
	}

	private void setAccessTime(Calendar c) throws ResourceException, ResourceUnknownFaultType
	{
		_fork.accessTime(c);
	}

	public MessageElement getSizeAttr() throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetSizeNamespace(), getSize());
	}

	public MessageElement getReadableAttr() throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetReadableNamespace(), getReadable());
	}

	public MessageElement getWriteableAttr() throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetWriteableNamespace(), getWriteable());
	}

	public Collection<MessageElement> getTransferMechsAttr()
	{
		ArrayList<MessageElement> ret = new ArrayList<MessageElement>();

		ret.add(new MessageElement(GetTransferMechanismNamespace(), ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		ret.add(new MessageElement(GetTransferMechanismNamespace(), ByteIOConstants.TRANSFER_TYPE_DIME_URI));
		ret.add(new MessageElement(GetTransferMechanismNamespace(), ByteIOConstants.TRANSFER_TYPE_MTOM_URI));

		return ret;
	}

	public MessageElement getCreateTimeAttr() throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetCreateTimeNamespace(), getCreateTime());
	}

	public MessageElement getModificationTimeAttr() throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetModificationTimeNamespace(), getModificationTime());
	}

	public void setModificationTimeAttr(MessageElement element) throws ResourceException, ResourceUnknownFaultType
	{
		setModificationTime(ObjectDeserializer.toObject(element, Calendar.class));
	}

	public MessageElement getAccessTimeAttr() throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetAccessTimeNamespace(), getAccessTime());
	}

	public void setAccessTimeAttr(MessageElement element) throws ResourceException, ResourceUnknownFaultType
	{
		setAccessTime(ObjectDeserializer.toObject(element, Calendar.class));
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
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