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

import java.util.HashSet;

import org.morgan.util.DefaultCompleter;
import org.morgan.util.ICompleter;

import junit.framework.TestCase;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class DefaultCompleterTest extends TestCase
{
	private ICompleter<String> _completer;
	
	protected void setUp() throws Exception
	{
		super.setUp();
		
		HashSet<String> strings = new HashSet<String>();
		
		strings.add("Wasson");
		strings.add("Karpovich");
		strings.add("Morgan");
		strings.add("Delvechio");
		strings.add("Martin");
		strings.add("Grimshaw");
		strings.add("Appleton");
		
		_completer = DefaultCompleter.buildCompleter(strings);
	}
	
	public void testCompleter()
	{
		TestCase.assertEquals("Appleton", _completer.complete(""));
		TestCase.assertEquals("Appleton", _completer.complete("A"));
		TestCase.assertEquals("Appleton", _completer.complete("App"));
		TestCase.assertEquals("Martin", _completer.complete("M"));
		TestCase.assertEquals("Morgan", _completer.complete("Mo"));
		TestCase.assertEquals("Martin", _completer.complete("Ma"));
		TestCase.assertEquals("Martin", _completer.complete("Martin"));
		TestCase.assertNull(_completer.complete("Margin"));
		TestCase.assertNull(_completer.complete("Morganm"));
	}
}
