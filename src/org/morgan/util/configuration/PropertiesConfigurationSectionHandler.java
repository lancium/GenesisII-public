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

import java.util.Properties;

import javax.xml.namespace.QName;

import org.morgan.util.MacroUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses a configuration section of the form:
 * &lt;mconf:properties&gt;
 * 		&lt;mconf:property name="some-name" value="some-value"/&gt;*
 * &lt;/mconf:properties&gt;
 * 
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class PropertiesConfigurationSectionHandler implements
		IXMLConfigurationSectionHandler
{
	static final public String PROPERTY_NAME = "property";
	static final public String PROPERTY_NAME_NAME = "name";
	static final public String PROPERTY_NAME_VALUE = "value";
	
	static public QName PROPERTY_ELEMENT_QNAME =
		new QName(XMLConfiguration.NAMESPACE, PROPERTY_NAME);
	
	public Object parse(Node n) throws ConfigurationException
	{
		Properties props = new Properties();
		
		NodeList children = n.getChildNodes();
		int length = children.getLength();
		
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				QName childQName = XMLConfiguration.getQName(child);
				if (!childQName.equals(PROPERTY_ELEMENT_QNAME))
					throw new ConfigurationException(
						"Found element with unexpected QName of \"" + 
						childQName + "\".");
				
				NamedNodeMap attrs = child.getAttributes();
				Node nameNode = attrs.getNamedItem(PROPERTY_NAME_NAME);
				if (nameNode == null)
					throw new ConfigurationException(
						"Couldn't find name attribute.");
				Node valueNode = attrs.getNamedItem(PROPERTY_NAME_VALUE);
				if (valueNode == null)
					throw new ConfigurationException(
						"Couldn't find value attribute.");
				
				props.setProperty(nameNode.getTextContent(), 
					MacroUtils.replaceMacros(
						System.getProperties(),
						valueNode.getTextContent()));
			}
		}
		
		return props;
	}
}
