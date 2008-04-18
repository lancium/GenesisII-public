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
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class ExportedDirUtils
{
	static final protected String _PATH_ELEM_NAME = "path";
	static final protected String _PARENT_IDS_ELEM_NAME = "parent-ids";
	static final protected String _REPLICATION_INDICATOR_ = "replicate";
	static final protected String _REXPORT_RESOLVER_EPR = "rexport-resolver-service-epr";
	static final public String _PARENT_ID_BEGIN_DELIMITER = ":";
	static final public String _PARENT_ID_END_DELIMITER = ":";
	
	static public class ExportedDirInitInfo
	{
		private String _path = null;
		private String _parentIds = null;
		private String _isReplicated = null;
		private EndpointReferenceType _resolverServiceEPR = null;
	
		public ExportedDirInitInfo(String path, String parentIds, String isReplicated)
		{
			_path = path;
			_parentIds = parentIds;
			_isReplicated = isReplicated;
		}
		
		public ExportedDirInitInfo(String path, String parentIds, String isReplicated,
				EndpointReferenceType resolverServiceEPR)
		{
			_path = path;
			_parentIds = parentIds;
			_isReplicated = isReplicated;
			_resolverServiceEPR = resolverServiceEPR;
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
		
		public EndpointReferenceType getResolverFactoryEPR()
		{
			return _resolverServiceEPR;
		}
	}
	
	static public MessageElement[] createCreationProperties(
		String path, String parentIds, String isReplicated)
	{
		MessageElement []any = new MessageElement[4];
		any[0] = new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _PATH_ELEM_NAME), path);
		any[1] = new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _PARENT_IDS_ELEM_NAME), parentIds);
		any[2] = new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _REPLICATION_INDICATOR_), isReplicated);
		any[3] = new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _REXPORT_RESOLVER_EPR),
				null);
		
		return any;
	}
	
	static public MessageElement[] createReplicationCreationProperties(
			String path, String parentIds, String isReplicated,
			EndpointReferenceType replicationServiceEPR)
		{
			MessageElement []any = new MessageElement[4];
			any[0] = new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _PATH_ELEM_NAME), path);
			any[1] = new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _PARENT_IDS_ELEM_NAME), parentIds);
			any[2] = new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _REPLICATION_INDICATOR_), isReplicated);
			any[3] = new MessageElement(new QName(
					GenesisIIConstants.GENESISII_NS, _REXPORT_RESOLVER_EPR),
					replicationServiceEPR);
			
			return any;
		}
	
	/**
	 * Add creation parameters for the resolver from the passed in creation params 
	 * for export root creation.
	 * Localpath of export on primary and the epr of the resolver service are added.
	 * 
	 * @param resolverCreationParams: collection to which resolver creation params are added.
	 * @param initInfo: Contains the processed creation parameters for export creation.
	 */
	static public void createResolverCreationProperties(
			Collection<MessageElement> resolverCreationParams,
			ExportedDirInitInfo initInfo)
	{
		resolverCreationParams.add(new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _PATH_ELEM_NAME), initInfo.getPath()));
		resolverCreationParams.add(new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _REXPORT_RESOLVER_EPR), 
				initInfo.getResolverFactoryEPR()));
		
				/*
		params[0] = new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _PATH_ELEM_NAME), initInfo.getPath());
		params[1] = new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _REXPORT_RESOLVER_EPR), 
				initInfo.getResolverFactoryEPR());
		*/
	}
	
	static public ExportedDirInitInfo extractCreationProperties(
		HashMap<QName, Object> properties) throws ResourceException
	{
		String path = null;
		String parentIds = null;
		String isReplicated = null;
		EndpointReferenceType resolverServiceEPR = null;
		
		if (properties == null)
			throw new IllegalArgumentException(
				"Can't have a null export creation properites parameter.");

		//get path
		MessageElement pathElement = 
			(MessageElement)properties.get(new QName(
				GenesisIIConstants.GENESISII_NS, _PATH_ELEM_NAME));
		if (pathElement == null)
			throw new IllegalArgumentException(
				"Couldn't find path in export creation properties.");
		path = pathElement.getValue();
		
		//get parentIds
		MessageElement parentIDSElement = 
			(MessageElement)properties.get(new QName(
				GenesisIIConstants.GENESISII_NS, _PARENT_IDS_ELEM_NAME));
		if (parentIDSElement == null)
			throw new IllegalArgumentException(
				"Couldn't find parentIds in export creation properties.");
		parentIds = parentIDSElement.getValue();
		if (parentIds == null)
			parentIds = "";
	
		//get replication state
		MessageElement replicationElement = 
			(MessageElement)properties.get(new QName(
				GenesisIIConstants.GENESISII_NS, _REPLICATION_INDICATOR_));
		if (replicationElement == null)
			throw new IllegalArgumentException(
				"Couldn't find replication indicator in export creation properties.");
		isReplicated = replicationElement.getValue();
		
		//get resolver service epr
		MessageElement resolverServiceElement = 
			(MessageElement)properties.get(new QName(
				GenesisIIConstants.GENESISII_NS, _REXPORT_RESOLVER_EPR));
		if (resolverServiceElement == null)
			throw new IllegalArgumentException(
				"Couldn't find resolver service epr in export creation properties.");
		try{
			resolverServiceEPR = (EndpointReferenceType) resolverServiceElement.getObjectValue(
				EndpointReferenceType.class);
		}
		catch(Exception e){
			throw new ResourceException("Uable to extract resolver factory epr: " 
					+ e.getMessage());
		}
		
		//isReplicated = (String) properties.get(new QName(
		//		GenesisIIConstants.GENESISII_NS, _REPLICATION_INDICATOR_));
		
		//ensure all properties filled in
		if (path == null)
			throw new IllegalArgumentException(
				"Couldn't find path in export creation properties.");
		if (parentIds == null)
			throw new IllegalArgumentException(
				"Couldn't find parent IDs in export creation properties.");
		if (isReplicated == null)
			throw new IllegalArgumentException(
				"Couldn't find replication indicator in export creation properties.");
		
		return new ExportedDirInitInfo(path, parentIds, isReplicated, resolverServiceEPR);
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
