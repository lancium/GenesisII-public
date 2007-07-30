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

import org.morgan.util.GUID;

import junit.framework.TestCase;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class GUIDTest extends TestCase
{
	public void testGeneration()
	{
		GUID g1 = new GUID();
		GUID g2 = new GUID();
		
		TestCase.assertFalse(g1.equals(g2));
		TestCase.assertFalse(g2.equals(g1));
		TestCase.assertEquals(g1, g1);
	}
	
	public void testStrings()
	{
		GUID g1 = new GUID();
		String str = g1.toString();
		GUID g2 = GUID.fromString(str);
		
		TestCase.assertEquals(g1, g2);
	}
}
