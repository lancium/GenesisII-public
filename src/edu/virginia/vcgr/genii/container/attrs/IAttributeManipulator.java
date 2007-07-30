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
package edu.virginia.vcgr.genii.container.attrs;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.common.rattrs.AttributeNotSettableFaultType;
import edu.virginia.vcgr.genii.common.rattrs.IncorrectAttributeCardinalityFaultType;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;

public interface IAttributeManipulator
{
	public QName getAttributeQName();
	
	public boolean allowsSet();
	
	public Collection<MessageElement> getAttributeValues()
		throws ResourceUnknownFaultType, RemoteException;
	public void setAttributeValues(Collection<MessageElement> values)
		throws ResourceUnknownFaultType, RemoteException,
			AttributeNotSettableFaultType, IncorrectAttributeCardinalityFaultType;
}
