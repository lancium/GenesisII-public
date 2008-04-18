package edu.virginia.vcgr.genii.client.replicatedExport;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.ClientConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class RExportUtils {
	static final protected String _PATH_NAME = "localpath-construction-param";
	static final protected String _PARENT_IDS = "parent-ids-construction-param";
	
	static final public String _PARENT_ID_BEGIN_DELIMITER = ":";
	static final public String _PARENT_ID_END_DELIMITER = ":";
	
	static public class RExportInitInfo{
		private URI _epi = null;
		private String _path = null;
		private String _parentIds = null;
	
		public RExportInitInfo(String path, String parentIds, URI epi){
			_epi = epi;
			_path = path;
			_parentIds = parentIds;
		}
		
		public URI getEPI(){
			return _epi;
		}
		
		public String getPath(){
			return _path;
		}
		
		public String getParentIds(){
			return _parentIds;
		}
	}
	
	static public MessageElement[] createCreationProperties(
			String path, String parentIds, URI epi){
		MessageElement []creationProperties = new MessageElement[3];
		creationProperties[0] = ClientConstructionParameters.createEndpointIdentifierProperty(epi);
		creationProperties[1] = new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _PATH_NAME), path);
		creationProperties[2] = new MessageElement(new QName(
				GenesisIIConstants.GENESISII_NS, _PARENT_IDS), parentIds);
			
		return creationProperties;
	}
	
	static public RExportInitInfo extractCreationProperties(
			HashMap<QName, Object> properties) throws ResourceException{
		URI epi = null;
		String path = null;
		String parentIds = null;
		
		if (properties == null)
			throw new IllegalArgumentException(
				"Null creation properites parameter.");

		//Get common EPI
		MessageElement epiElement = (MessageElement)properties.get(
				IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM);
		try{
			epi = ClientConstructionParameters.getEndpointIdentifierProperty(epiElement);
		}
		catch(URISyntaxException use){
			throw new ResourceException("malformed epi for RExport: " + use);
		}
		if (epi == null)
			throw new IllegalArgumentException(
				"Can't find epi in creation properties.");
		
		//Get local path of export
		MessageElement pathElement = (MessageElement)properties.get(new QName(
				GenesisIIConstants.GENESISII_NS, _PATH_NAME));
		if (pathElement == null)
			throw new IllegalArgumentException(
				"Can't find local path in creation properties.");
		path = pathElement.getValue();
		
		if (path == null)
			throw new IllegalArgumentException(
				"Couldn't find path in creation properties.");
		
		//Get list of container names - first name is location of primary
		MessageElement parentIdsElement = 
			(MessageElement)properties.get(new QName(
					GenesisIIConstants.GENESISII_NS, _PARENT_IDS));
		if (parentIdsElement == null)
			throw new IllegalArgumentException(
				"Can't find parentIds names in creation properties.");
		parentIds = parentIdsElement.getValue();
		
		if (parentIds == null)
			parentIds = "";
		
		return new RExportInitInfo(path, parentIds, epi);
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
