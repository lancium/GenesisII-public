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

import org.w3c.dom.Node;

/**
 * The interface that a user implements to parse a section from an XML
 * configuration file.
 * 
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public interface IXMLConfigurationSectionHandler
{
	public Object parse(Node n) throws ConfigurationException;
}
