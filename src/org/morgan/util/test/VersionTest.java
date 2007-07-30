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

import org.morgan.util.Version;

import junit.framework.TestCase;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class VersionTest extends TestCase
{
	public void testSimpleVersion()
	{
		Version a = new Version(1);
		Version b = new Version(1, 0);
		Version c = new Version(1, 0, 0);
		Version d = new Version(2, 1);
		Version e = new Version(2, 1, 1);
		Version f = new Version("3.1.4");
		
		TestCase.assertEquals(a, b);
		TestCase.assertEquals(a, c);
		TestCase.assertFalse(a.equals(d));
		TestCase.assertEquals("2.1.1", e.toString());
		TestCase.assertEquals(new Version(3, 1, 4), f);
	}
}
