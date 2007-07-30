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
package edu.virginia.vcgr.genii.ftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.ws.addressing.EndpointReferenceType;

class RedirectFile
{
	private byte []content;
	
	public RedirectFile(EndpointReferenceType epr)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream writer = new PrintStream(baos);
		
		writer.println("<HTML>");
		writer.println("<HEAD>");
		writer.println("<META HTTP-EQUIV=\"Refresh\"");
		writer.println("CONTENT=\"0; URL="
			+ epr.getAddress().get_value() + "\">");
		writer.println("</HEAD>");
		writer.println("<BODY>");
		writer.println("If you are not automatically redirected, please click ");
		writer.println("<A HREF=\"" + epr.getAddress().get_value() +
			"\"> here</A>");
		writer.println("</BODY>");
		writer.println("</HTML>");
		
		writer.flush();
		writer.close();
		
		content = baos.toByteArray();
	}
	
	public long getSize()
	{
		return content.length;
	}
	
	public InputStream getStream()
	{
		return new ByteArrayInputStream(content);
	}
}
