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

import javax.xml.namespace.QName;

public abstract class AbstractAttributeHandler
{
	private AttributePackage _package;
	protected abstract void registerHandlers() throws NoSuchMethodException;
	
	protected AbstractAttributeHandler(AttributePackage pkg) 
		throws NoSuchMethodException
	{
		_package = pkg;
		
		registerHandlers();
	}
	
	protected void addHandler(QName attributeName,
		String getMethodName) throws NoSuchMethodException
	{
		_package.addManipulator(
			DefaultAttributeManipulator.createManipulator(this, attributeName,
				getMethodName));
	}
	
	protected void addHandler(QName attributeName,
		String getMethodName, String setMethodName) throws NoSuchMethodException
	{
		_package.addManipulator(
			DefaultAttributeManipulator.createManipulator(this, attributeName,
				getMethodName, setMethodName));
	}
}
