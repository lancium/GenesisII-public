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
package edu.virginia.vcgr.genii.client.jsdl;

import javax.xml.namespace.QName;

public class UnsupportedJSDLElement extends JSDLException
{
	static final long serialVersionUID = 0;
	
	private QName _jsdlElementName;
	
	public UnsupportedJSDLElement(String msg, QName jsdlElement)
	{
		super(msg);
		_jsdlElementName = jsdlElement;
	}
	
	public UnsupportedJSDLElement(QName jsdlElement)
	{
		this("The jsdl element \"" + jsdlElement.toString() 
			+ "\" is unsupported.", jsdlElement);
	}
	
	public QName getJSDLElementName()
	{
		return _jsdlElementName;
	}
}
