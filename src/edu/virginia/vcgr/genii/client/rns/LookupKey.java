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
package edu.virginia.vcgr.genii.client.rns;

import edu.virginia.vcgr.genii.client.naming.WSName;

public class LookupKey
{
	private WSName _name;
	private String _entryExpression;
	
	public LookupKey(WSName name, String entryExpression)
	{
		_name = name;
		_entryExpression = entryExpression;
	}
	
	public int hashCode()
	{
		return _name.hashCode() ^ _entryExpression.hashCode();
	}
	
	public boolean equals(LookupKey other)
	{
		return (_name.equals(other._name) 
			&& _entryExpression.equals(other._entryExpression));
	}
	
	public boolean equals(Object other)
	{
		if (!(other instanceof LookupKey))
			return false;
		
		return equals((LookupKey)other);
	}
}