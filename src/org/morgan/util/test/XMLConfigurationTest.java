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
package org.morgan.util.test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.PropertiesConfigurationSectionHandler;
import org.morgan.util.configuration.XMLConfiguration;


import junit.framework.TestCase;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class XMLConfigurationTest extends TestCase
{
	private XMLConfiguration _conf = null;
	
	protected void setUp() throws Exception
	{
		InputStream in = null;
		
		try
		{
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			in = loader.getResourceAsStream(
				"org/morgan/util/test/example-configuration.xml");
			_conf = new XMLConfiguration(in);
		}
		finally
		{
			if (in != null)
				in.close();
		}
	}
	
	public void testParsing()
	{
		TestCase.assertNotNull(_conf);
	}
	
	public void testPropertiesHandler()
	{
		Properties props = (Properties)_conf.retrieveSection(
			new QName("http://www.mark-morgan.net/org/morgan/util/test",
				"test-properties"));
		
		TestCase.assertNotNull(props);
		
		String mark = props.getProperty("Mark");
		String jodie = props.getProperty("Jodie");
		String matt = props.getProperty("Matt");
		
		TestCase.assertNotNull(mark);
		TestCase.assertNotNull(jodie);
		TestCase.assertNull(matt);
		
		TestCase.assertEquals("Morgan", mark);
		TestCase.assertEquals("Martin", jodie);
	}
	
	@SuppressWarnings("unchecked")
	public void testClassHandler()
	{
		HashMap<String, Class> classes = (HashMap<String, Class>)_conf.retrieveSection(
			new QName("http://www.mark-morgan.net/org/morgan/util/test",
				"test-classes"));
		
		TestCase.assertNotNull(classes);
		
		Class alpha = classes.get("alpha");
		Class beta = classes.get("beta");
		
		TestCase.assertNotNull(alpha);
		TestCase.assertNotNull(beta);
		
		TestCase.assertEquals(ConfigurationException.class, alpha);
		TestCase.assertEquals(PropertiesConfigurationSectionHandler.class, beta);
	}
}
