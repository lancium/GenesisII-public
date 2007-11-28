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
package edu.virginia.vcgr.genii.client.exportdir;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class ExportedDirUtils
{
	static final protected String _PATH_ELEM_NAME = "path";
	static final protected String _PARENT_IDS_ELEM_NAME = "parent-ids";
	static final public String _PARENT_ID_BEGIN_DELIMITER = ":";
	static final public String _PARENT_ID_END_DELIMITER = ":";
	
	static public class ExportedDirInitInfo
	{
		private String _path = null;
		private String _parentIds = null;
	
		public ExportedDirInitInfo(String path, String parentIds)
		{
			_path = path;
			_parentIds = parentIds;
		}
		
		public String getPath()
		{
			return _path;
		}
		
		public String getParentIds()
		{
			return _parentIds;
		}
	}
	
	static public MessageElement[] createCreationProperties(
		String path, String parentIds)
	{
		MessageElement []any = new MessageElement[2];
		any[0] = new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _PATH_ELEM_NAME), path);
		any[1] = new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _PARENT_IDS_ELEM_NAME), parentIds);
		
		return any;
	}
	
	static public ExportedDirInitInfo extractCreationProperties(
		HashMap<QName, Object> properties) throws ResourceException
	{
		String path = null;
		String parentIds = null;
		
		if (properties == null)
			throw new IllegalArgumentException(
				"Can't have a null creation properites parameter.");

		MessageElement pathElement = (MessageElement)properties.get(new QName(
				GenesisIIConstants.GENESISII_NS, _PATH_ELEM_NAME));
		if (pathElement == null)
			throw new IllegalArgumentException(
				"Couldn't find path in creation properties.");
		path = pathElement.getValue();
		
		MessageElement parentIDSElement = 
			(MessageElement)properties.get(new QName(
					GenesisIIConstants.GENESISII_NS, _PARENT_IDS_ELEM_NAME));
		if (parentIDSElement == null)
			throw new IllegalArgumentException(
				"Couldn't find parentIds in creation properties.");
		parentIds = parentIDSElement.getValue();
		if (parentIds == null)
			parentIds = "";
		
		if (path == null)
			throw new IllegalArgumentException(
				"Couldn't find path in creation properties.");
		if (parentIds == null)
			throw new IllegalArgumentException(
				"Couldn't find parent IDs in creation properties.");
		
		return new ExportedDirInitInfo(path, parentIds);
	}
	
	static public String createParentIdsString(String ancestorIdString, String parentId)
	{
		if (ancestorIdString == null)
		{
			ancestorIdString = "";
		}
		if (parentId == null)
			return null;
		
		return ancestorIdString + _PARENT_ID_BEGIN_DELIMITER + parentId + _PARENT_ID_END_DELIMITER;
	}
	
	/*
	 * createLocalDir - Create new directory in local file system
	 * 
	 * @param path Path to new directory
	 * @return boolean Return true if dir does not exist and could be created.  
	 *         False if dir exists.  Pass through IOExceptions from create.
	 */
	static public boolean createLocalDir(String path)
	{
		File newFile = new File(path);
		
		return newFile.mkdir();
	}

	/*
	 * dirReadable - Return whether path points to a readable directory or not.
	 * 
	 * @param path Path to directory
	 * @return boolean true if path exists, is a directory and is readable.  Otherwise false.  
	 */
	static public boolean dirReadable(String path)
		throws IOException
	{
		File testFile = new File(path);
		
		if (testFile.exists() && testFile.isDirectory() && testFile.canRead())
			return true;
		return false;
	}
}
