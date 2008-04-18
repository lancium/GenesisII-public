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

public class ExportedFileUtils
{
	static final protected String _PATH_ELEM_NAME = "path";
	static final protected String _PARENT_IDS_ELEM_NAME = "parent-ids";
	static final protected String _REPLICATION_INDICATOR_ = "replicate";
	static final public String _PARENT_ID_BEGIN_DELIMITER = ":";
	static final public String _PARENT_ID_END_DELIMITER = ":";
	
	static public class ExportedFileInitInfo
	{
		private String _path = null;
		private String _parentIds = null;
		private String _isReplicated = null;
	
		public ExportedFileInitInfo(String path, String parentIds, String isReplicated)
		{
			_path = path;
			_parentIds = parentIds;
			_isReplicated = isReplicated;
		}
		
		public String getPath()
		{
			return _path;
		}
		
		public String getParentIds()
		{
			return _parentIds;
		}
		
		public String getReplicationState()
		{
			return _isReplicated;
		}
	}
	
	static public MessageElement[] createCreationProperties(
		String path, String parentIds, String isReplicated)
	{
		MessageElement[]ret = new MessageElement[3];
		
		ret[0] = new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _PATH_ELEM_NAME), path);
		ret[1] = new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _PARENT_IDS_ELEM_NAME), parentIds);
		ret[2] = new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _REPLICATION_INDICATOR_), 
				isReplicated);
		return ret;
	}
	
	static public ExportedFileInitInfo extractCreationProperties(
		HashMap<QName, Object> properties) throws ResourceException
	{
		String path = null;
		String parentIds = null;
		String isReplicated = null;
		
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
		
		//get replication state
		MessageElement replicationElement = (MessageElement)properties.get(new QName(
				GenesisIIConstants.GENESISII_NS, _REPLICATION_INDICATOR_));
		if (replicationElement == null)
			throw new IllegalArgumentException(
				"Couldn't find replication indicator in export creation properties.");
		isReplicated = replicationElement.getValue();
		
		if (path == null)
			throw new IllegalArgumentException(
				"Couldn't find path in creation properties.");
		if (parentIds == null)
			throw new IllegalArgumentException(
				"Couldn't find parent IDs in creation properties.");
		if (isReplicated == null)
			throw new IllegalArgumentException(
				"Couldn't find replication indicator in export creation properties.");
		
		return new ExportedFileInitInfo(path, parentIds, isReplicated);
	}
	
	static public String createFullPath(String dirName, String fileName) 
	{
		return ((new File(dirName, fileName)).getAbsolutePath());
	}

	/*
	 * createLocalFile - Create new file in local file system
	 * 
	 * @param path Path to new file
	 * @return boolean Return true if file does not exist and could be created.  
	 *         False if file exists.  Pass through IOExceptions from create.
	 */
	static public boolean createLocalFile(String path)
		throws IOException
	{
		File newFile = new File(path);
		
		return newFile.createNewFile();
	}
}
