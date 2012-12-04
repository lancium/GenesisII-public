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
package edu.virginia.vcgr.genii.container;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.morgan.util.io.StreamUtils;
import org.mortbay.jetty.HttpException;
import org.mortbay.jetty.handler.AbstractHandler;

public class ResourceFileHandler extends AbstractHandler
{
	static final long serialVersionUID = 0;
		
	private String _resourceBase;
	
	public ResourceFileHandler(String resourceBase)
	{
		_resourceBase = resourceBase;
		while (_resourceBase.endsWith("/"))
		{
			_resourceBase = _resourceBase.substring(0, _resourceBase.length());
		}
	}
	
	public void handle(String target, HttpServletRequest arg2, 
			HttpServletResponse arg3, int dispatch) 
			throws HttpException, IOException, ServletException
	{
		InputStream in = null;
		OutputStream out = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		try
		{
			out = arg3.getOutputStream();
			
			if (!target.endsWith("/"))
			{
				in = loader.getResourceAsStream(_resourceBase + target);
				if (in == null)
				{
					in = loader.getResourceAsStream(
							_resourceBase + target + "/index.html");
				}
			} else
			{
				in = loader.getResourceAsStream(_resourceBase + target +
					"index.html");
			}
			
			if (in == null)
			{
				throw new IOException(
					"Couldn't find resource \"" + target + "\".");
			}
			
			StreamUtils.copyStream(in, out);
//			arg3.commit();
		}
		catch (IOException ioe)
		{
			if (out != null)
			{
				PrintStream ps = new PrintStream(out);
				ps.println("<html><head><title>Error Page</title></head><body>");
				ps.println("Error trying to generate static web page:");
				ps.println("<pre>");
				ioe.printStackTrace(ps);
				ps.println("</pre>");
			} else
				throw ioe;
		}
		finally
		{
			StreamUtils.close(in);
			StreamUtils.close(out);
		}
	}
}
