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

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses config sections of the form:
 * &lt;mconf:classes&gt;
 * 		&lt;mconf:class name="name" [base="base class"]&gt;class&lt;/mconf:class&gt;
 * &lt;/mconf:classes&gt;
 * Returns this in a HashMap&lt;String, Class&gt; structure.
 * 
 *
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class ClassConfigurationSectionHandler implements
		IXMLConfigurationSectionHandler
{
	static final public String CLASS_ELEMENT_NAME = "class";
	static final public String CLASS_NAME_NAME = "name";
	static final public String CLASS_BASE_CLASS_NAME = "base";

	static public QName CLASS_ELEMENT_QNAME = new QName(
		XMLConfiguration.NAMESPACE, CLASS_ELEMENT_NAME);
	
	public Object parse(Node n) throws ConfigurationException
	{
		HashMap<String, Class> ret = new HashMap<String, Class>();
		
		NodeList children = n.getChildNodes();
		int length = children.getLength();
		
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				QName childQName = XMLConfiguration.getQName(child);
				if (!childQName.equals(CLASS_ELEMENT_QNAME))
					throw new ConfigurationException(
						"Found element with unexpected QName of \"" + 
						childQName + "\".");
				
				NamedNodeMap attrs = child.getAttributes();
				Node nameNode = attrs.getNamedItem(CLASS_NAME_NAME);
				if (nameNode == null)
					throw new ConfigurationException(
						"Couldn't find name attribute.");
				Node baseNode = attrs.getNamedItem(CLASS_BASE_CLASS_NAME);

				Node textNode = child.getFirstChild();
				if (textNode.getNodeType() != Node.TEXT_NODE)
					throw new ConfigurationException(
						"Found class node whose child was NOT a text node.");
				
				String base = (baseNode != null) ? baseNode.getTextContent()
						: null;
				ret.put(nameNode.getTextContent(), 
					findClass(textNode.getTextContent(), base));
			}
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	static protected Class findClass(String className, String baseName)
		throws ConfigurationException
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		try
		{
			Class base = (baseName == null) ? Object.class :
				loader.loadClass(baseName);
			Class target = loader.loadClass(className);
			
			if (!base.isAssignableFrom(target))
				throw new ConfigurationException("Target class \"" + className
					+ "\" is not derived off of (or does not implement) \""
					+ base.getName() + "\".");
			return target;
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ConfigurationException(cnfe);
		}
	}
}
