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
package org.morgan.util.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InstanceConfigurationSectionHandler 
	extends ClassConfigurationSectionHandler
{
	static final public String INSTANCE_ELEMENT_NAME = "instance";
	static final public String INSTANCE_NAME_NAME = "name";
	static final public String INSTANCE_TYPE_NAME = "type";
	static final public String INSTANCE_BASE_CLASS_NAME = "base";

	static public QName INSTANCE_ELEMENT_QNAME = new QName(
		XMLConfiguration.NAMESPACE, INSTANCE_ELEMENT_NAME);
	
	public Object parse(Node n) throws ConfigurationException
	{
		HashMap<String, Object> ret = new HashMap<String, Object>();
		
		NodeList children = n.getChildNodes();
		int length = children.getLength();
		
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				QName childQName = XMLConfiguration.getQName(child);
				if (!childQName.equals(INSTANCE_ELEMENT_QNAME))
					throw new ConfigurationException(
						"Found element with unexpected QName of \"" + 
						childQName + "\".");
				
				NamedNodeMap attrs = child.getAttributes();
				Node nameNode = attrs.getNamedItem(INSTANCE_NAME_NAME);
				if (nameNode == null)
					throw new ConfigurationException(
						"Couldn't find name attribute.");
				Node typeNode = attrs.getNamedItem(INSTANCE_TYPE_NAME);
				if (typeNode == null)
					throw new ConfigurationException(
						"Couldn't find type attribute.");
				Node baseNode = attrs.getNamedItem(INSTANCE_BASE_CLASS_NAME);
				
				String base = (baseNode != null) ? baseNode.getTextContent()
						: null;
				
				PropertiesConfigurationSectionHandler handler =
					new PropertiesConfigurationSectionHandler();
				Properties props = (Properties)handler.parse(child);
				
				try
				{
					ret.put(nameNode.getTextContent(),
							createInstance(
								findClass(typeNode.getTextContent(), base),
								props));
				}
				catch (Exception e)
				{
				}
			}
		}
		
		return ret;
	}
	
	static protected Object createInstance(Class<?> cl, Properties props)
		throws IllegalAccessException, InvocationTargetException, 
			InstantiationException, NoSuchMethodException
	{
		Constructor<?> cons = null;
		
		try
		{
			cons = cl.getConstructor(new Class[] {Properties.class});
			return cons.newInstance(new Object[] {props});
		}
		catch (NoSuchMethodException nsme)
		{
			cons = cl.getConstructor(new Class[0]);
			return cons.newInstance(new Object[0]);
		}
	}
}
