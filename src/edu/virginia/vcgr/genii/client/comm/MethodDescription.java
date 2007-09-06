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
package edu.virginia.vcgr.genii.client.comm;

import java.lang.reflect.Method;

public class MethodDescription
{
	private Method _method;

	public MethodDescription(Method m)
	{
		_method = m;
	}
	
	public int hashCode()
	{
		return _method.getName().hashCode();
	}
	
	public boolean equals(MethodDescription other)
	{
		if (other._method.getName().equals(_method.getName()))
		{
			Class<?> []otherParameterTypes = other._method.getParameterTypes();
			Class<?> []myParameterTypes = _method.getParameterTypes();
			
			if (otherParameterTypes.length == myParameterTypes.length)
			{
				for (int lcv = 0; lcv < myParameterTypes.length; lcv++)
				{
					if (!otherParameterTypes[lcv].equals(myParameterTypes[lcv]))
						return false;
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public boolean equals(Object other)
	{
		return equals((MethodDescription)other);
	}
	
	public Method getMethod()
	{
		return _method;
	}
}
