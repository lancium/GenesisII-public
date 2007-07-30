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

import org.morgan.util.Version;
import org.morgan.util.io.StreamUtils;
import org.morgan.util.updater.VersionList;

import junit.framework.TestCase;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class VersionListTest extends TestCase
{
	public void testParsing() throws Exception
	{
		InputStream in = null;
		
		try
		{
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				"org/morgan/util/test/example-version-list.ver");
			VersionList vList = new VersionList(in);
			
			String []files = vList.getRelativeFiles();
			TestCase.assertEquals(3, files.length);
			TestCase.assertEquals(new Version(1, 0, 0),
				vList.getVersion("foo/bar/com.1"));
			TestCase.assertEquals(new Version(1, 2, 0),
					vList.getVersion("foo/bar/com.2"));
			TestCase.assertEquals(new Version(1, 2, 3),
					vList.getVersion("foo/bar/com.3"));
			
			vList.store(System.out);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
}
