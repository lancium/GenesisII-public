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
package org.morgan.util.launcher;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Launcher
{
	static private Log _logger = LogFactory.getLog(Launcher.class);

	static public void main(String []args) 
		throws IOException, ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException
	{
		if (args.length < 2)
		{
			System.err.println("USAGE:  Launcher <jar-desc> <class> [ <args > ]");
			System.exit(1);
		}
		
		JarDescription description = new JarDescription(args[0]);
		ClassLoader loader = description.createClassLoader();
		Thread.currentThread().setContextClassLoader(loader);
		Class<?> cl = loader.loadClass(args[1]);
		Method main = cl.getMethod("main", new Class[] { String[].class });
		String []params = new String[args.length - 2];
		System.arraycopy(args, 2, params, 0, params.length);
		
		try
		{
			main.invoke(null, new Object[] { params });
		}
		catch (InvocationTargetException ite)
		{
			_logger.info("exception occurred in main", ite);
			System.exit(1);
		}
	}
}
