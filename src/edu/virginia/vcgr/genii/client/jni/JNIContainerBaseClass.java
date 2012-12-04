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
package edu.virginia.vcgr.genii.client.jni;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;

public class JNIContainerBaseClass
{
	static private Log _logger = LogFactory.getLog(JNIContainerBaseClass.class);
	
	static public final String VCGR_CONTAINER_LIB_NAME = "VcgrContainerLib";
	
	static 
	{
		try 
		{
			System.loadLibrary(VCGR_CONTAINER_LIB_NAME);
		}
		catch (UnsatisfiedLinkError e)
		{
			OperatingSystemType os = OperatingSystemType.getCurrent();
			if (os == OperatingSystemType.LINUX) {
				_logger.trace("saw expected failure to load library " + VCGR_CONTAINER_LIB_NAME + " on linux OS.");
			} else {
				_logger.warn("Problem loading shared library " + VCGR_CONTAINER_LIB_NAME, e);
			}
		}
	}
}
