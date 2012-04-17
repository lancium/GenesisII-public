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
package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface IRByteIOResource extends IResource
{
	public File chooseFile(HashMap<QName, Object> creationProperties)
		throws ResourceException;
	public File getCurrentFile() throws ResourceException;
	public void destroy() throws ResourceException;
	public void setFilePath(String path) throws ResourceException;
	public String getFilePath()	throws ResourceException;
	public void setCreateTime(Calendar tm) throws ResourceException;
	public Calendar getCreateTime()	throws ResourceException;
	public void setModTime(Calendar tm)	throws ResourceException;
	public Calendar getModTime() throws ResourceException;
	public void setAccessTime(Calendar tm) throws ResourceException;
	public Calendar getAccessTime()	throws ResourceException;
	public void setBitmapFilePath(String path) throws ResourceException;
	public String getBitmapFilePath() throws ResourceException;
}
