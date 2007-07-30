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
import java.util.HashMap;

import javax.xml.namespace.QName;

public class AttributePackage
{
	private HashMap<QName, IAttributeManipulator> _manipulators =
		new HashMap<QName, IAttributeManipulator>();
	
	public void addManipulator(IAttributeManipulator manipulator)
	{
		synchronized(_manipulators)
		{
			_manipulators.put(manipulator.getAttributeQName(), manipulator);
		}
	}
	
	public ArrayList<IAttributeManipulator> getManipulators()
	{
		ArrayList<IAttributeManipulator> manipulators;
		
		synchronized(_manipulators)
		{
			manipulators = new ArrayList<IAttributeManipulator>(
				_manipulators.values());
		}
		
		return manipulators;
	}
	
	public IAttributeManipulator getManipulator(QName attrName)
	{
		synchronized(_manipulators)
		{
			return _manipulators.get(attrName); 
		}
	}
}
