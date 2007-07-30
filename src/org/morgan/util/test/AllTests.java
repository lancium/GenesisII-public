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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.morgan.util.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(VersionTest.class);
		suite.addTestSuite(VersionListTest.class);
		suite.addTestSuite(EventTest.class);
		suite.addTestSuite(DefaultCompleterTest.class);
		suite.addTestSuite(StupidTest.class);
		suite.addTestSuite(XMLConfigurationTest.class);
		suite.addTestSuite(AlarmTest.class);
		suite.addTestSuite(GUIDTest.class);
		suite.addTestSuite(RationalNumberTest.class);
		//$JUnit-END$
		return suite;
	}

}
