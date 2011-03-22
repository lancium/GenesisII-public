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
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class ExportedDirUtils
{
	static final protected String _PATH_ELEM_NAME = "path";
	static final protected String _PARENT_IDS_ELEM_NAME = "parent-ids";
	static final protected String _REPLICATION_INDICATOR = "replicate";
	static final protected String _LAST_MODIFIED_TIME = "modify-time";
	static final protected String _REXPORT_RESOLVER_EPR = "rexport-resolver-service-epr";
	static final public String _PARENT_ID_BEGIN_DELIMITER = ":";
	static final public String _PARENT_ID_END_DELIMITER = ":";
	static final public String _SVN_USERNAME = "svn-user";
	static final public String _SVN_PASSWORD = "svn-pass";
	static final public String _SVN_REVISION = "svn-revision";
	
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(ExportedDirUtils.class);
	
	static public class ExportedDirInitInfo
	{
		private String _path = null;
		private String _parentIds = null;
		private String _isReplicated = null;
		private Long _lastModified = null;
		private EndpointReferenceType _resolverServiceEPR = null;
		private String _svnUser = null;
		private String _svnPass = null;
		private Long _svnRevision = null;
	
		/*public ExportedDirInitInfo(String path, String parentIds, String isReplicated)
		{
			_path = path;
			_parentIds = parentIds;
			_isReplicated = isReplicated;
		}
		*/
		public ExportedDirInitInfo(String path, String parentIds, String isReplicated,
			Long lastModified, EndpointReferenceType resolverServiceEPR,
			String svnUser, String svnPass, Long svnRevision)
		{
			_path = path;
			_parentIds = parentIds;
			_isReplicated = isReplicated;
			_lastModified = lastModified;
			_resolverServiceEPR = resolverServiceEPR;
			_svnUser = svnUser;
			_svnPass = svnPass;
			_svnRevision = svnRevision;
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
		
		public Long getLastModifiedTime(){
			return _lastModified;
		}
		
		public EndpointReferenceType getResolverFactoryEPR()
		{
			return _resolverServiceEPR;
		}
		
		public String svnUser()
		{
			return _svnUser;
		}
		
		public String svnPass()
		{
			return _svnPass;
		}
		
		public Long svnRevision()
		{
			return _svnRevision;
		}
	}
	
	static public MessageElement[] createCreationProperties(String humanName,
		String path, String svnUser, String svnPass, Long svnRevision,
		String parentIds, String isReplicated) throws RemoteException
	{
		Collection<MessageElement> any = new Vector<MessageElement>(6);
		any.add(new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _PATH_ELEM_NAME), path));
		any.add(new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _PARENT_IDS_ELEM_NAME), parentIds));
		any.add(new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _REPLICATION_INDICATOR), isReplicated));
		any.add(new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _LAST_MODIFIED_TIME), 
			getLastModifiedTime(path)));
		any.add(new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _REXPORT_RESOLVER_EPR),
			null));
		any.add(new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _SVN_USERNAME),
			svnUser));
		any.add(new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _SVN_PASSWORD),
			svnPass));
		any.add(new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _SVN_REVISION),
			svnRevision));
		
		if (humanName != null)
		{
			ConstructionParameters cParams = new ConstructionParameters();
			cParams.humanName(humanName);
			any.add(cParams.serializeToMessageElement());
		}
		
		return any.toArray(new MessageElement[any.size()]);
	}
	
	static public MessageElement[] createReplicationCreationProperties(
			String path, String parentIds, String isReplicated,
			EndpointReferenceType replicationServiceEPR)
		{
			MessageElement []any = new MessageElement[5];
			any[0] = new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _PATH_ELEM_NAME), path);
			any[1] = new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _PARENT_IDS_ELEM_NAME), parentIds);
			any[2] = new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _REPLICATION_INDICATOR), isReplicated);
			any[3] = new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _LAST_MODIFIED_TIME), 
				getLastModifiedTime(path));
			any[4] = new MessageElement(new QName(
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
	}
	
	static public ExportedDirInitInfo extractCreationProperties(
		HashMap<QName, Object> properties) throws ResourceException
	{
		String path = null;
		String parentIds = null;
		String isReplicated = null;
		Long lastModified = null;
		EndpointReferenceType resolverServiceEPR = null;
		String svnUser = null;
		String svnPass = null;
		Long svnRevision = null;
		
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
		
		// get svn user
		MessageElement svnUserElement =
			(MessageElement)properties.get(new QName(
				GenesisIIConstants.GENESISII_NS, _SVN_USERNAME));
		if (svnUserElement != null)
			svnUser = svnUserElement.getValue();

		// get svn pass
		MessageElement svnPassElement =
			(MessageElement)properties.get(new QName(
				GenesisIIConstants.GENESISII_NS, _SVN_PASSWORD));
		if (svnPassElement != null)
			svnPass = svnPassElement.getValue();
		
		// get svn revision
		MessageElement svnRevisionElement =
			(MessageElement)properties.get(new QName(
				GenesisIIConstants.GENESISII_NS, _SVN_REVISION));
		try
		{
			if (svnRevisionElement != null && svnRevisionElement.getValue() != null)
				svnRevision = (Long)svnRevisionElement.getObjectValue(Long.class);
		}
		catch (Exception e)
		{
			throw new ResourceException("Unable to extract svn revision.", e);
		}
		
		//get replication state
		MessageElement replicationElement = 
			(MessageElement)properties.get(new QName(
				GenesisIIConstants.GENESISII_NS, _REPLICATION_INDICATOR));
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
			if (resolverServiceElement.getObjectValue()!= null)
				resolverServiceEPR = (EndpointReferenceType) resolverServiceElement.getObjectValue(
						EndpointReferenceType.class);
		}
		catch(Exception e){
			throw new ResourceException("Uable to extract resolver factory epr: " 
					+ e.getMessage());
		}
		
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
		
		//get last modified time
		lastModified = getLastModifiedTime(path);
		
		return new ExportedDirInitInfo(path, parentIds, 
				isReplicated, lastModified, resolverServiceEPR,
				svnUser, svnPass, svnRevision);
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
	
	/**
	 * 
	 */
	static public long getLastModifiedTime(String path){
		long lastModifiedTime = 0;
		File testFile = new File(path);
		
		if ((testFile.exists()) && testFile.isDirectory())
			lastModifiedTime = testFile.lastModified();
		
		return lastModifiedTime;
	}
}
