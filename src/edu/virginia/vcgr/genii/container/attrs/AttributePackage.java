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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;

final public class AttributePackage
{
	private HashMap<QName, IAttributeManipulator> _manipulators =
		new HashMap<QName, IAttributeManipulator>();
	
	final public void addManipulator(IAttributeManipulator manipulator)
	{
		synchronized(_manipulators)
		{
			_manipulators.put(manipulator.getAttributeQName(), manipulator);
		}
	}
	
	final public ArrayList<IAttributeManipulator> getManipulators()
	{
		ArrayList<IAttributeManipulator> manipulators;
		
		synchronized(_manipulators)
		{
			manipulators = new ArrayList<IAttributeManipulator>(
				_manipulators.values());
		}
		
		return manipulators;
	}
	
	final public IAttributeManipulator getManipulator(QName attrName)
	{
		synchronized(_manipulators)
		{
			return _manipulators.get(attrName); 
		}
	}
	
	final public Map<QName, Collection<MessageElement>> getUnknownAttributes(
		IResource resource) throws ResourceException
	{
		Map<QName, Collection<MessageElement>> map = new HashMap<QName, Collection<MessageElement>>();
		Collection<MessageElement> attrs = resource.getUnknownAttributes();

		for (MessageElement e : attrs)
		{
			QName name = e.getQName();
			Collection<MessageElement> subList = map.get(name);
			if (subList == null)
				map.put(name, subList = new ArrayList<MessageElement>());
			subList.add(e);
		}
		
		return map;
	}
	
	final public void setUnknownAttributes(IResource resource, Collection<MessageElement> newAttributes) throws ResourceException
	{
		Map<QName, Collection<MessageElement>> map = new HashMap<QName, Collection<MessageElement>>();
		for (MessageElement e : newAttributes)
		{
			QName name = e.getQName();
			Collection<MessageElement> subList = map.get(name);
			if (subList == null)
				map.put(name, subList = new ArrayList<MessageElement>());
			subList.add(e);
		}
		
		resource.setUnknownAttributes(map);
	}
	
	final public void deleteUnknownAttributes(IResource resource, QName attrs) throws ResourceException
	{
		Set<QName> set = new HashSet<QName>();
		set.add(attrs);
		
		resource.deleteUnknownAttributes(set);
	}
}